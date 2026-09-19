package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.google.common.cache.CacheBuilder
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.Location
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

    // Counts are shared by everyone in the same 16x16x16 section while they stay fresh,
    // and callers bump them as they spawn so the cap still holds between scans
    private val sectionCountsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(2.seconds.toJavaDuration())
        .build<SectionKey, MutableMap<SpawnCategory, Int>>()

    private data class SectionKey(val world: UUID, val x: Int, val y: Int, val z: Int)

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

    fun calculateCategoriesNear(location: Location): MutableMap<SpawnCategory, Int> {
        val radius = searchRadius.toDouble()
        val counts = HashMap<SpawnCategory, Int>()
        for (entity in location.world.getNearbyLivingEntities(location, radius, radius, radius)) {
            if (entity is Player) continue
            counts.merge(getCategory(entity), 1, Int::plus)
        }
        return counts
    }

    fun countsNear(location: Location): MutableMap<SpawnCategory, Int> {
        val key = SectionKey(location.world.uid, location.blockX shr 4, location.blockY shr 4, location.blockZ shr 4)
        return sectionCountsCache.get(key) { calculateCategoriesNear(location) }
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
}
