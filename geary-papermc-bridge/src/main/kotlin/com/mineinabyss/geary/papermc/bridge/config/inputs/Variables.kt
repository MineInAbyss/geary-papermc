package com.mineinabyss.geary.papermc.bridge.config.inputs

import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Variables.Serializer::class)
class Variables(
    val entries: Map<String, Input<*>>
) {
    class Serializer : KSerializer<Variables> {
        private val polymorphicSerializer = object : PolymorphicListAsMapSerializer<Any>(
            PolymorphicSerializer(Any::class)
        ) {
            override fun decodeEntry(key: String, compositeDecoder: CompositeDecoder, namespaces: List<String>): Any {
                val derived = key.startsWith("derived")
                val (type, name) = key.removePrefix("derived ").split(" ")
                val serializer = findSerializerFor(compositeDecoder.serializersModule, namespaces, type)
                val inputSerializer = InputSerializer(serializer).apply {
                    forceDerived = derived
                }
                return name to compositeDecoder.decodeMapValue(inputSerializer)
            }
        }

        override val descriptor = SerialDescriptor("geary:variables", polymorphicSerializer.descriptor)

        override fun deserialize(decoder: Decoder): Variables {
            val input = polymorphicSerializer.deserialize(decoder) as List<Pair<String, Input<*>>>
            return Variables(input.toMap())
        }

        override fun serialize(encoder: Encoder, value: Variables) {
            TODO("Not yet implemented")
        }
    }
}
