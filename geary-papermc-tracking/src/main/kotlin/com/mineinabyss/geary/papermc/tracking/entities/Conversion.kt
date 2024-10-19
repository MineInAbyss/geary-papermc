package com.mineinabyss.geary.papermc.tracking.entities

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.entity.Entity

context(Geary)
fun BukkitEntity.toGeary(): GearyEntity {
    return toGearyOrNull() ?: error("Entity $this is not being tracked by Geary!")
}

context(Geary)
fun BukkitEntity.toGearyOrNull(): GearyEntity? =
    getAddon(EntityTracking).bukkit2Geary[this]

context(Geary)
fun GearyEntity.toBukkit(): BukkitEntity? = get(getAddon(EntityTracking).bukkitEntityComponent) as? BukkitEntity

context(Geary)
@JvmName("toBukkitAndCast")
inline fun <reified T : Entity> GearyEntity.toBukkit(): T? =
    get(getAddon(EntityTracking).bukkitEntityComponent) as? T
