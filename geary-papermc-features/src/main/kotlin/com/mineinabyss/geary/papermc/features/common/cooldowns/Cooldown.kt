package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.actions.execute
import com.mineinabyss.geary.serialization.setPersisting
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import kotlin.time.Duration

@Serializable
@SerialName("geary:cooldown")
class Cooldown(
    val length: @Serializable(with = DurationSerializer::class) Duration,
    val display: @Serializable(with = MiniMessageSerializer::class) Component? = null,
    val id: String,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val cooldownPassed = CooldownCondition(listOf(id)).execute(this)
        if (!cooldownPassed) return false
        entity?.setPersisting(
            Cooldowns(
                (entity?.get<Cooldowns>()?.cooldowns ?: mapOf())
                    .plus(id to StartedCooldown(System.currentTimeMillis() + length.inWholeMilliseconds, length, display))
            )
        )
        return true
    }
}
