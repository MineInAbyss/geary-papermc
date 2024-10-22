package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.World

// TODO per world engine support in the future
class WorldManager {
    private var _globalEngine: Geary? = null

    fun getGearyWorld(world: World): Geary? = _globalEngine

    fun setGlobalEngine(engine: Geary) {
        _globalEngine = engine
    }

    val global get() = _globalEngine ?: error("No global Geary engine set")
}

fun World.toGeary() = gearyPaper.worldManager.getGearyWorld(this) ?: error("No Geary engine found for world $name")
