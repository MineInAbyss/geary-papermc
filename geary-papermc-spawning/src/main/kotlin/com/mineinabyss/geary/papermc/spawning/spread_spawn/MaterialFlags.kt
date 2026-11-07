package com.mineinabyss.geary.papermc.spawning.spread_spawn

import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.block.data.Waterlogged

// Block.isPassable needs a live block, so scans use Material.isCollidable instead.
// SpawnPositionReader still validates every spot before it is used
class MaterialFlags {
    private val flags = ByteArray(Material.entries.size)
    private val waterloggable = BooleanArray(Material.entries.size)

    init {
        for (material in Material.entries) {
            @Suppress("DEPRECATION")
            if (!material.isBlock || material.isLegacy) continue
            var f = 0
            if (material.isSolid || material == Material.SNOW) f = f or SurfaceFlags.FLOOR
            if (!material.isCollidable) f = f or SurfaceFlags.PASSABLE
            if (material == Material.WATER || material == Material.LAVA) f = f or SurfaceFlags.LIQUID
            flags[material.ordinal] = f.toByte()
            waterloggable[material.ordinal] = material.createBlockData() is Waterlogged
        }
    }

    fun of(material: Material, waterlogged: Boolean): Int {
        val f = flags[material.ordinal].toInt()
        return if (waterlogged) f or SurfaceFlags.LIQUID else f
    }

    fun flagsAt(snapshot: ChunkSnapshot, x: Int, y: Int, z: Int): Int {
        val material = snapshot.getBlockType(x, y, z)
        if (!waterloggable[material.ordinal]) return of(material, false)
        val data = snapshot.getBlockData(x, y, z)
        return of(material, data is Waterlogged && data.isWaterlogged)
    }
}
