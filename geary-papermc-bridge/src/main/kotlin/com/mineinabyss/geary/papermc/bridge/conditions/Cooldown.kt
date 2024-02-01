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
        fun isComplete(entity: GearyEntity, id: GearyEntity, cooldown: Cooldown): Boolean {
            val cooldownStarted = entity.getRelation<CooldownStarted>(id)
            return cooldownStarted == null || System.currentTimeMillis() - cooldownStarted.time >= cooldown.length.inWholeMilliseconds
        }

        fun start(entity: GearyEntity, source: GearyEntity) {
            entity.setRelation(CooldownStarted(System.currentTimeMillis()), source)
        }
    }
}

class CooldownStarted(val time: Long)

class CooldownChecker : CheckingListener() {
    private val Pointers.cooldownDefinition by get<Cooldown>().on(source)

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.check(): Boolean {
        if (Cooldown.isComplete(target.entity, source!!.entity, cooldownDefinition)) {
            Cooldown.start(target.entity, source!!.entity)
            return true
        }
        return false
    }
}
