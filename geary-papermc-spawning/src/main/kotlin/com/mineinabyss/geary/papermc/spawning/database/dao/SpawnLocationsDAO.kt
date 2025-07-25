package com.mineinabyss.geary.papermc.spawning.database.dao

import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import org.bukkit.Chunk
import org.bukkit.Location

internal val json: Json = Json { ignoreUnknownKeys = true }

//TODO store per world
class SpawnLocationsDAO {
    /** Gets all stored spawn locations at most [radius] blocks away from [location] (in a cube.) */
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
            SpreadSpawnLocation.fromStatement(this)
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
        ) { SpreadSpawnLocation.fromStatement(this) }

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
    fun insertSpawnLocation(location: Location, store: StoredEntity) {
        val (x, y, z) = location
        tx.exec(
            """
            INSERT INTO SpawnLocationsRtree(minX, maxX, minY, maxY, minZ, maxZ)
            VALUES (:x, :x, :y, :y, :z, :z)
            """.trimIndent(),
            x, y, z
        )
        tx.exec(
            "INSERT INTO SpawnLocationsTable(id, data) VALUES (last_insert_rowid(), json(:data))",
            json.encodeToString(store)
        )
    }

    /** Deletes a stored spawn location by its row [id]. */
    context(tx: WriteTransaction)
    fun deleteSpawnLocation(id: Int) {
        tx.exec("DELETE FROM SpawnLocationsTable WHERE id = :id", id)
        tx.exec("DELETE FROM SpawnLocationsRtree WHERE id = :id", id)
    }
}
