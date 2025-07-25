package com.mineinabyss.geary.papermc.spawning.database.dao

import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import kotlinx.serialization.Serializable

/**
 * Represents an entity stored in the spread spawn database.
 */
@Serializable
data class StoredEntity(
    val type: String,
) {
    fun asSpawnType(): SpawnType? = SpawnType.Companion.getType(type)
}
