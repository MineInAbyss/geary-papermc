package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.plugin.PaperEngineModule
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.di.DI

private val prefabLoader get() = prefabs.loader

fun Command.reload() {
    "reload" {
        action {
            gearyPaper.configHolder.reload()
            DI.get<PaperEngineModule>().updateToMatch(gearyPaper.config)
            prefabLoader.loadOrUpdatePrefabs()
        }
    }
}
