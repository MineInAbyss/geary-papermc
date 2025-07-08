package com.mineinabyss.geary.papermc.features.items.cycleproperty

import com.mineinabyss.idofront.serialization.FloatRangeSerializer
import com.mineinabyss.idofront.util.FloatRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:cycle_property")
class CycleProperty(
    val customModelData: CycleCustomModelData
) {
    @Serializable
    data class CycleCustomModelData(
        val floats: List<@Serializable(FloatRangeSerializer::class) FloatRange> = listOf(0f..0f),
        val flags: List<Boolean?> = emptyList(),
        val strings: List<List<String>> = emptyList()
    )
}