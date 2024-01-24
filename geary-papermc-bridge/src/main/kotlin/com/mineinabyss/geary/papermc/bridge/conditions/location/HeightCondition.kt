package com.mineinabyss.geary.papermc.bridge.conditions.location

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.conditions.HealthConditions
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.papermc.bridge.helpers.nullOr
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity

@Serializable
@SerialName("geary:check.height")
class HeightCondition(
    val range: @Serializable(with = IntRangeSerializer::class) IntRange,
    val at: Input<@Contextual Location> = Input.reference("location")
)

class HeightConditionChecker : CheckingListener() {
    private val Pointers.condition by get<HeightCondition>().on(source)

    override fun Pointers.check(): Boolean {
        val location = condition.at.get(this)
        return location.y.toInt() in condition.range
    }
}
