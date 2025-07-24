package com.mineinabyss.geary.papermc.spawning.database.dao

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import org.bukkit.Chunk
import org.bukkit.Location


//TODO store per world
class SpawnLocationsDAO(
    val json: Json = Json { ignoreUnknownKeys = true },
) {
    @Serializable
    data class SpreadSpawn(
        /** */
        val entity: String,
    )

    data class SpreadSpawnLocation(
        val id: Int,
        val data: SpreadSpawn,
        val location: Location,
    )

    /**
     * Gets all stored spawn locations at most [radius] blocks away from [location] (in a cube.)
     */
    context(tx: Transaction)
    fun getSpawnsNear(location: Location, radius: Double): List<SpreadSpawnLocation> {
        return tx.getList(
            """
            SELECT id, data, minX, minY, minZ FROM SpawnLocations
            WHERE minX > :x - :rad AND minY > :y - :rad AND minZ > :z - :rad
            AND maxX < :x + :rad AND maxY < :y + :rad AND maxZ < :z + :rad
            ORDER BY abs(minX - :x) + abs(minY - :y) + abs(minZ - :z);
            """.trimIndent(),
            location.x, radius, location.y, location.z,
        ) {
            SpreadSpawnLocation(
                getInt(0),
                getText(1),
                Location(location.world, getDouble(2), getDouble(3), getDouble(4))
            )
        }
    }

    /**
     * Gets all stored spawn positions that land in this [chunk]
     */
    context(tx: Transaction)
    fun getSpawnsInChunk(chunk: Chunk): List<SpreadSpawnLocation> =
        tx.getList<SpreadSpawnLocation>(
            """
            SELECT id, data, minX, minY, minZ FROM SpawnLocations
            WHERE minX > :x AND minZ > :z
            AND maxX < :x + 16 AND maxZ < :z + 16;
            """,
            chunk.x shl 4, chunk.z shl 4
        ) { TODO() }

    /**
     * Gets the stored spawn with [preferred]'s id if it is within [radius],
     * OR the closest stored spawn [location] within [radius],
     * OR null if none are nearby.
     */
    context(tx: Transaction)
    fun getNearestLocationPreferringId(location: Location, radius: Double): SpreadSpawnLocation? {
        TODO()
    }

    context(tx: WriteTransaction)
    fun insertSpawnLocation(data: String, location: Location) {
        val loc = location
        tx.exec(
            """
            INSERT INTO SpawnLocationsRtree(minX, maxX, minY, maxY, minZ, maxZ)
            VALUES (:x, :x, :y, :y, :z, :z)
        """.trimIndent(),
            loc.x, loc.y, loc.z,
        )
        tx.exec(
            "INSERT INTO SpawnLocationsTable(id, data) VALUES (last_insert_rowid(), :data)",
            data
        )
    }

    context(tx: WriteTransaction)
    fun deleteSpawnLocation(id: Int) {
        //TODO foreign key?
        tx.exec("DELETE FROM SpawnLocationsTable WHERE id = :id", id)
        tx.exec("DELETE FROM SpawnLocationsRtree WHERE id = :id", id)
    }
}
