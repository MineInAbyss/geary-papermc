package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.GearySetup
import org.bukkit.World

// TODO per world engine support in the future
class WorldManager {
    private var _globalEngine: Geary? = null
    private val _initSteps = mutableListOf<GearySetup.() -> Unit>()
    val initSteps: List<GearySetup.() -> Unit> get() = _initSteps

    fun configure(run: GearySetup.() -> Unit) {
        _initSteps.add(run)
    }

    fun getGearyWorld(world: World): Geary? = _globalEngine

    fun setGlobalEngine(engine: Geary) {
        _globalEngine = engine
    }

    val global get() = _globalEngine ?: error("No global Geary engine set")
}

fun World.toGeary() = gearyPaper.worldManager.getGearyWorld(this) ?: error("No Geary engine found for world $name")
