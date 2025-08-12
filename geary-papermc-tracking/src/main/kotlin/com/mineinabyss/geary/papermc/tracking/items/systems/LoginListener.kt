package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.nbt.fastPDC
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import java.util.*

class LoginListener(
    world: Geary,
    val cacheImpl: () -> PlayerItemCache<*>,
) : Listener, Geary by world {
    @EventHandler
    fun PlayerJoinEvent.track() {
        val entity = player.toGeary()
        entity.set<PlayerItemCache<*>>(cacheImpl())
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun PlayerQuitEvent.clearCacheOnPlayerQuit() {
        val cache = player.toGearyOrNull()?.get<PlayerItemCache<*>>() ?: return
        cache.clear()
    }

    companion object {
        context(world: Geary)
        fun readItemInfo(item: NMSItemStack): ItemInfo {
            if (item.isEmpty) return ItemInfo.NothingEncoded
            val pdc = item.fastPDC ?: return ItemInfo.NothingEncoded
            return readItemInfo(pdc)
        }

        context(world: Geary)
        fun readItemInfo(item: ItemStack): ItemInfo {
            if (item.isEmpty) return ItemInfo.NothingEncoded
            val pdc = item.persistentDataContainer
            return readItemInfo(pdc)
        }

        context(world: Geary)
        fun readItemInfo(pdc: PersistentDataContainerView): ItemInfo {
            if (!pdc.hasComponentsEncoded) return ItemInfo.NothingEncoded

            val prefabKeys = pdc.decodePrefabs()
            val prefabs = prefabKeys.map { world.entityOfOrNull(it) ?: return ItemInfo.ErrorDecoding }.toSet()


            val uuid = pdc.decode<UUID>()
            return ItemInfo.EntityEncoded(uuid)
        }
    }
}
