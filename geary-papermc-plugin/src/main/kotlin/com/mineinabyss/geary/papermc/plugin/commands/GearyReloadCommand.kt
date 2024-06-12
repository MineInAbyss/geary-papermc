package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.brigadier.IdoRootCommand

private val prefabLoader get() = prefabs.loader

fun IdoRootCommand.reload() {
    "reload" {
        executes {
            gearyPaper.configHolder.reload()
            prefabLoader.loadOrUpdatePrefabs()
        }
    }
}
