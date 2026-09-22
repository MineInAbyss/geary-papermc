package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.papermc.gearyPaper
import com.nexomc.nexo.api.events.NexoPreItemsLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Holds Nexo's item-load until every prefab is read, and hands it the prefab-declared items again on a reload.
 *
 * Prefabs load a tick after geary enables and take far longer than the tick Nexo waits before loading
 * its own items, so without the hold the prefab-declared items register after Nexo generated its pack and
 * fired its loaded-event, leaving them out of the custom-block blockstates and out of Axiom until a
 * manual reload
 */
class NexoItemLoadListener : Listener {
    @EventHandler
    fun NexoPreItemsLoadEvent.onPreItemsLoad() {
        val loaded = PrefabLoading.awaitLoaded()
        // While prefabs are still being read the observers register each item as it comes in
        if (!loaded.isDone) return deferUntil(loaded)
        gearyPaper.forEachWorld { registerAllNexoItems() }
    }
}
