@file:UseSerializers(InstantAsUnixEpochSerializer::class)

package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.spawning.helpers.InstantAsUnixEpochSerializer
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

import java.time.Instant

/**
 * Represents an entity stored in the spread spawn database.
 *
 * @property type Entity type (the one we are spawning).
 * @property category The entity's category (what we are filtering for in the code).
 * @property createdTime When the entity was created in the database.
 */
@Serializable
data class StoredEntity(
    val type: String,
    val category: String = "none",
    val createdTime: Instant = Instant.now(),
) {
    fun asSpawnType(): SpawnType? = SpawnType.getType(type)
}
