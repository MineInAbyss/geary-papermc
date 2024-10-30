package com.mineinabyss.geary.papermc.features.items.food

import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

@JvmInline
@Serializable
@SerialName("geary:replace_burned_drop")
value class ReplaceBurnedDrop(
    val replaceWith: SerializableItemStack
)

class ReplaceBurnedDropListener : Listener {
    @EventHandler
    fun EntityDeathEvent.replaceBurnedDrops() = entity.withGeary {
        if (entity.fireTicks == 0) return

        itemEntityContext {
            val newDrops = mutableListOf<ItemStack>()
            drops.removeIf { drop ->
                val replace = drop.toGeary().get<ReplaceBurnedDrop>()
                replace?.let { newDrops += it.replaceWith.toItemStack() }
                replace != null
            }
            drops += newDrops
        }
    }

//    @EventHandler
//    fun MythicMobLootDropEvent.replaceBurnedMythicLoot() {
//        if (entity.fireTicks == 0) return
//
//        itemEntityContext {
//            val newDrops = mutableListOf<MythicDropsDrop>()
//            drops.drops.removeIf { drop ->
//                drop.
//                val replace = drop.toGeary().get<ReplaceBurnedDrop>()
//                replace?.let { newDrops += MythicDropsDrop(replace.replaceWith.toItemStack()) }
//                replace != null
//            }
//            drops.addAll(newDrops)
//        }
//    }
}
