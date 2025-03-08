package com.mineinabyss.geary.papermc.features.entities.bucketable

import com.mineinabyss.geary.papermc.tracking.entities.toGeary
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBucketEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

@Serializable
@SerialName("geary:bucketable")
class Bucketable(
    val bucketLiquidRequired: Material = Material.WATER,
    val bucketItem: SerializableItemStack
)

class BucketableListener : Listener {
    @EventHandler
    fun PlayerBucketEntityEvent.cancelBucketEntity() {
        if (!entity.toGeary().has<Bucketable>()) isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerInteractEntityEvent.onPickupMob() {
        val gearyEntity = rightClicked.toGearyOrNull() ?: return

        val bucketable = gearyEntity.get<Bucketable>() ?: run {
            gearyEntity.setPersisting(Bucketable(bucketItem=SerializableItemStack(Material.STONE)))
            return
        }
        val requiredBucket = Material.valueOf(bucketable.bucketLiquidRequired.toString() + "_BUCKET")
        val item = bucketable.bucketItem.toItemStack()
        if (!Material.entries.contains(requiredBucket)) return
        if (player.inventory.getItem(hand).type != requiredBucket) return
        player.inventory.setItemInMainHand(item)
        rightClicked.remove()
        isCancelled = true // Cancel vanilla behaviour
    }
}
