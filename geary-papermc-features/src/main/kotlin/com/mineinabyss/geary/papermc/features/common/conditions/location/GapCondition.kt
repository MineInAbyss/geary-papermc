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
        fun checkGap(
            location: Location,
            gap: IntRange,
            checkBelow: Boolean = true,
            checkAbove: Boolean = true,
            isPartOfGap: (Location) -> Boolean,
        ): Boolean {
            val minGap = gap.first
            require(minGap <= gap.last) { "Gap range $gap is empty" }
            val openEnded = gap.last == Int.MAX_VALUE
            val minHeight = location.world.minHeight
            val maxHeight = location.world.maxHeight
            // Clamping to the world's height span gives the same ranges below and keeps y + maxGap from overflowing
            val maxGap = gap.last.coerceAtMost(maxHeight - minHeight)
            val y = location.y.roundToInt().coerceIn(minHeight..maxHeight)
            val topRange = (y + maxGap).coerceAtMost(maxHeight)
            val bottomRange = (y - maxGap).coerceAtLeast(minHeight)
            val topGap = if (!checkAbove) y + 1 else (y..topRange + 1).firstOrNull {
                if (openEnded && it - y >= minGap) return true
                !isPartOfGap(location.apply { this.y = it.toDouble() })
            } ?: (topRange + 1)

            val bottomGap = if (!checkBelow) y - 1 else (y downTo bottomRange - 1).firstOrNull {
                if (openEnded && topGap - it - 1 >= minGap) return true
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
