package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.spawn_types.GearyReadSpawnCategoryEvent
import com.mineinabyss.geary.papermc.tracking.entities.toGearyOrNull
import com.mineinabyss.idofront.events.call
import io.lumine.mythic.bukkit.MythicBukkit
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.util.BoundingBox
import java.util.UUID
import java.util.function.Predicate
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
    val mobSpawnCategoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1.minutes.toJavaDuration())
        .maximumSize(1000)
        .build<String, String>()


    companion object {
        private val IGNORED_ENTITY_TYPES = ObjectOpenHashSet.of<EntityType>(
            EntityType.ITEM, EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, EntityType.ACACIA_BOAT, EntityType.BIRCH_BOAT,
            EntityType.CHERRY_BOAT, EntityType.DARK_OAK_BOAT, EntityType.JUNGLE_BOAT, EntityType.MANGROVE_BOAT,
            EntityType.OAK_BOAT, EntityType.PALE_OAK_BOAT, EntityType.SPRUCE_BOAT, EntityType.ACACIA_CHEST_BOAT,
            EntityType.BIRCH_CHEST_BOAT, EntityType.CHERRY_CHEST_BOAT, EntityType.DARK_OAK_CHEST_BOAT,
            EntityType.JUNGLE_CHEST_BOAT, EntityType.MANGROVE_CHEST_BOAT, EntityType.OAK_CHEST_BOAT,
            EntityType.PALE_OAK_CHEST_BOAT, EntityType.SPRUCE_CHEST_BOAT, EntityType.MINECART, EntityType.HOPPER_MINECART,
            EntityType.COMMAND_BLOCK_MINECART, EntityType.CHEST_MINECART, EntityType.FURNACE_MINECART,
            EntityType.SPAWNER_MINECART, EntityType.TNT_MINECART, EntityType.EXPERIENCE_ORB, EntityType.FALLING_BLOCK,
            EntityType.ARROW, EntityType.SPECTRAL_ARROW, EntityType.AREA_EFFECT_CLOUD, EntityType.INTERACTION,
            EntityType.BREEZE_WIND_CHARGE, EntityType.DRAGON_FIREBALL, EntityType.EGG, EntityType.ENDER_PEARL,
            EntityType.EYE_OF_ENDER, EntityType.EVOKER_FANGS, EntityType.EXPERIENCE_BOTTLE, EntityType.FIREBALL,
            EntityType.FIREWORK_ROCKET, EntityType.FISHING_BOBBER, EntityType.LEASH_KNOT, EntityType.LIGHTNING_BOLT,
            EntityType.LLAMA_SPIT, EntityType.OMINOUS_ITEM_SPAWNER, EntityType.TRIDENT, EntityType.SPLASH_POTION, EntityType.LINGERING_POTION,
            EntityType.SHULKER_BULLET, EntityType.SMALL_FIREBALL, EntityType.SNOWBALL, EntityType.WIND_CHARGE
        )
    }


    private val entityCategoryCache = CacheBuilder.newBuilder()
        .expireAfterWrite(30.seconds.toJavaDuration())
        .build<UUID, SpawnCategory>()

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

    fun calculateCategoriesNear(location: Location): Map<SpawnCategory, Int> {
        val boundingBox = BoundingBox.of(location, searchRadius.toDouble(), searchRadius.toDouble(), searchRadius.toDouble())
        val entities = location.world.getNearbyEntities(boundingBox) { it.type !in IGNORED_ENTITY_TYPES }
        return entities.groupingBy {
            //GearyReadSpawnCategoryEvent(it).also { it.call() }.category ?: SpawnCategory.of(it)
            getCategory(it)
        }.eachCount()
    }

    fun filterAllowedAt(location: Location, spawns: List<SpawnEntry>, predicate: Predicate<SpawnEntry>): List<SpawnEntry> {
        val mobCaps = calculateCategoriesNear(location)
        val defaultLimit = this.defaultCapLimit

        return spawns.filter { spawn ->
            val category = spawn.type.category
            val currentCount = mobCaps.getOrDefault(category, 0)
            val limit = caps.getOrDefault(category, defaultLimit)
            currentCount < limit && predicate.test(spawn)
        }
    }

    fun getAllowedCategoriesAt(location: Location): List<SpawnCategory> {
        val mobCaps = calculateCategoriesNear(location)
        return caps
            .filter { (category, cap) -> mobCaps.getOrDefault(category, 0) < cap }
            .keys.toList()
    }

//    fun isCategoryAllowedAt(location: Location, category: String): Boolean {
//        val mobCaps = calculateCategoriesNear(location)
//        return mobCaps.getOrDefault(category, 0) < caps.getOrDefault(category, 0)
//    }
}
