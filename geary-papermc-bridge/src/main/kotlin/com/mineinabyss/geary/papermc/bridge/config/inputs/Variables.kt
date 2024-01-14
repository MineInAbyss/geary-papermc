package com.mineinabyss.geary.papermc.bridge.config.inputs

import com.mineinabyss.geary.serialization.serializers.CustomMapSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Variables.Serializer::class)
class Variables(
    val entries: Map<String, Input<*>>
) {

    fun plus(other: Variables): Variables {
        return Variables(entries + other.entries)
    }

    fun evaluated(entities: Input.Entities): Variables {
        return Variables(entries
            .mapValues { (_, input) -> input.evaluate(entities) })
    }

    class Serializer : KSerializer<Variables> {
        val polymorphic = PolymorphicListAsMapSerializer.ofComponents()
        override val descriptor = SerialDescriptor("geary:variables", polymorphic.descriptor)

        override fun deserialize(decoder: Decoder): Variables {
            val module = decoder.serializersModule
            val namespaces = polymorphic.getNamespaces(module)
            val inputs = mutableMapOf<String, Input<*>>()

            val mapSerializer = object : CustomMapSerializer() {
                override fun decode(key: String, compositeDecoder: CompositeDecoder) {
                    val derived = key.startsWith("derived")
                    val (type, name) = key.removePrefix("derived ").split(" ")
                    val serializer = polymorphic.findSerializerFor(module, namespaces, type)
                    val inputSerializer = InputSerializer(serializer).apply {
                        forceDerived = derived
                    }
                    inputs[name] = compositeDecoder.decodeMapValue(inputSerializer)
                }
            }
            mapSerializer.deserialize(decoder)
            return Variables(inputs)
        }

        override fun serialize(encoder: Encoder, value: Variables) {
            TODO("Not yet implemented")
        }
    }
}
