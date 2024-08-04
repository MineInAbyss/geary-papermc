package com.mineinabyss.geary.papermc.mythicmobs.items

import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import io.lumine.mythic.bukkit.utils.numbers.RandomDouble
import io.lumine.mythic.core.drops.droppables.VanillaItemDrop
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener


class MythicMobDropListener : Listener {
    @EventHandler
    fun MythicDropLoadEvent.onMythicDropLoad() {
        if (dropName.lowercase() != "geary") return
        val lines = container.line.split(" ")
        val prefabKey = PrefabKey.of(lines[1])
        val amount = lines.getOrNull(2)?.takeIf { "-" in it } ?: "1-1"
        val itemStack = gearyItems.createItem(prefabKey) ?: return

        register(VanillaItemDrop(container.line, config, ItemComponentBukkitItemStack(itemStack), RandomDouble(amount)))
    }
}
