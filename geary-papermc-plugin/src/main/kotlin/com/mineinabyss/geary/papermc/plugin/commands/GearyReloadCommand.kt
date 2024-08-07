package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackGenerator
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.idofront.commands.Command

private val prefabLoader get() = prefabs.loader

fun Command.reload() {
    "reload" {
        action {
            gearyPaper.configHolder.reload()
            prefabLoader.loadOrUpdatePrefabs()
            ResourcePackGenerator().generateResourcePack()
        }
    }
}
