package com.mineinabyss.geary.papermc.features.items.food

import com.mineinabyss.geary.papermc.tracking.items.itemEntityContext
import com.mineinabyss.idofront.serialization.SerializableItemStack
import io.lumine.mythic.bukkit.events.MythicMobLootDropEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

const val DEFAULT_COOK_TIME = -1

@JvmInline
@Serializable
@SerialName("looty:replace_burned_drop")
value class ReplaceBurnedDrop(
    val replaceWith: SerializableItemStack
)

class ReplaceBurnedDropListener : Listener {
    @EventHandler
    fun EntityDeathEvent.replaceBurnedDrops() {
        if (entity.fireTicks == 0) return

        itemEntityContext {
            val newDrops = mutableListOf<ItemStack>()
            drops.removeIf { drop ->
                val replace = drop.toGeary().get<ReplaceBurnedDrop>()
                replace?.let { newDrops += it.replaceWith.toItemStack() }
                replace != null
            }
            drops.addAll(newDrops)
        }
    }

    @EventHandler
    fun MythicMobLootDropEvent.replaceBurnedMythicLoot() {
        if (entity.fireTicks == 0) return

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
    }
}
