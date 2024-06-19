package com.mineinabyss.geary.papermc.bridge.emit

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.geary.systems.builders.observeWithData
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.Serializable
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder


@Serializable(with = AddItem.Serializer::class)
data class AddItem(
    val item: SerializableItemStack,
) {
    object Serializer : InnerSerializer<SerializableItemStack, AddItem>(
        serialName = "geary:give",
        inner = SerializableItemStack.serializer(),
        inverseTransform = { it.item },
        transform = ::AddItem
    )
}

fun GearyModule.addItem() = observeWithData<AddItem>().exec(query<BukkitEntity>()) { (entity) ->
    if (entity !is InventoryHolder) return@exec
    if ((entity as? Player)?.gameMode == GameMode.CREATIVE) return@exec

    val inventory = entity.inventory
    val replacement = event.item.toItemStack()
    if (inventory.firstEmpty() != -1) inventory.addItem(replacement)
    else entity.world.dropItemNaturally(entity.location, replacement)
}
