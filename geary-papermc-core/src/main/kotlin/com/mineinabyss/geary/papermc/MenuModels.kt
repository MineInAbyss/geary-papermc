package com.mineinabyss.geary.papermc

import net.kyori.adventure.key.Key

object MenuModels {
    val SCROLL_UP: Key = Key.key("geary", "menu/scroll_up")
    val SCROLL_DOWN: Key = Key.key("geary", "menu/scroll_down")

    /** Conditional on custom model data flag 0, true shows the grouped icon */
    val GROUP_BY_FOLDERS: Key = Key.key("geary", "menu/group_by_folders")
}
