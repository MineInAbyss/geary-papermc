package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.papermc.PrefabLoading
import com.nexomc.nexo.api.events.NexoPreItemsLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Holds Nexo's item-load until every prefab is read.
 *
 * Prefabs load a tick after geary enables and take far longer than the tick Nexo waits before loading
 * its own items, so without this the prefab-declared items register after Nexo generated its pack and
 * fired its loaded-event, leaving them out of the custom-block blockstates and out of Axiom until a
 * manual reload.
 */
class NexoItemLoadListener : Listener {
    @EventHandler
    fun NexoPreItemsLoadEvent.onPreItemsLoad() {
        deferUntil(PrefabLoading.awaitLoaded())
    }
}
