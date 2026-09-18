package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.prefabs.PrefabKey
import net.kyori.adventure.key.Key
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

/**
 * Registers this prefab's item with Nexo as a custom block, equivalent to writing it in Nexo's items-folder.
 *
 * The component body is Nexo's `Mechanics.custom_block` section, handed to the custom block factory untouched,
 * so it takes everything that block does in an item config. `type` is required, one of Nexo's registered block
 * types, and `custom_variation` is picked automatically from the `model` when left out.
 */
@Serializable
@SerialName("nexo:custom_block")
@JvmInline
value class NexoCustomBlock(val mechanic: RawConfig = RawConfig()) {
    fun toItemSection(prefabKey: PrefabKey, material: Material?, itemModel: Key?): ConfigurationSection =
        mechanic.toItemSection(prefabKey, material, itemModel, "custom_block")
}
