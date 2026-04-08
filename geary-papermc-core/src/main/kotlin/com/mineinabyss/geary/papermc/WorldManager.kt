package com.mineinabyss.geary.papermc

import com.mineinabyss.dependencies.DIScope
import com.mineinabyss.geary.modules.Geary
import org.bukkit.World

// TODO per world engine support in the future
class WorldManager {
    private var _globalEngine: Geary? = null
    private val _initSteps = mutableListOf<DIScope.() -> Unit>()
    val initSteps: List<DIScope.() -> Unit> get() = _initSteps

    fun configure(run: DIScope.() -> Unit) {
        _initSteps.add(run)
    }

    fun getGearyWorld(world: World): Geary? = _globalEngine

    fun setGlobalEngine(engine: Geary) {
        _globalEngine = engine
    }

    @Deprecated("Geary may switch to having multiple world instances in the future", replaceWith = ReplaceWith("getGearyWorld()"))
    val global get() = _globalEngine ?: error("No global Geary engine set")
}

fun World.toGeary() = gearyPaper.worldManager.getGearyWorld(this) ?: error("No Geary engine found for world $name")
