package com.mineinabyss.geary.papermc

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.idofront.typealiases.BukkitEntity
import org.bukkit.block.Block
import org.bukkit.block.TileState

context(Geary)
fun PrefabKey.toEntityOrNull() = entityOfOrNull(this)

inline fun <T, R : BukkitEntity> R.withGeary(run: Geary.(R) -> T) = with(world.toGeary()) { run(this@withGeary) }

inline fun <T, R : TileState> R.withGeary(run: Geary.(R) -> T) = with(world.toGeary()) { run(this@withGeary) }

inline fun <T, R : Block> R.withGeary(run: Geary.(R) -> T) = with(world.toGeary()) { run(this@withGeary) }
