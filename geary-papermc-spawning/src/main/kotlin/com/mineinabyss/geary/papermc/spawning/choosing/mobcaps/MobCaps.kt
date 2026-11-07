package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.google.common.cache.CacheBuilder
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.spawn_types.EntityTypeNames
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import io.lumine.mythic.bukkit.MythicBukkit
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.entity.Entity
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class MobCaps(
    config: SpawnConfig,
) {
    private val caps: Map<SpawnCategory, Int> = config.playerCaps
    private val defaultCapLimit: Int = config.defaultCap
    val searchRadius: Double = config.range.playerCapRadius.toDouble()

    private val mythicLoaded: Boolean by lazy {
        Bukkit.getPluginManager().getPlugin("MythicMobs")?.isEnabled == true
    }

    // MythicMobs config string reads are slow, so cache by mob type
    private val mobSpawnCategoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1.minutes.toJavaDuration())
        .maximumSize(1000)
        .build<String, String>()

    private class CachedMob(val category: SpawnCategory, val categoryTick: Int, val type: String?, val typeTick: Int)

    // Per world and keyed by entity id, which the server never reuses while running.
    // Every scan builds the next map from the entities it sees, so removed entities drop out
    private val entityInfo = HashMap<UUID, Int2ObjectOpenHashMap<CachedMob>>()

    // Main thread only, callers record what they spawn so caps still hold between refreshes
    private val worldMobs = HashMap<UUID, WorldMobs>()

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

    // Walks the NMS entity list with the same filter as World.getLivingEntities, without building a list of Bukkit wrappers
    private fun scanWorld(world: World, tick: Int): WorldMobs {
        val snapshot = WorldMobs(world.uid, tick)
        val previous = entityInfo[world.uid]
        val infos = Int2ObjectOpenHashMap<CachedMob>(previous?.size ?: Hash.DEFAULT_INITIAL_SIZE)
        for (entity in (world as CraftWorld).handle.entities.all) {
            if (entity !is LivingEntity || entity is ServerPlayer || !entity.isAlive || !entity.valid) continue
            val id = entity.id
            val info = refreshed(previous?.get(id), entity, tick)
            infos.put(id, info)
            snapshot.add(info.type, info.category, entity.x, entity.y, entity.z)
        }
        entityInfo[world.uid] = infos
        return snapshot
    }

    private fun refreshed(cached: CachedMob?, entity: LivingEntity, tick: Int): CachedMob {
        val categoryFresh = cached != null && tick - cached.categoryTick < CATEGORY_REFRESH_TICKS
        val typeFresh = cached != null &&
                tick - cached.typeTick < if (cached.type == null) TYPE_MISS_REFRESH_TICKS else TYPE_REFRESH_TICKS
        if (categoryFresh && typeFresh) return cached
        val bukkit = entity.bukkitEntity
        return CachedMob(
            category = if (categoryFresh) cached.category else computeCategory(bukkit),
            categoryTick = if (categoryFresh) cached.categoryTick else tick,
            type = if (typeFresh) cached.type else EntityTypeNames.resolve(bukkit),
            typeTick = if (typeFresh) cached.typeTick else tick,
        )
    }

    // Refreshing here, on the first call in a world after the interval, means a refresh always runs before any spawn in that tick.
    // A refresh after a recorded spawn would count that mob twice
    fun nearbyMobs(location: Location): NearbyMobs =
        snapshotFor(location.world).around(location.x, location.y, location.z, searchRadius)

    /** The snapshot [nearbyMobs] would count from, refreshed the same way */
    fun snapshotFor(world: World): WorldMobs {
        val tick = Bukkit.getCurrentTick()
        return worldMobs[world.uid]?.takeIf { tick - it.tick < SNAPSHOT_REFRESH_TICKS }
            ?: scanWorld(world, tick).also { worldMobs[world.uid] = it }
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

    /** True when no spawn of [categories] could pass [filterAllowed] given [counts] */
    fun allCapped(categories: Set<SpawnCategory>, counts: Map<SpawnCategory, Int>): Boolean =
        categories.all { counts.getOrDefault(it, 0) >= caps.getOrDefault(it, defaultCapLimit) }

    private companion object {
        // 1 s at 20 TPS, 2 s at 10 TPS
        const val SNAPSHOT_REFRESH_TICKS = 20

        // 30 s at 20 TPS, so a category that resolves late is picked up again
        const val CATEGORY_REFRESH_TICKS = 600

        // The 30 s and 5 s lifetimes of the EntityTypeNames caches, misses retry sooner since MythicMobs attaches its data after entities load
        const val TYPE_REFRESH_TICKS = 600
        const val TYPE_MISS_REFRESH_TICKS = 100
    }
}
