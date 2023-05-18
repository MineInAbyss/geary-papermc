package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.datatypes.GearyEntity
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
import org.bukkit.inventory.ItemStack
import java.util.*

class NMSItemCache: PlayerItemCache<NMSItemStack>(64) {
    override fun readItemInfo(item: NMSItemStack): ItemInfo {
        return LoginListener.readItemInfo(item)
    }

    override fun convertToItemStack(item: NMSItemStack): ItemStack {
        return item.toBukkit()
    }

    override fun deserializeItem(item: NMSItemStack): GearyEntity? {
        return itemTracking.provider.deserializeItemStackToEntity(item)
    }

    override fun skipUpdate(slot: Int, newItem: NMSItemStack?): Boolean {
        return getCachedItem(slot) === newItem && !(get(slot) != null && newItem?.isEmpty == true)
    }

}
class LoginListener : Listener {
    @EventHandler
    fun PlayerJoinEvent.track() {
        val entity = player.toGeary()
        entity.set<PlayerItemCache<*>>(NMSItemCache())
    }

    companion object {
        fun readItemInfo(item: NMSItemStack): ItemInfo {
            val pdc = item.fastPDC ?: return ItemInfo.NothingEncoded
            if (item.item == Items.AIR) return ItemInfo.NothingEncoded

            if (!pdc.hasComponentsEncoded) return ItemInfo.NothingEncoded

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
}
