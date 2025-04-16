package com.mineinabyss.geary.papermc.scripting.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import org.bukkit.entity.LivingEntity

fun LivingEntity.nearbyEntities(radius: Int, vararg prefabs: String): Sequence<GearyEntity> {
    TODO()
}

fun LivingEntity.nearestEntity(radius: Int, vararg prefabs: String): GearyEntity? {
    TODO()
}
