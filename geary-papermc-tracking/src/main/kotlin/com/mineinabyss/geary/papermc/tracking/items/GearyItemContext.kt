package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.nms.nbt.fastPDC
import org.bukkit.inventory.ItemStack
import java.io.Closeable

class GearyItemContext(
    world: Geary,
) : Closeable, Geary by world {
    val gearyItems = world.getAddon(ItemTracking)
    val cached = mutableMapOf<ItemStack, GearyEntity>()
    fun ItemStack.toGeary(): GearyEntity {
        return cached.getOrPut(this) {
            toGearyOrNull() ?: entity()
        }
    }

    fun ItemStack.toGearyOrNull(): GearyEntity? {
        return cached.getOrPut(this) {
            gearyItems.itemProvider.deserializeItemStackToEntity(this.fastPDC)?.apply {
                set<ItemStack>(this@toGearyOrNull)
            } ?: return null
        }
    }

    override fun close() {
        cached.forEach { (item, entity) -> entity.removeEntity() }
    }
}

inline fun <T> Geary.itemEntityContext(run: GearyItemContext.() -> T): T {
    return GearyItemContext(this).use(run)
}
