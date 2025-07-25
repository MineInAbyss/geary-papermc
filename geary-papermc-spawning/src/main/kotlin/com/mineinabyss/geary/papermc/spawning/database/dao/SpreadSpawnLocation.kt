package com.mineinabyss.geary.papermc.spawning.database.dao

import me.dvyy.sqlite.binds.NamedColumnSqliteStatement
import org.bukkit.Location

/**
 * Represents a specific entity stored at a location in the spread spawns database.
 */
data class SpreadSpawnLocation(
    val id: Int,
    val stored: StoredEntity,
    val location: Location,
) {
    companion object {
        fun fromStatement(cursor: NamedColumnSqliteStatement) = with(cursor) {
            SpreadSpawnLocation(
                getInt("id"),
                json.decodeFromString<StoredEntity>(getText("data")),
                Location(null, getDouble("minX"), getDouble("minY"), getDouble("minZ"))
            )
        }
    }
}
