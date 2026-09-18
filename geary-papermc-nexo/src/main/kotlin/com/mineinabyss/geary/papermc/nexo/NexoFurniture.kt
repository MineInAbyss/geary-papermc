package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.prefabs.PrefabKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

/**
 * Registers this prefab's item with Nexo as furniture, equivalent to writing it in Nexo's items-folder.
 *
 * The component body is Nexo's `Mechanics.furniture` section, handed to the furniture factory untouched,
 * so it takes everything that block does in an item config.
 */
@Serializable
@SerialName("nexo:furniture")
@JvmInline
value class NexoFurniture(val mechanic: RawConfig = RawConfig()) {
    fun toItemSection(prefabKey: PrefabKey, material: Material?): ConfigurationSection =
        mechanic.toItemSection(prefabKey, material, "furniture")
}
