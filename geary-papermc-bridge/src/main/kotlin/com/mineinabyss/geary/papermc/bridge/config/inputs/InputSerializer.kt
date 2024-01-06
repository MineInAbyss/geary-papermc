package com.mineinabyss.geary.papermc.bridge.config.inputs

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.yamlMap
import com.charleskorn.kaml.yamlScalar
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class InputSerializer<T : Any>(val serializer: KSerializer<T>) : KSerializer<Input<T>> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("InputSerializer", SerialKind.CONTEXTUAL)
    var forceDerived: Boolean = false

    override fun deserialize(decoder: Decoder): Input<T> {

        val type =
            if (serializer == GearyEntitySerializer) componentId<GearyEntity>()
            else componentId(
                serializableComponents.serializers.getKClassFor(serializer)
                    ?: error("$serializer was not registered as a component in Geary")
            )

        // Try reading as a derived variable, don't use the $: property
        if (forceDerived) {
            return Input.Derived(type, GearyEntitySerializer.deserialize(decoder))
        }

        // Try reading a single line string as a variable reference starting with $
        val compositeString = runCatching {
            decoder.beginStructure(String.serializer().descriptor) as YamlInput
        }.getOrNull()
        if (compositeString != null && compositeString.node.yamlScalar.content.startsWith("$")) {
            val expression = compositeString.decodeString()
            compositeString.endStructure(String.serializer().descriptor)
            return Input.VariableReference(type, expression.removePrefix("$"))
        }

        // Try reading as a derived variable, with a single $: property indicating this
        val compositeMap =
            runCatching { decoder.beginStructure(InPlaceInput.serializer().descriptor) as YamlInput }.getOrNull()
        if (compositeMap != null) {
            val map = compositeMap.node.yamlMap.entries
            compositeMap.endStructure(InPlaceInput.serializer().descriptor)
            if (map.size == 1 && map.keys.first().content == "\$derived") {
                return Input.Derived(
                    type,
                    InPlaceInput.serializer().deserialize(decoder).input
                )
            }
        }

        // Fallback to reading the value in-place
        val composite = decoder.beginStructure(serializer.descriptor)

        return Input.Value(
            composite.decodeSerializableElement(
                serializer.descriptor,
                0,
                serializer
            )
        )
    }

    override fun serialize(encoder: Encoder, value: Input<T>) {
        TODO("Not yet implemented")
    }
}
