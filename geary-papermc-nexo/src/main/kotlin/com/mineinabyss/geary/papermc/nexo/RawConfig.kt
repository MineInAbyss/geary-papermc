package com.mineinabyss.geary.papermc.nexo

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTaggedNode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A chunk of prefab yaml kept as plain values, so it can be handed to plugins that read Bukkit configs.
 *
 * Only readable from yaml, since there is no schema to map any other format onto.
 */
@kotlinx.serialization.Serializable(with = RawConfig.Serializer::class)
class RawConfig(val values: Map<String, Any?> = emptyMap()) {
    object Serializer : KSerializer<RawConfig> {
        override val descriptor = MapSerializer(String.serializer(), String.serializer()).descriptor

        override fun deserialize(decoder: Decoder): RawConfig {
            val input = decoder as? YamlInput
                ?: throw SerializationException("Nexo config can only be read from yaml, got ${decoder::class.simpleName}")

            @Suppress("UNCHECKED_CAST")
            return RawConfig(input.node.toPlainValue() as? Map<String, Any?> ?: emptyMap())
        }

        override fun serialize(encoder: Encoder, value: RawConfig) {
            throw SerializationException("Nexo config is read-only, it only exists to pass prefab yaml on to Nexo")
        }

        private fun YamlNode.toPlainValue(): Any? = when (this) {
            is YamlNull -> null
            // Bukkit configs are typed, so scalars have to be narrowed the same way snakeyaml would.
            // Int before Long matters, plugins check for it, ConfigurationSection.isInt is false for a Long
            is YamlScalar -> content.toBooleanStrictOrNull() ?: content.toIntOrNull() ?: content.toLongOrNull()
                ?: content.toDoubleOrNull() ?: content
            is YamlList -> items.map { it.toPlainValue() }
            is YamlMap -> entries.entries.associate { (key, node) -> key.content to node.toPlainValue() }
            is YamlTaggedNode -> innerNode.toPlainValue()
        }
    }
}
