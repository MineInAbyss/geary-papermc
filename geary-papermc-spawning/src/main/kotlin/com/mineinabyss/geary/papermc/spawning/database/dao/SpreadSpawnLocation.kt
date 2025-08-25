package com.mineinabyss.geary.papermc.spawning.database.dao

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.coroutines.withContext
import me.dvyy.sqlite.statement.NamedColumnSqliteStatement
import org.bukkit.Location
import org.bukkit.World
import kotlin.random.Random

/**
 * Represents a specific entity stored at a location in the spread spawns database.
 */
data class SpreadSpawnLocation(
    val id: Int,
    val stored: StoredEntity,
    val location: Location,
) {
    /**
     * Spawns the stored entity at the specified location with a random yaw.
     *
     * Caller is responsible for ensuring the entity isn't spawned multiple times and removed as needed.
     */
    suspend fun spawn(): BukkitEntity? = withContext(gearyPaper.plugin.minecraftDispatcher) {
        val loc = location.toCenterLocation().subtract(0.0, 0.5, 0.5).setRotation(Random.nextFloat() * 360, location.pitch)
        val bukkitEntity = stored.asSpawnType()?.spawnAt(loc) ?: return@withContext null
        bukkitEntity.toGearyOrNull()?.set<SpreadSpawnLocation>(this@SpreadSpawnLocation)
        bukkitEntity
    }

    companion object {
        fun fromStatement(cursor: NamedColumnSqliteStatement, world: World) = with(cursor) {
            SpreadSpawnLocation(
                getInt("id"),
                json.decodeFromString<StoredEntity>(getText("data")),
                Location(world, getDouble("minX"), getDouble("minY"), getDouble("minZ"))
            )
        }
    }
}
