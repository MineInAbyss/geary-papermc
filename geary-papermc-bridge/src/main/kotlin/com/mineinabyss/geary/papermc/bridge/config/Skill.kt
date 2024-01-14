package com.mineinabyss.geary.papermc.bridge.config

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.bridge.config.inputs.Variables
import com.mineinabyss.geary.serialization.serializers.CustomMapSerializer
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.SerializableGearyEntity
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Skill.Serializer::class)
class Skill(
    val event: EventComponent? = null,
    val using: SetTarget? = null,
    val vars: List<Variables>? = null,
    val conditions: List<GearyEntity>? = null,
    val run: Skills? = null,
    val execute: GearyEntity? = null,
) {
    //TODO how do we handle creating children and lookup for variables, separate entry or on execute?
    class Serializer : KSerializer<Skill> {
        val polymorphic = PolymorphicListAsMapSerializer.ofComponents()
        override val descriptor: SerialDescriptor =
            SerialDescriptor(
                "geary:skill",
                MapSerializer(String.serializer(), ContextualSerializer(Any::class)).descriptor
            )

        override fun deserialize(decoder: Decoder): Skill {
            var event: EventComponent? = null
            var using: SetTarget? = null
            var vars: List<Variables>? = null
            var conditions: List<SerializableGearyEntity>? = null
            var run: Skills? = null
            val execute = mutableListOf<Any>()

            val mapSerializer = object : CustomMapSerializer() {
                override fun decode(key: String, compositeDecoder: CompositeDecoder) {
                    val module = compositeDecoder.serializersModule
                    when (key) {
                        "event" -> event = compositeDecoder.decodeMapValue(EventComponent.serializer())
                        "using" -> using = compositeDecoder.decodeMapValue(SetTarget.serializer())
                        "vars" -> vars = compositeDecoder.decodeMapValue(ListSerializer(Variables.serializer()))
                        "conditions" -> conditions =
                            compositeDecoder.decodeMapValue(ListSerializer(GearyEntitySerializer))

                        "run" -> run = compositeDecoder.decodeMapValue(Skills.serializer())
                        else -> execute += compositeDecoder.decodeMapValue(
                            polymorphic.findSerializerFor(module, polymorphic.getNamespaces(module), key)
                        )
                    }
                }
            }
            mapSerializer.deserialize(decoder)
            return Skill(
                event = event,
                using = using,
                vars = vars,
                conditions = conditions,
                run = run,
                execute = entity {
                    setAll(execute)
                },
            )
        }

        override fun serialize(encoder: Encoder, value: Skill) {
            TODO("Not yet implemented")
        }
    }
}
