package com.mineinabyss.geary.papermc.features.common.conditions.location

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.features.common.conditions.location.GapCondition.Companion.checkGap
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Serializable

@Serializable(with = GapAboveCondition.Serializer::class)
class GapAboveCondition(
    val gap: @Serializable(with = IntRangeSerializer::class) IntRange,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val location = location?.clone() ?: return true
        return checkGap(location, gap, checkBelow = false) { it.block.isEmpty }
    }

    object Serializer : InnerSerializer<IntRange, GapAboveCondition>(
        "geary:gap_above",
        IntRangeSerializer,
        { GapAboveCondition(it) },
        { it.gap },
    )
}
