package com.mineinabyss.geary.papermc.mythicmobs.items

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.features.items.food.ReplaceBurnedDrop
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.getAddon
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import io.lumine.mythic.api.adapters.AbstractItemStack
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.DropMetadata
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.adapters.item.ItemComponentBukkitItemStack
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import io.lumine.mythic.bukkit.utils.numbers.RandomDouble
import io.lumine.mythic.core.drops.droppables.VanillaItemDrop
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.jvm.optionals.getOrNull


class MythicMobDropListener : Listener {
    @EventHandler
    fun MythicDropLoadEvent.onMythicDropLoad() {
        if (dropName.lowercase() != "geary") return
        val lines = container.line.lowercase().split(" ")
        val prefabKey = PrefabKey.of(lines[1])
        val amount = lines.getOrNull(2)?.takeIf { "-" in it } ?: "1-1"
        val noLooting = "nolooting" in lines
        val world = gearyPaper.worldManager.global
        val itemStack = world.getAddon(ItemTracking).createItem(prefabKey) ?: return

        register(
            GearyDrop(
                world,
                prefabKey,
                noLooting,
                container.line,
                config,
                ItemComponentBukkitItemStack(itemStack),
                RandomDouble(amount)
            )
        )
    }
}

class GearyDrop(
    world: Geary,
    val prefab: PrefabKey,
    val noLooting: Boolean,
    line: String, config: MythicLineConfig, item: AbstractItemStack,
    val randomAmount: RandomDouble,
) : VanillaItemDrop(line, config, item, randomAmount) {
    val cookedItem = world.entityOfOrNull(prefab)?.get<ReplaceBurnedDrop>()?.replaceWith?.toItemStack()
    val cookedDrop = cookedItem?.let { VanillaItemDrop(line, config, ItemComponentBukkitItemStack(it), randomAmount) }

    override fun getDrop(metadata: DropMetadata?, amount: Double): AbstractItemStack {
        val isOnFire = (metadata?.dropper?.getOrNull()?.entity?.bukkitEntity?.fireTicks ?: 0) > 0
        val itemInKillerHand = (BukkitAdapter.adapt(metadata?.cause?.getOrNull()) as? Player)?.inventory?.itemInMainHand
        val lootingLvl = if (noLooting) 0 else itemInKillerHand?.enchantments?.get(Enchantment.LOOTING) ?: 0
        val amount = (randomAmount.min.toInt()..(randomAmount.max.toInt() + lootingLvl)).random().toDouble()
        return if (isOnFire && cookedDrop != null) cookedDrop.getDrop(metadata, amount)
        else super.getDrop(metadata, amount)
    }
}
