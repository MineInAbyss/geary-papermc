package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
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
) {

    companion object {
        fun isComplete(entity: GearyEntity, id: GearyEntity): Boolean {
            val cooldownStarted = entity.getRelation<CooldownStarted>(id)
            val isComplete =
                cooldownStarted == null || System.currentTimeMillis() - cooldownStarted.time >= cooldownStarted.cooldown.length.inWholeMilliseconds
            if (isComplete) entity.removeRelation<CooldownStarted>(id)
            return isComplete
        }

        fun start(entity: GearyEntity, source: GearyEntity, cooldown: Cooldown) {
            entity.setRelation(CooldownStarted(System.currentTimeMillis(), cooldown), source)
        }
    }
}

class CooldownStarted(val time: Long, val cooldown: Cooldown)

class CooldownChecker : CheckingListener() {
    private val Pointers.cooldownDefinition by get<Cooldown>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.check(): Boolean {
        if (Cooldown.isComplete(target.entity, source!!.entity)) {
            Cooldown.start(target.entity, source!!.entity, cooldownDefinition)
            return true
        }
        return false
    }
}
