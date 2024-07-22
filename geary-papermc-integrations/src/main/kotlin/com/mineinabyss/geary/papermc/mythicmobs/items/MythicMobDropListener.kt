package com.mineinabyss.geary.papermc.mythicmobs.items

import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.papermc.tracking.items.helpers.GearyItemPrefabQuery.Companion.getKeys
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.idofront.messaging.logError
import io.lumine.mythic.bukkit.adapters.BukkitItemStack
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import io.lumine.mythic.bukkit.utils.numbers.RandomDouble
import io.lumine.mythic.core.drops.DropMetadataImpl
import io.lumine.mythic.core.drops.droppables.ItemDrop
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
