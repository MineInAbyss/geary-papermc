package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import kotlin.time.Duration

@Serializable
@SerialName("geary:start_cooldown")
class StartCooldown(
    val length: @Serializable(with = DurationSerializer::class) Duration,
    val display: @Serializable(with = MiniMessageSerializer::class) Component? = null,
    val id: String,
) : Action {
    override fun ActionGroupContext.execute() {
        entity.setPersisting(
            Cooldowns(
                (entity.get<Cooldowns>()?.cooldowns ?: mapOf())
                    .plus(id to StartedCooldown(System.currentTimeMillis() + length.inWholeMilliseconds, length, display))
            )
        )
    }
}
