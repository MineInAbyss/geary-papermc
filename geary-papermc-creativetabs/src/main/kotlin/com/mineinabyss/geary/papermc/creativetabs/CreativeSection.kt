package com.mineinabyss.geary.papermc.creativetabs

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.nexo.NexoCustomBlock
import com.mineinabyss.geary.papermc.nexo.NexoFurniture
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.helpers.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.entityOfOrNull
import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

class CreativeEntry(val key: PrefabKey, val item: ItemStack, val category: String, val vanillaTab: String)

class CreativeSection(val id: String, val title: Component, val entries: List<CreativeEntry>)

fun buildCreativeSections(world: Geary): List<CreativeSection> = with(world) {
    val tracking = getAddon(ItemTracking)
    val prefabs = tracking.prefabs.getKeys()
        .sortedWith(compareBy({ it.namespace }, { it.key }))
        .mapNotNull { key -> entityOfOrNull(key)?.let { key to it } }

    fun section(id: String, title: String, defaultVanillaTab: String, filter: GearyEntity.() -> Boolean) = CreativeSection(
        id,
        Component.text(title),
        prefabs.filter { (_, entity) -> entity.filter() }.mapNotNull { (key, entity) ->
            val item = tracking.createItem(key) ?: return@mapNotNull null
            val placement = entity.get<CreativeTabPlacement>()
            val folder = placement?.category ?: entity.get<Prefab>()?.file?.parent?.name?.toTitle() ?: "Other"
            CreativeEntry(key, item, folder, placement?.vanillaTab ?: defaultVanillaTab)
        },
    )

    listOf(
        section("blocks", "Blocks", "minecraft:building_blocks") { has<NexoCustomBlock>() },
        section("furniture", "Furniture", "minecraft:functional_blocks") { has<NexoFurniture>() },
        section("items", "Items", "minecraft:ingredients") { !has<NexoCustomBlock>() && !has<NexoFurniture>() },
    ).filter { it.entries.isNotEmpty() }
}

private fun String.toTitle() = split('_', '-', ' ').filter { it.isNotEmpty() }.joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
