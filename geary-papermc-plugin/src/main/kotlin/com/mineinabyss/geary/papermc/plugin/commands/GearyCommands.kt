package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.geary.papermc.plugin.GearyPluginImpl
import com.mineinabyss.geary.papermc.plugin.commands.mobs.mobs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.commands.brigadier.commands

internal fun GearyPluginImpl.registerGearyCommands() = commands {
    "geary" {
        items()
        mobs()
        prefabs()
        reload(this@registerGearyCommands)
        debug()
    }
}

internal fun Collection<PrefabKey>.filterPrefabs(arg: String): List<String> =
    filter { it.key.startsWith(arg) || it.full.startsWith(arg) }.map { it.toString() }.take(20)
