package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.idofront.nms.nbt.fastPDC
import org.bukkit.inventory.ItemStack
import java.io.Closeable

class GearyItemContext : Closeable {
    val cached = mutableMapOf<ItemStack, GearyEntity>()
    fun ItemStack.toGeary(): GearyEntity {
        return cached.getOrPut(this) {
            gearyItems.itemProvider.deserializeItemStackToEntity(this.fastPDC) ?: entity()
        }
    }

    override fun close() {
        cached.forEach { item, entity -> entity.removeEntity() }
    }
}


inline fun <T> itemEntityContext(run: GearyItemContext.() -> T): T {
    return GearyItemContext().use(run)
}
