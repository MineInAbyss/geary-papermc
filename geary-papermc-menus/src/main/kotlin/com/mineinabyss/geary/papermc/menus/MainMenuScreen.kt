package com.mineinabyss.geary.papermc.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mineinabyss.guiy.components.canvases.Chest
import com.mineinabyss.guiy.components.items.Text
import com.mineinabyss.guiy.modifiers.click.clickable
import me.dvyy.compose.mini.layout.modifiers.height
import me.dvyy.compose.mini.layout.modifiers.offset
import me.dvyy.compose.mini.modifier.Modifier

/** The buttons are painted into the title texture, invisible items over them carry the click and tooltip */
@Composable
fun MainMenuScreen(title: String, onOpenSection: (MenuSection) -> Unit) {
    Chest(title, Modifier.height(3.dp), onClose = { exit() }) {
        MenuSection.entries.forEach { section ->
            Text(
                "<white><b>${section.title}",
                modifier = Modifier.offset(section.slot.dp, 1.dp).clickable { onOpenSection(section) },
            )
        }
    }
}
