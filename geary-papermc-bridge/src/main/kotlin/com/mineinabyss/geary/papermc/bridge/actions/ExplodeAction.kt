package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
class ExplodeAction(
    val breakBlocks: Expression<Boolean>,
    val setFire: Expression<Boolean>,
    val power: Expression<Float>,
    val at: Expression<@Contextual Location>,
) : Action {
    override fun ActionGroupContext.execute() {
        eval(at).createExplosion(eval(power), eval(setFire), eval(breakBlocks))
    }
}
