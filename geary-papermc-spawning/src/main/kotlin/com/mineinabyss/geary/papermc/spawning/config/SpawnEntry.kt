package com.mineinabyss.geary.papermc.spawning.config

import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import com.mineinabyss.idofront.serialization.IntRangeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class SpawnEntry(
    val position: SpawnPosition = SpawnPosition.GROUND,
    val type: SpawnType = SpawnType.None,
    val spread: @Serializable(with = IntRangeSerializer::class) IntRange = 0..0,
    val ySpread: @Serializable(with = IntRangeSerializer::class) IntRange = 0..0,
    val amount: @Serializable(with = IntRangeSerializer::class) IntRange = 1..1,
    val regions: List<String> = listOf(),
    /**
     * When multiple mobs exist in an area, how much should this mob be weighted over others in the same category.
     *
     * Ex. if only one mob from a category can spawn in a spot, it will always be picked.
     * If two can spawn with priorities 1 and 2, the priority 2 mob is twice as likely to be chosen.
     */
    val priority: Double = 1.0,
    /**
     * A chance between 0 and 1 for this spawn to fail immediately when chosen.
     * Useful when trying to make a spawn more rare.
     */
    val chance: Double = 1.0,
    val conditions: List<EnsureAction> = listOf(),
)
