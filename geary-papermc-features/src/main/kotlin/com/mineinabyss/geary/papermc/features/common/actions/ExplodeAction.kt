package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.actions.expressions.expr
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("geary:explode")
class ExplodeAction(
    val breakBlocks: Expression<Boolean> = expr(true),
    val setFire: Expression<Boolean> = expr(true),
    val power: Expression<Float> = expr(1.0f),
    val at: Expression<@Contextual Location>,
) : Action {
    override fun ActionGroupContext.execute() {
        eval(at).createExplosion(eval(power), eval(setFire), eval(breakBlocks))
    }
}
