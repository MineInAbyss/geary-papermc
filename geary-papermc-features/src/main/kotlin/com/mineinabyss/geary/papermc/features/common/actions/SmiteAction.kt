package com.mineinabyss.geary.papermc.features.common.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.papermc.location
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:smite")
class SmiteAction : Action {
    override fun ActionGroupContext.execute() {
        val location = location ?: return
        location.world.strikeLightning(location)
    }
}
