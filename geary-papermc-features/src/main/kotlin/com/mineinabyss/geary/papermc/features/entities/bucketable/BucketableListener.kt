package com.mineinabyss.geary.papermc.features.entities.bucketable

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.toSerializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBucketEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot

@Serializable
@SerialName("geary:bucketable")
class Bucketable(
    val bucketRequired: Material = Material.WATER_BUCKET,
    val bucketedItem: SerializableItemStack
)

class BucketableListener : Listener {

    fun handlePickup(player: Player, hand: EquipmentSlot, entity: Entity) : Boolean {
        val bucketable = entity.toGeary().get<Bucketable>() ?: return false

        if (!Material.entries.contains(bucketable.bucketRequired)) return false
        if (player.inventory.getItem(hand).type != bucketable.bucketRequired) return false

        player.inventory.setItemInMainHand(bucketable.bucketedItem.toItemStack())
        entity.remove()
        return true
    }

    // TODO: Handle this in configs instead of making vanilla mobs bucketable by default
    @EventHandler
    fun PlayerBucketEntityEvent.cancelBucketEntity() {
        val gearyEntity = entity.toGeary() // This entity should be bucketable in vanilla
        if (!gearyEntity.has<Bucketable>()) {
            run {
                gearyEntity.setPersisting(Bucketable(bucketRequired = originalBucket.type,
                                                     bucketedItem = entityBucket.toSerializable()))
            }
        }
        handlePickup(player, hand, entity) // Run custom bucket function
        isCancelled = true // Cancel vanilla behavior
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onPickupMob() {
        if (!handlePickup(player, hand, rightClicked)) return // Will break if no early return
        isCancelled = true // Cancel vanilla behaviour
    }
}
