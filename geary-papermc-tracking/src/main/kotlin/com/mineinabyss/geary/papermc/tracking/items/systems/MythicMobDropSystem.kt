package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery.Companion.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import io.lumine.mythic.bukkit.adapters.BukkitItemStack
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import io.lumine.mythic.core.drops.droppables.ItemDrop
import io.lumine.mythic.core.drops.droppables.VanillaItemDrop
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener


class MythicMobDropSystem : Listener {

    @EventHandler
    fun MythicDropLoadEvent.onMythicDropLoad() {
        if (dropName.lowercase() != "geary") return
        val prefabKey = PrefabKey.of(container.line.split(" ")[1])
        if (prefabKey !in gearyItems.prefabs.getKeys()) return

        register(VanillaItemDrop(container.line, config, BukkitItemStack(gearyItems.createItem(prefabKey))))
    }
}
