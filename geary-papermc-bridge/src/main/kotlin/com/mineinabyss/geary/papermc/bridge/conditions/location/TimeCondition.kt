package com.mineinabyss.geary.papermc.bridge.conditions.location

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("geary:check.time")
class TimeCondition(
    //TODO change to range
    val min: Long = -1,
    val max: Long = 10000000,
    val at: Input<@Contextual Location> = Input.reference("location"),
)

fun GearyModule.createTimeConditionChecker() = listener(
    object : ListenerQuery() {
        val condition by source.get<TimeCondition>()
    }
).check {
    val location = condition.at.get(this)
    val time = location.world.time

    // support these two possibilities
    // ====max-----min====
    // ----min=====max----
    with(condition) {
        if (min < max) time in min..max
        else time !in max..min
    }
}
