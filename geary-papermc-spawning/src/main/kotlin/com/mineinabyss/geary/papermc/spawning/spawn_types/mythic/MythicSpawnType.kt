package com.mineinabyss.geary.papermc.spawning.spawn_types.mythic

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import com.mineinabyss.idofront.typealiases.BukkitEntity
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import net.minecraft.world.entity.EntityDimensions
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftEntityType
import org.bukkit.util.BoundingBox
import kotlin.jvm.optionals.getOrNull

class MythicSpawnType(
    override val key: String,
    mobName: String,
) : SpawnType {
    // Mobs declaring a Template are registered in a later pass than plain ones, so they are missing from
    // MythicMobs' registry while we read spawn configs on enable, and may only be resolved once the server is up
    val mythicMob by lazy {
        MythicBukkit.inst().mobManager.getMythicMob(mobName).getOrNull()
            ?: error("Mythic mob $mobName not found")
    }

    private val bukkitEntityType by lazy { CraftEntityType.stringToBukkit(mythicMob.entityType.name) }

    // Null when MythicMobs uses an entity name Bukkit cannot map, e.g. BABY_ZOMBIE
    private val dimensions: EntityDimensions? by lazy {
        runCatching { CraftEntityType.bukkitToMinecraft(bukkitEntityType).dimensions }.getOrNull()
    }

    override fun spawnAt(location: Location): BukkitEntity {
        val spawned = mythicMob.spawn(BukkitAdapter.adapt(location), 1.0)
        return spawned.entity.bukkitEntity
    }

    override fun boundingBoxAt(location: Location): BoundingBox? {
        val dims = dimensions ?: return null
        val halfWidth = dims.width() / 2.0
        return BoundingBox(
            location.x - halfWidth, location.y, location.z - halfWidth,
            location.x + halfWidth, location.y + dims.height(), location.z + halfWidth,
        )
    }

    override val category: SpawnCategory by lazy {
        SpawnCategory(
            mythicMob.config.getString("SpawnCategory")
                ?: SpawnCategory.of(bukkitEntityType)
        )
    }
}
