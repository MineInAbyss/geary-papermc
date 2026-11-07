package com.mineinabyss.geary.papermc.spawning.choosing

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

/** Positions of [players] read once, only valid during the spawn run that created it */
class PlayerPositions(players: List<Player>) {
    private val worlds = arrayOfNulls<World>(players.size)
    private val coords = DoubleArray(players.size * 3)

    init {
        val scratch = Location(null, 0.0, 0.0, 0.0)
        players.forEachIndexed { i, player ->
            player.getLocation(scratch)
            worlds[i] = scratch.world
            coords[i * 3] = scratch.x
            coords[i * 3 + 1] = scratch.y
            coords[i * 3 + 2] = scratch.z
        }
    }

    fun anyWithin(location: Location, distanceSq: Double): Boolean {
        val world = location.world
        val x = location.x
        val y = location.y
        val z = location.z
        for (i in worlds.indices) {
            if (worlds[i] != world) continue
            val dx = coords[i * 3] - x
            val dy = coords[i * 3 + 1] - y
            val dz = coords[i * 3 + 2] - z
            if (dx * dx + dy * dy + dz * dz < distanceSq) return true
        }
        return false
    }
}
