package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.prefabs.PrefabKey
import net.kyori.adventure.key.Key
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
 *
 * [itemModel] has to be mirrored onto Nexo's copy because placed furniture and custom blocks render from
 * the item Nexo built, not from the one geary hands the player, so without it they show a bare material
 */
internal fun RawConfig.toItemSection(
    prefabKey: PrefabKey,
    material: Material?,
    itemModel: Key?,
    mechanic: String,
): ConfigurationSection {
    val section = YamlConfiguration().createSection(nexoId(prefabKey))
    section.set("material", (material ?: Material.PAPER).name)
    itemModel?.let { section.createSection("Components").set("item_model", it.asString()) }
    section.createSection("Mechanics").createSection(mechanic, values)
    return section
}
