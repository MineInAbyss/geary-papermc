package com.mineinabyss.geary.papermc.features.common.actions.blocks

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Serializable

@Serializable(with = GapCondition.Serializer::class)
class GapCondition(
    val gap: @Serializable(with = IntRangeSerializer::class) IntRange,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val location = location?.clone() ?: return true
        val y = location.y.toInt().coerceIn(location.world.minHeight..location.world.maxHeight)
        val topRange = gap.max().coerceAtMost(location.world.maxHeight)
        val bottomRange = gap.min().coerceAtMost(location.world.minHeight)
        val topGap = (y..topRange).firstOrNull {
            if (it > gap.min() && gap.max() == Int.MAX_VALUE) return true
            !location.apply { this.y = it.toDouble() }.block.isEmpty
        } ?: topRange

        val bottomGap = (y downTo bottomRange).firstOrNull {
            if (topGap + it > gap.min() && gap.max() == Int.MAX_VALUE) return true
            !location.apply { this.y = it.toDouble() }.block.isEmpty
        } ?: bottomRange

        return topGap - bottomGap in gap
    }

    object Serializer : InnerSerializer<IntRange, GapCondition>(
        "geary:gap",
        IntRangeSerializer,
        { GapCondition(it) },
        { it.gap },
    )
}
