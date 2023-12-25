package com.mineinabyss.geary.papermc.tracking.entities.events

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList


/**
 * Called when a bukkit entity is added to the world and registered with Geary.
 *
 * Will avoid multiple calls like the Paper event currently does
 */
class GearyEntityAddToWorldEvent(
    val gearyEntity: GearyEntity,
    val entity: BukkitEntity,
) : Event() {
    companion object {
        @JvmStatic
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers() = HANDLER_LIST

}
