package com.mineinabyss.geary.papermc.features.common.cooldowns

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.MiniMessageSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data class StartedCooldown(
    val endTime: Long,
    val length: Duration,
    val display: @Serializable(with = MiniMessageSerializer::class) net.kyori.adventure.text.Component? = null,
) {
    val timeLeft: Duration get() = (endTime - System.currentTimeMillis()).milliseconds
    val startDuration: Int = 1000 // Hardcoded
    val endDuration: Int = 1000   // Hardcoded
    val startTime: Long = endTime - length.inWholeMilliseconds
    fun isVisible(): Boolean = System.currentTimeMillis() <= startTime + startDuration || System.currentTimeMillis() >= endTime - endDuration
    fun isComplete(): Boolean = System.currentTimeMillis() >= endTime
}

@Serializable(with = Cooldowns.Serializer::class)
data class Cooldowns(
    val cooldowns: Map<String, StartedCooldown>,
) {
    @Transient
    val hasDisplayableCooldowns = cooldowns.values.any { it.display != null }

    object Serializer : InnerSerializer<Map<String, StartedCooldown>, Cooldowns>(
        "geary:cooldowns",
        MapSerializer(String.serializer(), StartedCooldown.serializer()),
        { Cooldowns(it) },
        { it.cooldowns }
    )

    companion object {
        fun isComplete(entity: GearyEntity, id: String): Boolean {
            val endTime = entity.get<Cooldowns>()?.cooldowns?.get(id)?.endTime ?: return true
            return System.currentTimeMillis() >= endTime
        }

        fun areComplete(entity: GearyEntity, ids: List<String>): Boolean {
            val endTimes = entity.get<Cooldowns>()?.cooldowns ?: return true
            return ids.all { System.currentTimeMillis() >= (endTimes[it]?.endTime ?: return true) }
        }
    }
}

@Serializable(with = CooldownCondition.Serializer::class)
data class CooldownCondition(
    val ids: List<String>,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val entity = entity ?: return true
        return Cooldowns.areComplete(entity, ids)
    }

    object Serializer : InnerSerializer<List<String>, CooldownCondition>(
        "geary:cooldowns_complete",
        ListSerializer(String.serializer()),
        { CooldownCondition(it) },
        { it.ids }
    )
}
