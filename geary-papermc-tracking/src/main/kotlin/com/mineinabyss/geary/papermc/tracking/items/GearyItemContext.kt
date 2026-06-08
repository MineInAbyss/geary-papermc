package com.mineinabyss.geary.papermc.tracking.items

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.WorldScoped
import org.bukkit.inventory.ItemStack
import java.io.Closeable

class GearyItemContext(
    world: WorldScoped,
) : Closeable, WorldScoped by world.newScope() {
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
            entity ?: return null
        }
    }

    override fun close() {
        cached.forEach { (_, entity) -> entity.removeEntity() }
        super.close()
    }
}

/**
 * Creates a temporary scope to decode [ItemStack]s to [GearyEntity] instances.
 * Can be used to read or modify component data from items without going through PersistentDataContainer.
 *
 * Any created item entities in this scope will be removed when the scope closes.
 */
inline fun <T> WorldScoped.itemEntityContext(run: GearyItemContext.() -> T): T {
    return GearyItemContext(this).use(run)
}
