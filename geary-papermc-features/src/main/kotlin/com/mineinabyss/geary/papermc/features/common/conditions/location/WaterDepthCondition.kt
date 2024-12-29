package com.mineinabyss.geary.papermc.features.common.conditions.location

import com.mineinabyss.geary.actions.ActionGroupContext
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.papermc.features.common.conditions.location.GapCondition.Companion.checkGap
import com.mineinabyss.geary.papermc.location
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable(with = WaterDepthCondition.Serializer::class)
class WaterDepthCondition(
    val gap: @Serializable(with = IntRangeSerializer::class) IntRange,
) : Condition {
    override fun ActionGroupContext.execute(): Boolean {
        val location = location?.clone() ?: return true
        return checkGap(location, gap) { it.block.type == Material.WATER }
    }

    object Serializer : InnerSerializer<IntRange, WaterDepthCondition>(
        "geary:water_depth",
        IntRangeSerializer,
        { WaterDepthCondition(it) },
        { it.gap },
    )
}
