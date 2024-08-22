package com.mineinabyss.geary.papermc.features.common.actions.blocks

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.idofront.location.down
import com.mineinabyss.idofront.location.up
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:gap")
class GapCondition(
    val gap: @Serializable(with = IntRangeSerializer::class) IntRange,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val location = location
        val topGap = (0..gap.max()).firstOrNull {
            if (it > gap.min() && gap.max() == Int.MAX_VALUE) return true
            !location.up(it).block.isPassable
        } ?: gap.max()

        val bottomGap = (0..gap.max()).firstOrNull {
            if (topGap + it > gap.min() && gap.max() == Int.MAX_VALUE) return true
            !location.down(it).block.isPassable
        } ?: gap.max()

        return topGap + bottomGap in gap
    }
}
