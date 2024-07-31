package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("geary:smite")
class SmiteAction(
    val at: Expression<@Contextual Location>,
) : Action {
    override fun ActionGroupContext.execute() {
        eval(at).world.strikeLightning(eval(at))
    }
}
