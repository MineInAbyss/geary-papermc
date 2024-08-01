package com.mineinabyss.geary.papermc.features.common.conditions

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

@JvmInline
@Serializable
@SerialName("geary:chance")
value class Chance(val percentage: Double) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        return Random.nextDouble() < percentage
    }
}
