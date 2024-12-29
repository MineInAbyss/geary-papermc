package com.mineinabyss.geary.papermc.features.common.conditions.location

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Location
import kotlin.math.roundToInt

@Serializable(with = GapCondition.Serializer::class)
class GapCondition(
    val gap: @Serializable(with = IntRangeSerializer::class) IntRange,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val location = location?.clone() ?: return true
        return checkGap(location, gap) { it.block.isEmpty }
    }

    companion object {
        fun checkGap(location: Location, gap: IntRange, isPartOfGap: (Location) -> Boolean): Boolean {
            val y = location.y.roundToInt().coerceIn(location.world.minHeight..location.world.maxHeight)
            val topRange = (y + gap.max()).coerceAtMost(location.world.maxHeight)
            val bottomRange = (y - gap.max()).coerceAtLeast(location.world.minHeight)
            val topGap = (y..topRange + 1).firstOrNull {
                if (it > gap.min() && gap.max() == Int.MAX_VALUE) return true
                !isPartOfGap(location.apply { this.y = it.toDouble() })
            } ?: (topRange + 1)

            val bottomGap = (y downTo bottomRange - 1).firstOrNull {
                if (topGap + it > gap.min() && gap.max() == Int.MAX_VALUE) return true
                !isPartOfGap(location.apply { this.y = it.toDouble() })
            } ?: (bottomRange - 1)

            return (topGap - bottomGap - 1) in gap
        }
    }

    object Serializer : InnerSerializer<IntRange, GapCondition>(
        "geary:gap",
        IntRangeSerializer,
        { GapCondition(it) },
        { it.gap },
    )
}
