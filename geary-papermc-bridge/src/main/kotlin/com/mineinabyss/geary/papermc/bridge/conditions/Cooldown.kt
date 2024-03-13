package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
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

fun GearyModule.createCooldownChecker() = listener(
    object : ListenerQuery() {
        val cooldownDefinition by source.get<Cooldown>()
    }
).check {
    if (Cooldown.isComplete(entity, source.entity)) {
        Cooldown.start(entity, source.entity, cooldownDefinition)
        return@check true
    }
    return@check false
}
