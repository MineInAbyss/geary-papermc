package com.mineinabyss.geary.papermc.tracking.items.systems

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.datastore.encode
import com.mineinabyss.geary.serialization.components.Persists
import com.mineinabyss.geary.systems.query.Query
import com.mineinabyss.idofront.items.editItemMeta
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration.Companion.seconds

fun Geary.createPeriodicSaveSystem() = system(
    object : Query(this) {
        val persisting by getRelationsWithData<Persists, Any>()
        val item by get<ItemStack>()
    }
).every(5.seconds).exec { q ->
//        val forceSave = every(iterations = 100)
//
//        if (forceSave) {
//            entity.encodeComponentsTo(item)
//            return
//        }

    q.item.editItemMeta {
        q.persisting.forEach {
            val newHash = it.targetData.hashCode()
            if (newHash != it.data.hash) {
                it.data.hash = newHash
                persistentDataContainer.encode(it.targetData)
            }
        }
    }
}
