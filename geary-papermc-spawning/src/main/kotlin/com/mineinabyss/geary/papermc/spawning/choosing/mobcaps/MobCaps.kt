package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.google.common.cache.CacheBuilder
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.spawn_types.EntityTypeNames
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class MobCaps(
    config: SpawnConfig,
) {
    private val caps: Map<SpawnCategory, Int> = config.playerCaps
    private val defaultCapLimit: Int = config.defaultCap
    private val searchRadius: Int = config.range.playerCapRadius

    private val mythicLoaded: Boolean by lazy {
        Bukkit.getPluginManager().getPlugin("MythicMobs")?.isEnabled == true
    }

    // MythicMobs config string reads are slow, so cache by mob type
    private val mobSpawnCategoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1.minutes.toJavaDuration())
        .maximumSize(1000)
        .build<String, String>()

    private val entityCategoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(30.seconds.toJavaDuration())
        .build<UUID, SpawnCategory>()

    // Main thread only, callers record what they spawn so caps still hold between refreshes
    private val worldMobs = HashMap<UUID, WorldMobs>()

    fun getCategory(entity: Entity): SpawnCategory =
        entityCategoryCache.get(entity.uniqueId) { computeCategory(entity) }

    private fun computeCategory(entity: Entity): SpawnCategory {
        val cat = entity.toGearyOrNull()?.get<SpawnCategory>() ?: run mmCat@{
            if (!mythicLoaded) return@mmCat null
            val registry = MythicBukkit.inst().mobManager.mobRegistry
            val mob = registry.getActiveMob(entity.uniqueId).getOrNull() ?: return@mmCat null
            val category = mobSpawnCategoryCache.get(mob.mobType) {
                mob.type.config.getString("SpawnCategory") ?: "default"
            }
            return@mmCat SpawnCategory(category)
        }
        return cat ?: SpawnCategory.of(entity)
    }

    private fun scanWorld(world: World, tick: Int): WorldMobs {
        val snapshot = WorldMobs(world.uid, tick)
        val entityLoc = Location(world, 0.0, 0.0, 0.0)
        for (entity in world.livingEntities) {
            if (entity is Player) continue
            entity.getLocation(entityLoc)
            snapshot.add(EntityTypeNames.of(entity), getCategory(entity), entityLoc.x, entityLoc.y, entityLoc.z)
        }
        return snapshot
    }

    // Refreshing here, on the first call in a world after the interval, means a refresh always runs before any spawn in that tick.
    // A refresh after a recorded spawn would count that mob twice
    fun nearbyMobs(location: Location): NearbyMobs {
        val world = location.world
        val tick = Bukkit.getCurrentTick()
        val snapshot = worldMobs[world.uid]?.takeIf { tick - it.tick < SNAPSHOT_REFRESH_TICKS }
            ?: scanWorld(world, tick).also { worldMobs[world.uid] = it }
        return snapshot.around(location.x, location.y, location.z, searchRadius.toDouble())
    }

    /** Filters [spawns] to those under their category cap given [counts], skipping entries whose backing mob cannot be resolved. */
    fun filterAllowed(spawns: List<SpawnEntry>, counts: Map<SpawnCategory, Int>): List<SpawnEntry> {
        return spawns.filter { spawn ->
            val category = runCatching { spawn.type.category }.getOrNull() ?: return@filter false
            val currentCount = counts.getOrDefault(category, 0)
            val limit = caps.getOrDefault(category, defaultCapLimit)
            currentCount < limit
        }
    }

    private companion object {
        // 1 s at 20 TPS, and the old 2 s section cache lifetime at 10 TPS
        const val SNAPSHOT_REFRESH_TICKS = 20
    }
}
