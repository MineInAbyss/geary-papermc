package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Item id Nexo knows a prefab by.
 *
 * Nexo ids are flat strings, so the `:` geary separates namespace and key with becomes `_`,
 * keeping ids unique across namespaces.
 */
internal fun nexoId(prefabKey: PrefabKey): String = "${prefabKey.namespace}_${prefabKey.key}"

/**
 * Builds the config section Nexo would have read out of an items file, with this config as the
 * item's `Mechanics.[mechanic]` block.
 *
 * [material] only matters for what a mechanic drops without a placed item to copy, geary's own item wins otherwise.
 */
internal fun RawConfig.toItemSection(prefabKey: PrefabKey, material: Material?, mechanic: String): ConfigurationSection {
    val section = YamlConfiguration().createSection(nexoId(prefabKey))
    section.set("material", (material ?: Material.PAPER).name)
    section.createSection("Mechanics").createSection(mechanic, values)
    return section
}
