package com.mineinabyss.geary.papermc.spawning.database.dao

import com.mineinabyss.geary.papermc.spawning.database.schema.SpawnLocationTables.dataTable
import com.mineinabyss.geary.papermc.spawning.database.schema.SpawnLocationTables.locationsView
import com.mineinabyss.geary.papermc.spawning.database.schema.SpawnLocationTables.rtree
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import kotlin.time.Clock
import kotlin.time.Duration

internal val json: Json = Json { ignoreUnknownKeys = true }

//TODO store per world
class SpawnLocationsDAO {
    /** Gets all stored spawn locations at most [radius] blocks away from [location] (in a cube.) */
    context(tx: Transaction)
    fun getSpawnsNear(location: Location, radius: Double): List<SpreadSpawnLocation> = tx.select(
        """
        SELECT id, data, minX, minY, minZ FROM ${locationsView(location.world)}
        WHERE minX > :x - :rad AND minY > :y - :rad AND minZ > :z - :rad
        AND maxX < :x + :rad AND maxY < :y + :rad AND maxZ < :z + :rad
        ORDER BY abs(minX - :x) + abs(minY - :y) + abs(minZ - :z);
        """.trimIndent(),
        location.x, radius, location.y, location.z,
    ).map {
        SpreadSpawnLocation.fromStatement(this)
    }

    /** Counts the number of spawns stored inside a bounding [box] for a given [world]. */
    context(tx: Transaction)
    fun countSpawnsInBB(world: World, box: BoundingBox): Int = tx.select(
        """
        SELECT count(*) FROM ${rtree(world)}
        WHERE minX >= :minX AND minY >= :minY AND minZ >= :minZ
        AND maxX < :maxX AND maxY < :maxY AND maxZ < :maxZ
        """.trimIndent(),
        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ,
    ).first { getInt(0) }

    context(tx: Transaction)
    fun areSpawnsNearby(location: Location, radius: Double): Boolean {
        val (x, y, z) = location
        return tx.select(
            """
            SELECT count(*) FROM ${rtree(location.world)}
            WHERE minX > :x - :rad AND minY > :y - :rad AND minZ > :z - :rad
            AND maxX < :x + :rad AND maxY < :y + :rad AND maxZ < :z + :rad
            AND (minX - :x) * (minX - :x) +
                (minY - :y) * (minY - :y) +
                (minZ - :z) * (minZ - :z) <= :rad * :rad
            LIMIT 1;
            """.trimIndent(),
            x, radius, y, z
        ).first { getInt(0) } > 0
    }

    context(tx: Transaction)
    fun getClosestSpawn(location: Location, maxDistance: Double): SpreadSpawnLocation? {
        val (x, y, z) = location
        return tx.select(
            """
            SELECT id, data, minX, minY, minZ FROM ${locationsView(location.world)}
            WHERE minX > :x - :rad AND minY > :y - :rad AND minZ > :z - :rad
            AND maxX < :x + :rad AND maxY < :y + :rad AND maxZ < :z + :rad
            ORDER BY square(minX - :x) + square(minY - :y) + square(minZ - :z)
            LIMIT 1;
            """.trimIndent(),
            x, maxDistance, y, z
        ).firstOrNull {
            SpreadSpawnLocation.fromStatement(this)
        }
    }

    /** Gets all stored spawn positions that land in this [chunk]. */
    context(tx: Transaction)
    fun getSpawnsInChunk(chunk: Chunk): List<SpreadSpawnLocation> = tx.select(
        """
        SELECT id, data, minX, minY, minZ FROM ${locationsView(chunk.world)}
        WHERE minX >= :x AND minZ >= :z
        AND maxX < :x + 16 AND maxZ < :z + 16;
        """.trimIndent(),
        chunk.x shl 4, chunk.z shl 4
    ).map { SpreadSpawnLocation.fromStatement(this) }

    context(tx: WriteTransaction)
    fun insertSpawnLocation(location: Location, store: StoredEntity) {
        val (x, y, z) = location
        tx.exec(
            """
            INSERT INTO ${rtree(location.world)}(minX, maxX, minY, maxY, minZ, maxZ)
            VALUES (:x, :x, :y, :y, :z, :z)
            """.trimIndent(),
            x, y, z
        )
        tx.exec(
            "INSERT INTO ${dataTable(location.world)}(id, data) VALUES (last_insert_rowid(), json(:data))",
            json.encodeToString(store)
        )
    }

    /** Deletes a stored spawn location by its row [id]. */
    context(tx: WriteTransaction)
    fun deleteSpawnLocation(world: World, id: Int) {
        tx.exec("DELETE FROM ${dataTable(world)} WHERE id = :id", id)
    }

    context(tx: WriteTransaction)
    fun deleteSpawnsOlderThan(world: World, age: Duration) {
        val epochSeconds = (Clock.System.now() - age).epochSeconds
        tx.exec("DELETE FROM ${dataTable(world)} WHERE data ->> 'createdTime' < :epochSeconds", epochSeconds)
    }
}
