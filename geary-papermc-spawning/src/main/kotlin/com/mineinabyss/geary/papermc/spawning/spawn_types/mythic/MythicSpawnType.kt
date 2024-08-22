package com.mineinabyss.geary.papermc.spawning.spawn_types.mythic

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftEntityType
import kotlin.jvm.optionals.getOrNull

class MythicSpawnType(
    override val key: String,
    mobName: String,
) : SpawnType {
    val mythicMob = MythicBukkit.inst().mobManager.getMythicMob(mobName).getOrNull()
        ?: error("Mythic mob $mobName not found")

    override fun spawnAt(location: Location) {
        val spawned = mythicMob.spawn(BukkitAdapter.adapt(location), 1.0)
        spawned.entity.bukkitEntity.persistentDataContainer
    }

    override val category: SpawnCategory = SpawnCategory(
        mythicMob.config.getString("SpawnCategory")
            ?: SpawnCategory.of(CraftEntityType.stringToBukkit(mythicMob.entityType.name))
    )
}
