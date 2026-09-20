package com.mineinabyss.geary.papermc.menus

/** [slot] is the column of the button painted on the middle row of the title texture */
enum class MenuSection(val title: String, val slot: Int) {
    BLOCKS("Blocks", 2),
    ITEMS("Items", 4),
    FURNITURE("Furniture", 6),
}
