package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

@Serializable
@SerialName("geary:send_action_bar")
class SendActionBarAction(
    val text: Expression<@Serializable(with = MiniMessageSerializer::class) Component>,
) : Action {
    override fun ActionGroupContext.execute() {
        val player = entity.get<Player>() ?: return
        player.sendActionBar(eval(text))
    }
}
