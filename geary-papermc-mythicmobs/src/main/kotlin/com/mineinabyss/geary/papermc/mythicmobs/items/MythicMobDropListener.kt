package com.mineinabyss.geary.papermc.mythicmobs.items

import com.mineinabyss.geary.papermc.features.items.food.ReplaceBurnedDrop
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import io.lumine.mythic.api.adapters.AbstractItemStack
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.DropMetadata
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import io.lumine.mythic.bukkit.utils.numbers.RandomDouble
import io.lumine.mythic.core.drops.droppables.VanillaItemDrop
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.jvm.optionals.getOrNull


class MythicMobDropListener : Listener {
    @EventHandler
    fun MythicDropLoadEvent.onMythicDropLoad() {
        if (dropName.lowercase() != "geary") return
        val lines = container.line.split(" ")
        val prefabKey = PrefabKey.of(lines[1])
        val amount = lines.getOrNull(2)?.takeIf { "-" in it } ?: "1-1"
        val itemStack = gearyItems.createItem(prefabKey) ?: return

        register(
            GearyDrop(
                prefabKey,
                container.line,
                config,
                ItemComponentBukkitItemStack(itemStack),
                RandomDouble(amount)
            )
        )
    }
}

class GearyDrop(
    val prefab: PrefabKey,
    line: String, config: MythicLineConfig, item: AbstractItemStack, amount: RandomDouble,
) : VanillaItemDrop(line, config, item, amount) {
    //    val item = gearyItems.createItem(prefab)
    val cookedItem = prefab.toEntityOrNull()?.get<ReplaceBurnedDrop>()?.replaceWith?.toItemStack()
    val cookedDrop = cookedItem?.let { VanillaItemDrop(line, config, ItemComponentBukkitItemStack(it), amount) }
    override fun getDrop(metadata: DropMetadata?, amount: Double): AbstractItemStack {
        val isOnFire = (metadata?.dropper?.getOrNull()?.entity?.bukkitEntity?.fireTicks ?: 0) > 0
        return if (isOnFire && cookedDrop != null) cookedDrop.getDrop(metadata, amount)
        else super.getDrop(metadata, amount)
    }
}
