package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Entity

fun BukkitEntity.toGeary(): GearyEntity {
    return entityTracking.bukkit2Geary.getOrCreate(this@toGeary)
}

fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    entityTracking.bukkit2Geary[this]

fun GearyEntity.toBukkit(): BukkitEntity? = get()

@JvmName("toBukkitAndCast")
inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get<Entity>() as? T
