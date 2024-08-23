package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.expr
import com.mineinabyss.geary.papermc.location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:explode")
class ExplodeAction(
    val breakBlocks: Expression<Boolean> = expr(true),
    val setFire: Expression<Boolean> = expr(true),
    val power: Expression<Float> = expr(1.0f),
) : Action {
    override fun ActionGroupContext.execute() {
        val location = location ?: return
        location.createExplosion(eval(power), eval(setFire), eval(breakBlocks))
    }
}
