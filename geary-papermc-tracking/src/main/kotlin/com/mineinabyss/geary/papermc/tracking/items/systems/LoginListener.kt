package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.papermc.datastore.decode
import com.mineinabyss.geary.papermc.datastore.decodePrefabs
import com.mineinabyss.geary.papermc.datastore.hasComponentsEncoded
import com.mineinabyss.geary.papermc.datastore.remove
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.items.cache.ItemInfo
import com.mineinabyss.geary.papermc.tracking.items.cache.PlayerItemCache
import com.mineinabyss.geary.papermc.tracking.items.components.PlayerInstancedItem
import com.mineinabyss.geary.papermc.tracking.items.itemTracking
import com.mineinabyss.idofront.nms.aliases.NMSItemStack
import com.mineinabyss.idofront.nms.aliases.toBukkit
import com.mineinabyss.idofront.nms.nbt.fastPDC
import net.minecraft.world.item.Items
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.*

class LoginListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.track() {
        val entity = player.toGeary()
        entity.set(PlayerItemCache(
            64,
            readItemInfo = ::readItemInfo,
            deserializeItem = { itemTracking.provider.deserializeItemStackToEntity(it) },
            cacheConverter = { it.toBukkit() }
        ))
    }

    fun readItemInfo(item: NMSItemStack): ItemInfo {
        val pdc = item.fastPDC ?: return ItemInfo.NothingEncoded
        if (item.item == Items.AIR) return ItemInfo.NothingEncoded

        //TODO move out of here
        val didMigration = !itemTracking.migration.encodePrefabsFromCustomModelDataIfPresent(pdc, item)

        if (!pdc.hasComponentsEncoded && !didMigration) return ItemInfo.NothingEncoded

        val prefabKeys = pdc.decodePrefabs()
        val prefabs = prefabKeys.map { it.toEntityOrNull() ?: return ItemInfo.ErrorDecoding }.toSet()

        if (prefabs.any { it.has<PlayerInstancedItem>() }) {
            pdc.remove<UUID>() // in case of migration
            return ItemInfo.PlayerInstanced(prefabKeys)
        }

        val uuid = pdc.decode<UUID>()
        return ItemInfo.EntityEncoded(uuid)
    }
}
