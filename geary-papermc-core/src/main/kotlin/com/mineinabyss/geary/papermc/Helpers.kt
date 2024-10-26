package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.block.Block

context(Geary)
fun PrefabKey.toEntityOrNull() = entityOfOrNull(this)

inline fun <T> BukkitEntity.withGeary(run: Geary.() -> T) = with(world.toGeary()) { run() }

inline fun <T> Block.withGeary(run: Geary.() -> T) = with(world.toGeary()) { run() }
