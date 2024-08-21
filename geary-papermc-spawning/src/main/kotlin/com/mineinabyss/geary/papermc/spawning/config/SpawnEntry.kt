package com.mineinabyss.geary.papermc.spawning.config

import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class SpawnEntry(
    val position: SpawnPosition = SpawnPosition.GROUND,
    val type: SpawnType,
    val radius: @Serializable(with = IntRangeSerializer::class) IntRange = 0..0,
    val amount: @Serializable(with = IntRangeSerializer::class) IntRange = 1..1,
    val regions: List<String> = listOf(),
    val weight: Double = 1.0,
    val conditions: EnsureAction,
)
