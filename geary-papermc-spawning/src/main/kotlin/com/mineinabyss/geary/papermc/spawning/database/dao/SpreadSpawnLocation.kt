package com.mineinabyss.geary.papermc.spawning.database.dao

import me.dvyy.sqlite.statement.NamedColumnSqliteStatement
import org.bukkit.Location
import org.bukkit.World

/**
 * Represents a specific entity stored at a location in the spread spawns database.
 */
data class SpreadSpawnLocation(
    val id: Int,
    val stored: StoredEntity,
    val location: Location,
) {
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
