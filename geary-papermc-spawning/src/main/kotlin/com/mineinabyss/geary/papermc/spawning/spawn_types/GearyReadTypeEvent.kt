package com.mineinabyss.geary.papermc.spawning.spawn_types

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GearyReadTypeEvent(
    val name: String,
) : Event() {
    var spawnType: SpawnType? = null

    fun register(type: SpawnType) {
        spawnType = type
    }

    companion object {
        @JvmStatic
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers() = HANDLER_LIST

}
