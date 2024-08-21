package com.mineinabyss.geary.papermc.spawning.spawn_types

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class GearyReadSpawnCategoryEvent(
    val entity: BukkitEntity,
) : Event() {
    var category: SpawnCategory? = null

    companion object {
        @JvmStatic
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers() = HANDLER_LIST

}
