package com.mineinabyss.geary.papermc.features.items.repair

import com.mineinabyss.geary.actions.Tasks
import com.mineinabyss.geary.actions.actions.EnsureAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Right clicking this item from the cursor onto a damaged item transfers the kit's remaining durability onto it.
 * Only as much as the item is missing is used, leaving the rest on the kit.
 *
 * Kits without `max_damage` fully repair the item and consume one from the stack
 */
@Serializable
@SerialName("geary:repair_kit")
class RepairKit(
    /** Run against the damaged item, with the player available as `player` */
    val conditions: List<EnsureAction> = listOf(),
    /**
     * Run once the item has been repaired, with the repaired item as the entity, the durability restored as `repaired` and the player's location in context.
     *
     * Actions needing the player as their entity can take it with `with: { entity: "{{player}}" }`
     */
    val actions: Tasks? = null,
)
