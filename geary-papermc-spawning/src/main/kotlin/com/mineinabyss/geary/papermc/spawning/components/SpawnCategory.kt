package com.mineinabyss.geary.papermc.spawning.components

import com.mineinabyss.idofront.typealiases.BukkitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("geary:spawn_category")
@JvmInline
value class SpawnCategory(val category: String) {
    companion object {
        fun of(entity: BukkitEntity) = SpawnCategory(entity.spawnCategory.name.lowercase())
    }
}
