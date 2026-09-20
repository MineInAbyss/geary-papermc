package com.mineinabyss.geary.papermc.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.mineinabyss.geary.papermc.MenuModels
import com.mineinabyss.geary.papermc.MenusConfig
import com.mineinabyss.geary.papermc.ScrollMode
import com.mineinabyss.guiy.components.Item
import com.mineinabyss.guiy.components.VerticalGrid
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.components.lists.NavbarPosition
import com.mineinabyss.guiy.components.lists.ScrollDirection
import com.mineinabyss.guiy.components.lists.Scrollable
import com.mineinabyss.guiy.components.lists.ScrollableState
import com.mineinabyss.guiy.modifiers.click.clickable
import com.mineinabyss.guiy.modifiers.click.onClickEvent
import com.mineinabyss.guiy.navigation.LocalBackGestureDispatcher
import com.mineinabyss.idofront.textcomponents.miniMsg
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import me.dvyy.compose.mini.layout.jetpack.Box
import me.dvyy.compose.mini.layout.modifiers.fillMaxHeight
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.layout.modifiers.size
import me.dvyy.compose.mini.layout.modifiers.width
import me.dvyy.compose.mini.modifier.Modifier
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

private const val GRID_WIDTH = 8
private const val GRID_HEIGHT = 6

// Rows of the right column, search and back sit over icons painted into the title texture
private const val ROW_SEARCH = 0
private const val ROW_GROUPING = 1
private const val ROW_UP = 2
private const val ROW_DOWN = 3
private const val ROW_BACK = 5

@Composable
fun EntryGridScreen(
    config: MenusConfig,
    entries: List<MenuEntry>,
    groupByFolders: Boolean,
    query: String,
    onToggleGrouping: () -> Unit,
    onOpenCategory: (MenuEntry.Category) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
) {
    val direction = if (config.scrollMode == ScrollMode.PAGINATED) ScrollDirection.PAGINATED else ScrollDirection.VERTICAL
    // The nav host reuses this composition for every category, so the list is the key that lands a new one on the first row
    val state = remember(direction, entries) { ScrollableState(direction) }
    Chest(config.gridTitle, Modifier.height(GRID_HEIGHT.dp), onClose = { exit() }) {
        Scrollable(
            entries,
            state = state,
            navbarPosition = NavbarPosition.END,
            scrollbar = { Sidebar(state, groupByFolders, query, onToggleGrouping, onSearch, onClearSearch) },
        ) { page ->
            VerticalGrid(Modifier.size(GRID_WIDTH.dp, GRID_HEIGHT.dp)) {
                page.forEach { entry ->
                    when (entry) {
                        is MenuEntry.Category -> Item(entry.icon, Modifier.clickable { onOpenCategory(entry) })
                        is MenuEntry.Item -> Item(entry.icon, Modifier.giveOnClick(entry.icon))
                    }
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    state: ScrollableState,
    groupByFolders: Boolean,
    query: String,
    onToggleGrouping: () -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
) {
    val back = LocalBackGestureDispatcher.current
    // In VERTICAL mode a page is one row and pageMax counts rows, so the last page still on screen is pageMax - pagesOnScreen
    val canScrollDown = state.page < state.pageMax - state.pagesOnScreen
    val grouping = remember(groupByFolders) {
        modelItem(MenuModels.GROUP_BY_FOLDERS, "<white><b>Group by Folders: ${if (groupByFolders) "<green>On" else "<red>Off"}").apply {
            setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFlag(groupByFolders))
        }
    }
    val up = remember { modelItem(MenuModels.SCROLL_UP, "<white><b>Scroll up") }
    val down = remember { modelItem(MenuModels.SCROLL_DOWN, "<white><b>Scroll down") }

    Box(Modifier.width(1.dp).fillMaxHeight()) {
        if (query.isEmpty()) Text("<white><b>Search", modifier = Modifier.offset(0.dp, ROW_SEARCH.dp).clickable { onSearch() })
        else Text(
            "<white><b>Search: <yellow>$query", "<gray>Right click to clear",
            modifier = Modifier.offset(0.dp, ROW_SEARCH.dp).clickable { if (clickType.isRightClick) onClearSearch() else onSearch() },
        )
        Item(grouping, Modifier.offset(0.dp, ROW_GROUPING.dp).clickable { onToggleGrouping() })
        if (state.page > 0) Item(up, Modifier.offset(0.dp, ROW_UP.dp).clickable { state.previousPage() })
        if (canScrollDown) Item(down, Modifier.offset(0.dp, ROW_DOWN.dp).clickable { state.nextPage() })
        Text("<white><b>Back", modifier = Modifier.offset(0.dp, ROW_BACK.dp).clickable { back.onBack() })
    }
}

private fun modelItem(model: Key, name: String) = ItemStack.of(Material.PAPER).apply {
    setData(DataComponentTypes.ITEM_MODEL, model)
    setData(DataComponentTypes.ITEM_NAME, name.miniMsg())
}

// Like guiy's CreativeItem but without the creative mode check, the command permission gates access instead.
// The client turns the second of two fast clicks into a double click, so that counts as another left click
private fun Modifier.giveOnClick(itemStack: ItemStack) = onClickEvent onClick@{
    isConsumed = true
    val cursor = cursor?.takeUnless { it.isEmpty }
    val sameItem = cursor == null || cursor.isSimilar(itemStack)
    val result = when {
        clickType == ClickType.SHIFT_LEFT -> if (sameItem) itemStack.asQuantity(itemStack.maxStackSize) else null
        clickType.isShiftClick || clickType == ClickType.MIDDLE -> return@onClick
        clickType.isRightClick -> cursor?.clone()?.subtract() ?: return@onClick
        !sameItem -> null
        cursor == null -> itemStack.asOne()
        else -> cursor.clone().add()
    }
    whoClicked.setItemOnCursor(result)
}
