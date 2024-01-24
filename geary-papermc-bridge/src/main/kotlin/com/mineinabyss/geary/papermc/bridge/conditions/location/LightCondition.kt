package com.mineinabyss.geary.papermc.bridge.conditions.location

import com.mineinabyss.geary.events.CheckingListener
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.systems.accessors.Pointers
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.block.BlockFace

@Serializable
@SerialName("geary:check.light")
class LightCondition(
    @Serializable(with = IntRangeSerializer::class) val range: IntRange = 0..15,
    val at: Input<@Contextual Location> = Input.reference("location")
)


class LightConditionChecker : CheckingListener() {
    private val Pointers.condition by get<LightCondition>().on(source)

    override fun Pointers.check(): Boolean {
        val location = condition.at.get(this)
        val block = location.block
        // Check the current block's light level or the one above if this block is solid
        val check =
            if (block.isSolid) block.getRelative(org.bukkit.block.BlockFace.UP)
            else block
        return check.lightLevel in condition.range
    }
}
