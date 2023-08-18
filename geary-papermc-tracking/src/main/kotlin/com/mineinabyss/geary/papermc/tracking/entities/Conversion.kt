package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Entity

fun BukkitEntity.toGeary(): GearyEntity {
    return toGearyOrNull() ?: error("Entity $this is not being tracked by Geary!")
}

fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    gearyMobs.bukkit2Geary[this]

fun GearyEntity.toBukkit(): BukkitEntity? = get(gearyMobs.bukkitEntityComponent) as? BukkitEntity

@JvmName("toBukkitAndCast")
inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get(gearyMobs.bukkitEntityComponent) as? T
