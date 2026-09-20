package com.mineinabyss.geary.papermc.menus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.guiy.viewmodel.GuiyViewModel
import org.bukkit.inventory.ItemStack

class ItemsMenuViewModel(world: Geary) : GuiyViewModel() {
    var groupByFolders by mutableStateOf(false)
    var query by mutableStateOf("")

    private val items = loadMenuItems(world).groupBy { it.section }
    private val trees = mutableMapOf<Triple<MenuSection, Boolean, String>, MenuEntry.Category>()

    fun root(section: MenuSection): MenuEntry.Category = trees.getOrPut(Triple(section, groupByFolders, query)) {
        MenuEntry.Category(section.title, emptyList(), ItemStack.empty(), sectionEntries(items[section].orEmpty(), groupByFolders, query))
    }

    fun categoryAt(section: MenuSection, path: List<String>): MenuEntry.Category? =
        path.fold(root(section) as MenuEntry.Category?) { category, name -> category?.subcategory(name) }
}
