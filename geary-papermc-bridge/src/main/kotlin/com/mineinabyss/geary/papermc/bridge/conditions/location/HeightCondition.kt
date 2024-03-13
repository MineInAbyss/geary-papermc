package com.mineinabyss.geary.papermc.bridge.conditions.location

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("geary:check.height")
class HeightCondition(
    val range: @Serializable(with = IntRangeSerializer::class) IntRange,
    val at: Input<@Contextual Location> = Input.reference("location")
)

fun GearyModule.createHeightConditionChecker() = listener(
    object : ListenerQuery() {
        val condition by source.get<HeightCondition>()
    }
).check {
    val location = condition.at.get(this)
    location.y.toInt() in condition.range
}
