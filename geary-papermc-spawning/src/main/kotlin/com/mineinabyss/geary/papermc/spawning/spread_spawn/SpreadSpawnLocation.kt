package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.idofront.typealiases.BukkitEntity
import me.dvyy.sqlite.statement.NamedColumnSqliteStatement
import org.bukkit.Location
import org.bukkit.World
import kotlin.random.Random

/**
 * Represents a specific entity stored at a location in the spread spawns database.
 */
data class SpreadSpawnLocation(
    val id: Long,
    val stored: StoredEntity,
    val location: Location,
) {
    /**
     * Spawns the stored entity at the specified location with a random yaw.
     *
     * Caller is responsible for ensuring the entity isn't spawned multiple times and removed as needed.
     * Must be called on the main thread
     */
    fun spawn(): BukkitEntity? {
        val loc = location.toCenterLocation().subtract(0.0, 0.5, 0.0).setRotation(Random.nextFloat() * 360, location.pitch)
        return stored.asSpawnType()?.spawnAt(loc)
    }

    val pos get() = SpreadPos(location.blockX, location.blockY, location.blockZ)

    /** Epoch seconds, matching what the database stores */
    val createdTime get() = stored.createdTime.epochSecond

    companion object {
        fun fromStatement(cursor: NamedColumnSqliteStatement, world: World) = with(cursor) {
            SpreadSpawnLocation(
                getLong("id"),
                json.decodeFromString<StoredEntity>(getText("data")),
                Location(world, getDouble("minX"), getDouble("minY"), getDouble("minZ"))
            )
        }
    }
}
