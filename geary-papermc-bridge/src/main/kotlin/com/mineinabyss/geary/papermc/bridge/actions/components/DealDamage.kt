package com.mineinabyss.geary.papermc.bridge.actions.components

import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Deals damage to the target entity.
 *
 * @param damage The damage amount.
 */
@Serializable
@SerialName("geary:dealDamage")
data class DealDamage(
    val damage: @Serializable(with = DoubleRangeSerializer::class) DoubleRange,
    val minHealth: Double = 0.0,
    val ignoreArmor: Boolean = false,
)
