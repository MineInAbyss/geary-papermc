package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.withGeary
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Entity

fun BukkitEntity.toGeary(): GearyEntity = withGeary {
    return toGearyOrNull() ?: error("Entity $this is not being tracked by Geary!")
}

fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    withGeary { getAddon(EntityTracking).bukkit2Geary[this@toGearyOrNull] }

fun GearyEntity.toBukkit(): BukkitEntity? =
    with(world) { get(getAddon(EntityTracking).bukkitEntityComponent) as? BukkitEntity }

context(Geary)
@JvmName("toBukkitAndCast")
inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get(getAddon(EntityTracking).bukkitEntityComponent) as? T
