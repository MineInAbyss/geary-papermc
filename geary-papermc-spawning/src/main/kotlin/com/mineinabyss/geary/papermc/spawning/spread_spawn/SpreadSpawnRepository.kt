package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.data.SpawnQueries
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import kotlinx.serialization.json.Json
import me.dvyy.sqlite.Database
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox
import kotlin.time.Clock
import kotlin.time.Duration

internal val json: Json = Json { ignoreUnknownKeys = true }

class SpreadSpawnRepository(
    val db: Database,
    val spawns: SpawnQueries,
) {
    /** Gets all stored spawn locations at most [radius] blocks away from [location] (in a cube.) */
    suspend fun getSpawnsNear(location: Location, radius: Double): List<SpreadSpawnLocation> = db.read {
        spawns.getSpawnsNear(x = location.x, y = location.y, z = location.z, rad = radius).map {
            SpreadSpawnLocation.fromStatement(this, location.world)
        }
    }

    /**
     * Counts the number of spawns stored inside a bounding [box] for a given [world],
     * optionally counting only spawns of a certain [type]
     */
    suspend fun countSpawnsInBB(
        world: World,
        box: BoundingBox,
        type: String? = null,
    ): Int = db.read {
        when (type) {
            null -> spawns.countSpawnsInBB(minX = box.minX, minY = box.minY, minZ = box.minZ, maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ)
            else -> spawns.countSpawnsInBBOfType(minX = box.minX, minY = box.minY, minZ = box.minZ, maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ, type = type)
        }.first { getInt(it.count) }
    }

    suspend fun countNearby(location: Location, radius: Double, type: String? = null): Int = db.read {
        val (x, y, z) = location
        when (type) {
            null -> spawns.countNearby(x = x, y = y, z = z, rad = radius)
            else -> spawns.countNearbyOfType(x = x, y = y, z = z, rad = radius, type = type)
        }.first { getInt(it.count) }
    }

    suspend fun getClosestSpawn(
        location: Location,
        maxDistance: Double,
        type: String? = null,
    ): SpreadSpawnLocation? = db.read {
        val (x, y, z) = location
        when (type) {
            null -> spawns.getClosestSpawn(x = x, y = y, z = z, rad = maxDistance)
            else -> spawns.getClosestSpawnOfType(x = x, y = y, z = z, rad = maxDistance, type = type)
        }.firstOrNull { SpreadSpawnLocation.fromStatement(this, location.world) }
    }

    suspend fun getSpawnsInBB(world: World, boundingBox: BoundingBox) = db.read {
        val bb = boundingBox
        spawns.getSpawnsInBB(minX = bb.minX, minY = bb.minY, minZ = bb.minZ, maxX = bb.maxX, maxY = bb.maxY, maxZ = bb.maxZ).map {
            SpreadSpawnLocation.fromStatement(this, world)
        }
    }

    /** Gets all stored spawn positions that land in this [chunk]. */
    suspend fun getSpawnsInChunk(
        chunk: Chunk,
        type: String? = null,
    ): List<SpreadSpawnLocation> = db.read {
        when (type) {
            null -> spawns.getSpawnsInChunk(chunk.x shl 4, chunk.z shl 4)
            else -> spawns.getSpawnsInChunkOfType(chunk.x shl 4, chunk.z shl 4, type)
        }.map { SpreadSpawnLocation.fromStatement(this, chunk.world) }
    }

    suspend fun deleteSpawnsOlderThan(world: World, age: Duration) = db.write {
        val epochSeconds = (Clock.System.now() - age).epochSeconds
        spawns.deleteSpawnsOlderThan(epochSeconds)
    }

    suspend fun deleteSpawnLocation(world: World, id: Long): Unit = db.write {
        spawns.deleteSpawn(id = id)
    }

    suspend fun insertSpawnLocation(location: Location, store: StoredEntity): SpreadSpawnLocation = db.write {
        val (x, y, z) = location
        val id = spawns.insertRtree(x = x, y = y, z = z)
        spawns.insertData(id = id, data = json.encodeToString(store))

        SpreadSpawnLocation(
            id = id,
            stored = store,
            location = location
        )
    }

    suspend fun dropAll(world: World) = db.write { spawns.dropAll() }
}