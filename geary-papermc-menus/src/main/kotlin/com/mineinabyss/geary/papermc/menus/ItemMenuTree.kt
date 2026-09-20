package com.mineinabyss.geary.papermc.menus

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.nexo.NexoCustomBlock
import com.mineinabyss.geary.papermc.nexo.NexoFurniture
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.papermc.tracking.items.helpers.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.textcomponents.miniMsg
import com.mineinabyss.idofront.textcomponents.toPlainText
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import kotlinx.io.files.Path
import kotlinx.io.files.SystemPathSeparator
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class LoadedItem(val key: PrefabKey, val file: Path?, val section: MenuSection, val entry: MenuEntry.Item)

// Prefabs without an item, like directional block children, have nothing to show
fun loadMenuItems(world: Geary): List<LoadedItem> = with(world) {
    val tracking = getAddon(ItemTracking)
    tracking.prefabs.getKeys().mapNotNull { key ->
        val stack = tracking.createItem(key) ?: return@mapNotNull null
        val entity = entityOfOrNull(key)
        val section = when {
            entity?.has<NexoCustomBlock>() == true -> MenuSection.BLOCKS
            entity?.has<NexoFurniture>() == true -> MenuSection.FURNITURE
            else -> MenuSection.ITEMS
        }
        LoadedItem(key, entity?.get<Prefab>()?.file, section, MenuEntry.Item(key, stack, stack.effectiveName().toPlainText()))
    }
}

// When grouped the query also keeps everything under a folder whose name matches
fun sectionEntries(items: List<LoadedItem>, groupByFolders: Boolean, query: String): List<MenuEntry> {
    fun LoadedItem.matches() = query.isEmpty() || key.full.contains(query, true) || entry.name.contains(query, true)

    if (!groupByFolders) return items.filter { it.matches() }
        .sortedWith(compareBy({ it.key.namespace }, { it.key.key })).map { it.entry }

    val byFile = items.sortedWith(compareBy<LoadedItem, String?>(nullsLast()) { it.file?.toString() }.thenBy { it.key.full })
    return byFile.groupBy { it.key.namespace }.toSortedMap().mapNotNull { (namespace, inside) ->
        val path = listOf(namespace)
        val root = commonRoot(inside)
        val located = inside.map { it.dirSegments().drop(root.size) to it }
            .filter { (dir, item) -> item.matches() || dir.any { it.contains(query, true) } }
            .map { (dir, item) -> dir to item.entry }
        if (located.isEmpty()) null else category(namespace, path, nestByFolder(located, path))
    }
}

private fun LoadedItem.dirSegments(): List<String> =
    file?.parent?.toString()?.split(SystemPathSeparator) ?: emptyList()

// Files under one namespace can be loaded from any root, so the root is taken as their common ancestor
private fun commonRoot(items: List<LoadedItem>): List<String> = items
    .filter { it.file != null }
    .map { it.dirSegments() }
    .reduceOrNull { acc, other -> acc.zip(other).takeWhile { (a, b) -> a == b }.map { it.first } }
    ?: emptyList()

private fun nestByFolder(items: List<Pair<List<String>, MenuEntry.Item>>, path: List<String>): List<MenuEntry> {
    val (here, deeper) = items.partition { (dir, _) -> dir.isEmpty() }
    val folders = deeper.groupBy { (dir, _) -> dir.first() }.toSortedMap().map { (folder, inside) ->
        val folderPath = path + folder
        category(folder, folderPath, nestByFolder(inside.map { (dir, item) -> dir.drop(1) to item }, folderPath))
    }
    return folders + here.map { (_, item) -> item }
}

private fun category(name: String, path: List<String>, entries: List<MenuEntry>): MenuEntry.Category {
    val items = entries.allItems()
    val first = entries.firstOrNull { it is MenuEntry.Item } ?: items.firstOrNull()
    val icon = (first?.icon?.clone() ?: ItemStack.of(Material.CHEST)).apply {
        setData(DataComponentTypes.ITEM_NAME, "<white>$name".miniMsg())
        setData(DataComponentTypes.LORE, ItemLore.lore(listOf("<gray>${items.size} items".miniMsg())))
    }
    return MenuEntry.Category(name, path, icon, entries)
}

private fun List<MenuEntry>.allItems(): List<MenuEntry.Item> = flatMap {
    when (it) {
        is MenuEntry.Category -> it.entries.allItems()
        is MenuEntry.Item -> listOf(it)
    }
}
