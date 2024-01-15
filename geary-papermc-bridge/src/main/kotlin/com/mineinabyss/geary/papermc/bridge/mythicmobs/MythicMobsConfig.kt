package com.mineinabyss.geary.papermc.bridge.mythicmobs

import com.charleskorn.kaml.YamlInput
import com.mineinabyss.geary.datatypes.ComponentDefinition
import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.papermc.bridge.events.entities.OnSpawn
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
import io.lumine.mythic.bukkit.MythicBukkit
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MythicMobsConfig.Serializer::class)
class MythicMobsConfig(val yaml: String) {
    class Serializer : KSerializer<MythicMobsConfig> {
        override val descriptor =
            SerialDescriptor(
                "geary:mythic",
                MapSerializer(String.serializer(), ContextualSerializer(Any::class)).descriptor
            )

        override fun deserialize(decoder: Decoder): MythicMobsConfig {
            val structure = decoder.beginStructure(descriptor)
            require(structure is YamlInput)
            return MythicMobsConfig(structure.node.contentToString())
        }

        override fun serialize(encoder: Encoder, value: MythicMobsConfig) {
            TODO("Not yet implemented")
        }

    }

    fun register() {
        val config = MythicBukkit.inst().skillManager
        config.loadFromString(yaml)
        config.
    }
    companion object: ComponentDefinition by EventHelpers.defaultTo<OnPrefabRegister>()
}


class RegisterMythicConfigurationFromComponentSystem() : GearyListener() {
    val Pointers.mythicConfig by get<MythicMobsConfig>().on(source)
    override fun Pointers.handle() {
        TODO("Not yet implemented")
    }

}
