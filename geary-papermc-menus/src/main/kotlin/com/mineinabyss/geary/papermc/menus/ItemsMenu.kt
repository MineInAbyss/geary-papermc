package com.mineinabyss.geary.papermc.menus

import androidx.compose.runtime.Composable
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.toGeary
import com.mineinabyss.guiy.canvas.CurrentPlayer
import com.mineinabyss.guiy.navigation.NavHost
import com.mineinabyss.guiy.navigation.composable
import com.mineinabyss.guiy.navigation.rememberNavController
import com.mineinabyss.guiy.viewmodel.viewModel

sealed interface ItemsScreen {
    data object Main : ItemsScreen
    data class Section(val section: MenuSection, val path: List<String>) : ItemsScreen
}

@Composable
fun ItemsMenu() {
    val player = CurrentPlayer
    val nav = rememberNavController()
    val config = gearyPaper.config.menus
    val viewModel = viewModel { ItemsMenuViewModel(player.world.toGeary()) }

    NavHost(nav, startDestination = ItemsScreen.Main) {
        composable<ItemsScreen.Main> {
            MainMenuScreen(config.mainTitle, onOpenSection = { nav.navigate(ItemsScreen.Section(it, emptyList())) })
        }
        composable<ItemsScreen.Section> { screen ->
            val category = viewModel.categoryAt(screen.section, screen.path) ?: viewModel.root(screen.section)
            EntryGridScreen(
                config = config,
                entries = category.entries,
                groupByFolders = viewModel.groupByFolders,
                query = viewModel.query,
                onToggleGrouping = { viewModel.groupByFolders = !viewModel.groupByFolders },
                onOpenCategory = { nav.navigate(ItemsScreen.Section(screen.section, it.path)) },
                // The dialog draws over the chest and hands it back when closed, so nothing here reopens the menu
                onSearch = { player.showDialog(searchDialog(config.searchTitle, viewModel.query) { viewModel.query = it }) },
                onClearSearch = { viewModel.query = "" },
            )
        }
    }
}
