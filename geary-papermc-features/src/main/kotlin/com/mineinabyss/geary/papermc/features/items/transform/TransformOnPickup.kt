package com.mineinabyss.geary.papermc.features.items.transform

import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.idofront.serialization.SerializableItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Transforms items picked up by a player while this item is equipped, the first [ItemTransform] whose conditions pass wins.
 */
@JvmInline
@Serializable
@SerialName("geary:transform_on_pickup")
value class TransformOnPickup(val transforms: List<ItemTransform>)

@Serializable
data class ItemTransform(
    /** Run against the picked up item, with the player available as `player` */
    val conditions: List<EnsureAction> = listOf(),
    /** Amount is taken from the picked up stack, anything set here is ignored */
    val output: SerializableItemStack,
    /**
     * Run once the item has been transformed, with the output registered as `output` and the player's location in context.
     *
     * Actions needing the player as their entity can take it with `with: { entity: "{{player}}" }`
     */
    val actions: Tasks? = null,
)
