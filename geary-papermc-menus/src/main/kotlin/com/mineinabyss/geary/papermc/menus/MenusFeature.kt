package com.mineinabyss.geary.papermc.menus

import com.mineinabyss.dependencies.module
import com.mineinabyss.guiy.canvas.guiy
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.features.requirePlugins

val MenusFeature = module("menus") {
    requirePlugins("Guiy")
}.mainCommand {
    "items" {
        permission = "geary.admin.items"
        executes.asPlayer { guiy(player) { ItemsMenu() } }
    }
}
