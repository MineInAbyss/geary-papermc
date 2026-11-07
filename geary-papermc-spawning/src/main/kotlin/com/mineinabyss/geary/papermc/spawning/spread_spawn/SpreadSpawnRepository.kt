package com.mineinabyss.geary.papermc.spawning.spread_spawn

import com.mineinabyss.geary.papermc.data.SpawnQueries
import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
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
        categories: List<String>? = null,
    ): Int = db.read {
        when (categories) {
            null -> spawns.countSpawnsInBB(minX = box.minX, minY = box.minY, minZ = box.minZ, maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ)
            else -> spawns.countSpawnsInBBOfType(minX = box.minX, minY = box.minY, minZ = box.minZ, maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ, categories = json(categories))
        }.first { getInt(it.count) }
    }

    suspend fun countNearby(
        location: Location, radius: Double,
        categories: List<String>? = null,
    ): Int = db.read {
        val (x, y, z) = location
        when (categories) {
            null -> spawns.countNearby(x = x, y = y, z = z, rad = radius)
            else -> spawns.countNearbyOfType(x = x, y = y, z = z, rad = radius, categories = json(categories))
        }.first { getInt(it.count) }
    }


    suspend fun getClosestSpawn(
        location: Location,
        maxDistance: Double,
        categories: List<String>? = null,
    ): SpreadSpawnLocation? = db.read {
        val (x, y, z) = location
        when (categories) {
            null -> spawns.getClosestSpawn(x = x, y = y, z = z, rad = maxDistance)
            else -> spawns.getClosestSpawnOfType(x = x, y = y, z = z, rad = maxDistance, categories = json(categories))
        }.firstOrNull { SpreadSpawnLocation.fromStatement(this, location.world) }
    }

    suspend fun getSpawnsInBB(
        world: World,
        boundingBox: BoundingBox,
        categories: List<String>? = null,
    ): List<SpreadSpawnLocation> = db.read {
        when (categories) {
            null -> spawns.getSpawnsInBB(minX = boundingBox.minX, minY = boundingBox.minY, minZ = boundingBox.minZ, maxX = boundingBox.maxX, maxY = boundingBox.maxY, maxZ = boundingBox.maxZ)
            else -> spawns.getSpawnsInBBOfType(minX = boundingBox.minX, minY = boundingBox.minY, minZ = boundingBox.minZ, maxX = boundingBox.maxX, maxY = boundingBox.maxY, maxZ = boundingBox.maxZ, categories = json(categories))
        }.map { SpreadSpawnLocation.fromStatement(this, world) }
    }

    /** Gets all stored spawn positions that land in this [chunk]. */
    suspend fun getSpawnsInChunk(
        chunk: Chunk,
        categories: List<String>? = null,
    ): List<SpreadSpawnLocation> = db.read {
        when (categories) {
            null -> spawns.getSpawnsInChunk(chunk.x shl 4, chunk.z shl 4)
            else -> spawns.getSpawnsInChunkOfType(chunk.x shl 4, chunk.z shl 4, categories = json(categories))
        }.map { SpreadSpawnLocation.fromStatement(this, chunk.world) }
    }

    suspend fun getSpawnsWithIds(world: World, ids: Collection<Long>): List<SpreadSpawnLocation> = db.read {
        spawns.getSpawnsWithIds(jsonIds(ids)).map { SpreadSpawnLocation.fromStatement(this, world) }
    }

    suspend fun deleteSpawnsOlderThan(world: World, age: Duration, keepIds: Collection<Long> = emptyList()) = db.write {
        val epochSeconds = (Clock.System.now() - age).epochSeconds
        spawns.deleteSpawnsOlderThanExcept(epochSeconds, jsonIds(keepIds))
    }

    suspend fun deleteSpawnLocation(world: World, id: Long): Unit = db.write {
        spawns.deleteSpawn(id = id)
    }

    suspend fun deleteSpawnLocations(world: World, ids: Collection<Long>): Unit = db.write {
        spawns.deleteSpawns(jsonIds(ids))
    }

    suspend fun insertSpawnLocation(location: Location, store: StoredEntity): SpreadSpawnLocation = db.write {
        val (x, y, z) = location
        spawns.createCategoriesIfMissing(json(listOf(store.category)))
        spawns.insertRtree(x = x, y = y, z = z, category = store.category)
        val id = spawns.lastInsertedRtreeId().first { getLong(it.id) }
        spawns.insertData(id = id, data = json.encodeToString(store))

        SpreadSpawnLocation(
            id = id,
            stored = store,
            location = location
        )
    }

    suspend fun dropAll(world: World) = db.write { spawns.dropAll() }

    /** Calls [onEach] on the database read thread */
    suspend fun loadSurfaces(world: World, onEach: (chunkKey: Long, surface: ChunkSurface) -> Unit): Int = db.read {
        var count = 0
        spawns.loadSurfaceChunks(world.uid.toString()).forEach {
            onEach(getLong(it.chunk), ChunkSurface.decode(getBlob(it.histogram), getBlob(it.positions), getInt(it.version), getLong(it.scanned_at)))
            count++
        }
        count
    }

    suspend fun putSurfaces(world: World, surfaces: Map<Long, ChunkSurface>) = db.write {
        val worldId = world.uid.toString()
        surfaces.forEach { (key, surface) ->
            spawns.putSurfaceChunk(worldId, key, surface.version, surface.scannedAt, surface.encodeHistogram(), surface.encodePositions())
        }
    }

    suspend fun dropSurfaces(world: World) = db.write { spawns.dropSurfaceChunks(world.uid.toString()) }

    private fun json(filter: List<String>) = json.encodeToString(ListSerializer(String.serializer()), filter)

    private fun jsonIds(ids: Collection<Long>) = json.encodeToString(ListSerializer(Long.serializer()), ids.toList())
}
