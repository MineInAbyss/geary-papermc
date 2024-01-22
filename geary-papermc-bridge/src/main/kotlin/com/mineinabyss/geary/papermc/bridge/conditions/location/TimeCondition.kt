package com.mineinabyss.geary.papermc.bridge.conditions.location

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.accessors.Pointers
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
    val at: Input<@Contextual Location> = Input.reference("event.location"),
)


class TimeConditionChecker : CheckingListener() {
    private val Pointers.condition by get<TimeCondition>().on(source)

    override fun Pointers.check(): Boolean {
        val location = condition.at.get(this)
        val time = location.world.time

        // support these two possibilities
        // ====max-----min====
        // ----min=====max----
        return with(condition) {
            if (min < max) time in min..max
            else time !in max..min
        }
    }
}
