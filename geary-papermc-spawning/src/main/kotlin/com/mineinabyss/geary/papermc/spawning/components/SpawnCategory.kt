package com.mineinabyss.geary.papermc.spawning.components

import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.Serializable
import org.bukkit.craftbukkit.entity.CraftEntityType
import org.bukkit.craftbukkit.util.CraftSpawnCategory
import org.bukkit.entity.EntityType

@Serializable
@JvmInline
value class SpawnCategory(val category: String) {
    companion object {
        fun of(entity: BukkitEntity) = SpawnCategory(entity.spawnCategory.name.lowercase())

        fun of(entityType: EntityType) =
            CraftSpawnCategory.toBukkit(CraftEntityType.bukkitToMinecraft(entityType).category).name.lowercase()
    }
}
