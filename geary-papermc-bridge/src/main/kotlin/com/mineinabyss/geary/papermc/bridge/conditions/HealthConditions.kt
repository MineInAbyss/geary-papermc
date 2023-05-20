@file:UseSerializers(
    DoubleRangeSerializer::class
)

package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.util.DoubleRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.entity.Player

/**
 * Conditions that can be checked against a [Player].
 */
//TODO add more!
@Serializable
@SerialName("geary:check.health")
class HealthConditions(
    val within: DoubleRange? = null,
    val withinPercent: DoubleRange? = null,
)

