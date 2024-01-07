@file:UseSerializers(
    DoubleRangeSerializer::class
)

package com.mineinabyss.geary.papermc.bridge.conditions

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.helpers.nullOr
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.serialization.DoubleRangeSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import com.mineinabyss.idofront.util.DoubleRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.attribute.Attribute
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

class HealthConditionChecker : CheckingListener() {
    private val Pointers.bukkit by get<BukkitEntity>().on(target)
    private val Pointers.health by get<HealthConditions>().on(source)

    override fun Pointers.check(): Boolean {
        val living = bukkit as? LivingEntity ?: return false

        return (health.within nullOr { living.health in it } && health.withinPercent nullOr {
            living.health / (living.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH)?.value ?: return false) in it
        })
    }
}
