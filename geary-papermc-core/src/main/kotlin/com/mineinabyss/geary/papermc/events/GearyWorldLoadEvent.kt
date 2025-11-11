package com.mineinabyss.geary.papermc.events

import com.mineinabyss.geary.modules.GearySetup
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class GearyWorldLoadEvent(
    val setup: GearySetup,
) : Event() {
    fun configure(run: GearySetup.() -> Unit) {
        run.invoke(setup)
    }

    companion object {
        @JvmStatic
        private val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLER_LIST
    }

    override fun getHandlers() = HANDLER_LIST
}
