package com.mineinabyss.geary.papermc.menus

import com.mineinabyss.geary.prefabs.PrefabKey
import org.bukkit.inventory.ItemStack

sealed interface MenuEntry {
    val icon: ItemStack

    data class Category(
        val name: String,
        val path: List<String>,
        override val icon: ItemStack,
        val entries: List<MenuEntry>,
    ) : MenuEntry {
        fun subcategory(name: String): Category? = entries.firstOrNull { it is Category && it.name == name } as? Category
    }

    data class Item(
        val key: PrefabKey,
        override val icon: ItemStack,
        val name: String,
    ) : MenuEntry
}
