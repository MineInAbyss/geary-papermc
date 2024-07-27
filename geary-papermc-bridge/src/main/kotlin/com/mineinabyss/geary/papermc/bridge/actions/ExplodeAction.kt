package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("geary:explode")
class ExplodeAction(
    val breakBlocks: Expression<Boolean> = Expression.Fixed(true),
    val setFire: Expression<Boolean> = Expression.Fixed(true),
    val power: Expression<Float> = Expression.Fixed(1.0f),
    val at: Expression<@Contextual Location>,
) : Action {
    override fun ActionGroupContext.execute() {
        eval(at).createExplosion(eval(power), eval(setFire), eval(breakBlocks))
    }
}
