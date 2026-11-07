package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import it.unimi.dsi.fastutil.doubles.DoubleArrayList
import java.util.UUID
import kotlin.math.abs
import kotlin.math.floor

/**
 * Non-player mobs of one [world] at [tick], plus every spawn recorded since.
 * Holds no world or entity references so a snapshot of an unloaded world is just a few stale numbers
 */
class WorldMobs(val world: UUID, val tick: Int) {
    private class Cell {
        // Flat x, y, z triples, indexed alongside categories and types
        val coords = DoubleArrayList()
        val categories = ArrayList<SpawnCategory>()
        val types = ArrayList<String?>()
    }

    private val cells = HashMap<Long, Cell>()

    fun add(type: String?, category: SpawnCategory, x: Double, y: Double, z: Double) {
        val cell = cells.getOrPut(cellKey(cellOf(x), cellOf(z))) { Cell() }
        cell.coords.add(x)
        cell.coords.add(y)
        cell.coords.add(z)
        cell.categories.add(category)
        cell.types.add(type)
    }

    fun around(x: Double, y: Double, z: Double, radius: Double): NearbyMobs {
        val counts = HashMap<SpawnCategory, Int>()
        forEachNear(x, y, z, radius) { cell, i -> counts.merge(cell.categories[i], 1, Int::plus) }
        return NearbyMobs(this, counts)
    }

    fun countNear(types: Collection<String>, x: Double, y: Double, z: Double, radius: Double, stopAt: Int): Int {
        var count = 0
        forEachNear(x, y, z, radius) { cell, i ->
            val type = cell.types[i]
            if (type != null && type in types && ++count >= stopAt) return count
        }
        return count
    }

    private inline fun forEachNear(x: Double, y: Double, z: Double, radius: Double, action: (Cell, Int) -> Unit) {
        for (cellX in cellOf(x - radius)..cellOf(x + radius)) {
            for (cellZ in cellOf(z - radius)..cellOf(z + radius)) {
                val cell = cells[cellKey(cellX, cellZ)] ?: continue
                val coords = cell.coords
                for (i in cell.categories.indices) {
                    if (abs(coords.getDouble(i * 3) - x) <= radius &&
                        abs(coords.getDouble(i * 3 + 1) - y) <= radius &&
                        abs(coords.getDouble(i * 3 + 2) - z) <= radius
                    ) action(cell, i)
                }
            }
        }
    }

    private fun cellOf(coord: Double): Int = floor(coord).toInt() shr CELL_SHIFT

    private fun cellKey(cellX: Int, cellZ: Int): Long = (cellX.toLong() shl 32) or (cellZ.toLong() and 0xFFFFFFFFL)

    private companion object {
        // 64 block columns, so a 128 to 220 block cap radius visits about 25 to 64 cells
        const val CELL_SHIFT = 6
    }
}
