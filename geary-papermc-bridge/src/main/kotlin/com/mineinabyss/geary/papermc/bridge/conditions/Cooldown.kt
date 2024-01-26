package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.systems.accessors.Pointers
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
    val displayName: @Serializable(with = MiniMessageSerializer::class) Component? = null,
)

class CooldownStarted(val time: Long)

class CooldownChecker : CheckingListener() {
    private val Pointers.cooldownDefinition by get<Cooldown>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.check(): Boolean {
        val cooldown = target.entity.getRelation<CooldownStarted>(source!!.entity)
        if (cooldown == null || System.currentTimeMillis() - cooldown.time >= cooldownDefinition.length.inWholeMilliseconds) {
            target.entity.setRelation(CooldownStarted(System.currentTimeMillis()), source!!.entity)
            return true
        }
        return false
    }
}
