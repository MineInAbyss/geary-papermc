package com.mineinabyss.geary.papermc.features.general.cooldown

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.component
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
        inline fun <reified T> isComplete(entity: GearyEntity): Boolean {
            return isComplete(entity, component<T>())
        }

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
