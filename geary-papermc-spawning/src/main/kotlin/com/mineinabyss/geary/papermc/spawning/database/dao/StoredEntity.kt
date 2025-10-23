@file:UseSerializers(InstantAsUnixEpochSerializer::class)

package com.mineinabyss.geary.papermc.spawning.database.dao

import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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

object InstantAsUnixEpochSerializer : KSerializer<Instant> {
    override val descriptor = Long.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSecond)
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochSecond(decoder.decodeLong())
    }
}
