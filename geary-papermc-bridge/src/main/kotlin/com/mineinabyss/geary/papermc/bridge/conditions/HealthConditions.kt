@file:UseSerializers(
    DoubleRangeSerializer::class
)

package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.helpers.nullOr
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.idofront.util.DoubleRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Conditions that can be checked against a [Player].
 */
//TODO add more!
@Serializable
@SerialName("geary:check.health")
class HealthConditions(
    val within: DoubleRange? = null,
    val withinPercent: DoubleRange? = null,
)

fun GearyModule.createHealthConditionChecker() = listener(
    object : ListenerQuery() {
        val bukkit by get<BukkitEntity>()
        val health by source.get<HealthConditions>()
    }
).check {
    val living = bukkit as? LivingEntity ?: return@check false

    (health.within nullOr { living.health in it } && health.withinPercent nullOr {
        living.health / (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value
            ?: return@check false) in it
    })
}
