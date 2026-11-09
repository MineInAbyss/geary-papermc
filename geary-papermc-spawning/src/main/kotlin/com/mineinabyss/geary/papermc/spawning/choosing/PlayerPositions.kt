package com.mineinabyss.geary.papermc.spawning.choosing

import com.mineinabyss.idofront.destructure.component1
import com.mineinabyss.idofront.destructure.component2
import com.mineinabyss.idofront.destructure.component3
import it.unimi.dsi.fastutil.doubles.DoubleArrayList
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.IdentityHashMap
import kotlin.math.floor
import kotlin.math.sqrt

/** Positions of [players] read once, only valid during the spawn run that created it */
class PlayerPositions() {
    // Worlds are one instance per loaded world, so identity avoids CraftWorld.equals.
    // Long.hashCode of a packed cell key is cellX xor cellZ, which collides along diagonals, fastutil mixes the bits
    private val worlds = IdentityHashMap<World, Long2ObjectOpenHashMap<DoubleArrayList>>()

    constructor(players: List<Player>) : this() {
        val scratch = Location(null, 0.0, 0.0, 0.0)
        for (player in players) add(player.getLocation(scratch)!!)
    }

    private fun add(location: Location) {
        add(location.world ?: return, location.x, location.y, location.z)
    }

    fun add(world: World, x: Double, y: Double, z: Double) {
        val cells = worlds.getOrPut(world) { Long2ObjectOpenHashMap() }
        val key = cellKey(cellOf(x), cellOf(z))
        val coords = cells.get(key) ?: DoubleArrayList().also { cells.put(key, it) }

        coords.add(x)
        coords.add(y)
        coords.add(z)
    }

    fun anyWithin(location: Location, distanceSq: Double): Boolean {
        val cells = worlds[location.world ?: return false] ?: return false
        val (x, y, z) = location
        val r = sqrt(distanceSq)

        val zRange = cellOf(z - r)..cellOf(z + r)
        for (cellX in cellOf(x - r)..cellOf(x + r)) for (cellZ in zRange) {
            val coords = cells.get(cellKey(cellX, cellZ)) ?: continue
            for (i in coords.indices step 3) {
                val dx = coords.getDouble(i) - x
                val dy = coords.getDouble(i + 1) - y
                val dz = coords.getDouble(i + 2) - z

                if (dx * dx + dy * dy + dz * dz < distanceSq) return true
            }
        }

        return false
    }

    private companion object {
        const val CELL_SIZE = 32.0

        fun cellOf(coord: Double) = floor(coord / CELL_SIZE).toInt()

        fun cellKey(cellX: Int, cellZ: Int) = (cellX.toLong() shl 32) or (cellZ.toLong() and 0xFFFFFFFFL)
    }
}
