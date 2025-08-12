package com.mineinabyss.geary.papermc.spawning.database.dao

import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
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
    fun spawn() {
        val loc = location.clone()
        loc.yaw = Random.nextFloat() * 360f
        val type = stored.asSpawnType() ?: return
        val bukkitEntity = type.spawnAt(loc)
        val gearyEntity = bukkitEntity.toGearyOrNull()
        gearyEntity?.set<SpreadSpawnLocation>(this)
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
