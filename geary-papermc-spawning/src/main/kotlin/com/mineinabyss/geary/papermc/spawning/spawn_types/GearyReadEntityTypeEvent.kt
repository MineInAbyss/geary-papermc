package com.mineinabyss.geary.papermc.spawning.spawn_types

import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GearyReadEntityTypeEvent(
    val entity: BukkitEntity,
) : Event() {
    var type: String? = null

    companion object {
        @JvmStatic
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers() = HANDLER_LIST

}
