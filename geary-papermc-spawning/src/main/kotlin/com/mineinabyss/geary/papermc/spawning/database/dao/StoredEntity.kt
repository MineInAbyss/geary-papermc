@file:UseSerializers(InstantAsUnixEpochSerializer::class)

package com.mineinabyss.geary.papermc.spawning.database.dao

import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Represents an entity stored in the spread spawn database.
 */
@Serializable
data class StoredEntity(
    val type: String,
    val createdTime: Instant = Clock.System.now(),
) {
    fun asSpawnType(): SpawnType? = SpawnType.Companion.getType(type)
}

object InstantAsUnixEpochSerializer : KSerializer<Instant> {
    override val descriptor = Long.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSeconds)
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.fromEpochSeconds(decoder.decodeLong())
    }
}
