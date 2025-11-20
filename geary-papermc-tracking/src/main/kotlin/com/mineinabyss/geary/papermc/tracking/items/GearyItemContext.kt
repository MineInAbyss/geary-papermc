package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.getAddon
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
            var entity: GearyEntity? = null
            editPersistentDataContainer {
                entity = gearyItems.itemProvider.deserializeItemStackToEntity(it)?.apply {
                    set<ItemStack>(this@toGearyOrNull)
                }
            }
            return entity
        }
    }

    override fun close() {
        cached.forEach { (_, entity) -> entity.removeEntity() }
    }
}

inline fun <T> Geary.itemEntityContext(run: GearyItemContext.() -> T): T {
    return GearyItemContext(this).use(run)
}
