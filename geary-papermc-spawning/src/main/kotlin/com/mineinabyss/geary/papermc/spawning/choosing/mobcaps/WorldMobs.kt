package com.mineinabyss.geary.papermc.spawning.choosing.mobcaps

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.UUID
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Non-player mobs of one [world] at [tick], plus every spawn recorded since.
 * Holds no world or entity references so a snapshot of an unloaded world is just a few stale numbers
 */
class WorldMobs(val world: UUID, val tick: Int) {
    private class Part {
        var size = 0

        // Flat x, y, z triples, indexed alongside categories and types
        var coords = DoubleArray(3 * INITIAL_PART_CAPACITY)

        // Indexes into categoryList
        var categories = IntArray(INITIAL_PART_CAPACITY)
        var types = arrayOfNulls<String>(INITIAL_PART_CAPACITY)

        // Indexed by category id, may be shorter than categoryList
        var categoryCounts = IntArray(0)
        var minY = Double.POSITIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY

        fun add(type: String?, id: Int, categoryCount: Int, x: Double, y: Double, z: Double) {
            if (size == categories.size) {
                coords = coords.copyOf(coords.size * 2)
                categories = categories.copyOf(categories.size * 2)
                types = types.copyOf(types.size * 2)
            }
            coords[size * 3] = x
            coords[size * 3 + 1] = y
            coords[size * 3 + 2] = z
            categories[size] = id
            types[size] = type
            size++
            if (id >= categoryCounts.size) categoryCounts = categoryCounts.copyOf(categoryCount)
            categoryCounts[id]++
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }
    }

    private class Slab {
        // Indexed by partOf
        val parts = arrayOfNulls<Part>(PARTS_PER_SIDE * PARTS_PER_SIDE)

        // Indexed by category id, may be shorter than categoryList
        var categoryCounts = IntArray(0)
        var minY = Double.POSITIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY
    }

    private class Cell {
        // slabs[i] holds the mobs of slab index slabBase + i
        var slabBase = 0
        var slabs = arrayOfNulls<Slab>(0)

        fun slabFor(slab: Int): Slab {
            if (slabs.isEmpty()) {
                slabBase = slab
                slabs = arrayOfNulls(1)
            } else if (slab < slabBase) {
                val grown = arrayOfNulls<Slab>(slabs.size + slabBase - slab)
                System.arraycopy(slabs, 0, grown, slabBase - slab, slabs.size)
                slabs = grown
                slabBase = slab
            } else if (slab >= slabBase + slabs.size) {
                slabs = slabs.copyOf(slab - slabBase + 1)
            }
            return slabs[slab - slabBase] ?: Slab().also { slabs[slab - slabBase] = it }
        }
    }

    private val cells = Long2ObjectOpenHashMap<Cell>()
    private val categoryIds = Object2IntOpenHashMap<SpawnCategory>().apply { defaultReturnValue(-1) }
    private val categoryList = ArrayList<SpawnCategory>()

    // While deferring, adds wait here so other threads can read the cells, see deferAdds
    private var deferring = false
    private var pendingSize = 0
    private var pendingCoords = DoubleArray(0)
    private var pendingCategories = arrayOfNulls<SpawnCategory>(0)
    private var pendingTypes = arrayOfNulls<String>(0)

    fun add(type: String?, category: SpawnCategory, x: Double, y: Double, z: Double) {
        if (deferring) {
            addPending(type, category, x, y, z)
            return
        }
        addToCells(type, category, x, y, z)
    }

    /**
     * Until [applyDeferredAdds], adds only touch a pending list read on the calling thread, so [cellCounts] can run on other threads meanwhile.
     * Main thread only, like [add]
     */
    fun deferAdds() {
        deferring = true
    }

    /** Callers must make sure no [cellCounts] call is still running */
    fun applyDeferredAdds() {
        deferring = false
        for (i in 0 until pendingSize) {
            addToCells(pendingTypes[i], pendingCategories[i]!!, pendingCoords[i * 3], pendingCoords[i * 3 + 1], pendingCoords[i * 3 + 2])
            pendingTypes[i] = null
            pendingCategories[i] = null
        }
        pendingSize = 0
    }

    private fun addPending(type: String?, category: SpawnCategory, x: Double, y: Double, z: Double) {
        if (pendingSize == pendingCategories.size) {
            val capacity = maxOf(INITIAL_PART_CAPACITY, pendingSize * 2)
            pendingCoords = pendingCoords.copyOf(capacity * 3)
            pendingCategories = pendingCategories.copyOf(capacity)
            pendingTypes = pendingTypes.copyOf(capacity)
        }
        pendingCoords[pendingSize * 3] = x
        pendingCoords[pendingSize * 3 + 1] = y
        pendingCoords[pendingSize * 3 + 2] = z
        pendingCategories[pendingSize] = category
        pendingTypes[pendingSize] = type
        pendingSize++
    }

    private fun pendingInCube(i: Int, x: Double, y: Double, z: Double, radius: Double): Boolean =
        abs(pendingCoords[i * 3] - x) <= radius &&
            abs(pendingCoords[i * 3 + 1] - y) <= radius &&
            abs(pendingCoords[i * 3 + 2] - z) <= radius

    private fun addToCells(type: String?, category: SpawnCategory, x: Double, y: Double, z: Double) {
        val key = cellKey(cellOf(x), cellOf(z))
        val cell = cells.get(key) ?: Cell().also { cells.put(key, it) }
        var id = categoryIds.getInt(category)
        if (id < 0) {
            id = categoryList.size
            categoryList.add(category)
            categoryIds.put(category, id)
        }
        val slab = cell.slabFor(slabOf(y))
        val partIndex = partOf(x, z)
        val part = slab.parts[partIndex] ?: Part().also { slab.parts[partIndex] = it }
        part.add(type, id, categoryList.size, x, y, z)
        if (id >= slab.categoryCounts.size) slab.categoryCounts = slab.categoryCounts.copyOf(categoryList.size)
        slab.categoryCounts[id]++
        if (y < slab.minY) slab.minY = y
        if (y > slab.maxY) slab.maxY = y
    }

    fun around(x: Double, y: Double, z: Double, radius: Double): NearbyMobs =
        nearby(cellCounts(x, y, z, radius), x, y, z, radius)

    /** [cellCounts] for the same cube plus the deferred adds inside it, which together equal [around] */
    fun nearby(cellCounts: HashMap<SpawnCategory, Int>, x: Double, y: Double, z: Double, radius: Double): NearbyMobs {
        for (i in 0 until pendingSize) {
            if (pendingInCube(i, x, y, z, radius)) cellCounts.merge(pendingCategories[i]!!, 1, Int::plus)
        }
        return NearbyMobs(this, cellCounts)
    }

    /** Cap counts without deferred adds. Only reads, so any thread may call it while adds are deferred */
    fun cellCounts(x: Double, y: Double, z: Double, radius: Double): HashMap<SpawnCategory, Int> {
        val perCategory = IntArray(categoryList.size)
        // Whole slabs are only counted when clear of the cube faces by EDGE_MARGIN, so rounding in the per mob test cannot disagree with them
        val minX = x - radius + EDGE_MARGIN
        val maxX = x + radius - EDGE_MARGIN
        val minY = y - radius + EDGE_MARGIN
        val maxY = y + radius - EDGE_MARGIN
        val minZ = z - radius + EDGE_MARGIN
        val maxZ = z + radius - EDGE_MARGIN
        val lowSlab = slabOf(y - radius)
        val highSlab = slabOf(y + radius)
        for (cellX in cellOf(x - radius)..cellOf(x + radius)) {
            val cellMinX = (cellX shl CELL_SHIFT).toDouble()
            val insideX = cellMinX >= minX && cellMinX + CELL_SIZE <= maxX
            for (cellZ in cellOf(z - radius)..cellOf(z + radius)) {
                val cell = cells[cellKey(cellX, cellZ)] ?: continue
                val cellMinZ = (cellZ shl CELL_SHIFT).toDouble()
                val insideXZ = insideX && cellMinZ >= minZ && cellMinZ + CELL_SIZE <= maxZ
                val slabs = cell.slabs
                val from = max(lowSlab - cell.slabBase, 0)
                val to = min(highSlab - cell.slabBase, slabs.size - 1)
                for (s in from..to) {
                    val slab = slabs[s] ?: continue
                    if (insideXZ && slab.minY >= minY && slab.maxY <= maxY) {
                        val counts = slab.categoryCounts
                        for (id in counts.indices) perCategory[id] += counts[id]
                        continue
                    }
                    val parts = slab.parts
                    for (p in parts.indices) {
                        val part = parts[p] ?: continue
                        val partMinX = cellMinX + partX(p) * PART_SIZE
                        val partMinZ = cellMinZ + partZ(p) * PART_SIZE
                        if (part.minY >= minY && part.maxY <= maxY &&
                            partMinX >= minX && partMinX + PART_SIZE <= maxX &&
                            partMinZ >= minZ && partMinZ + PART_SIZE <= maxZ
                        ) {
                            val counts = part.categoryCounts
                            for (id in counts.indices) perCategory[id] += counts[id]
                            continue
                        }
                        if (part.outside(partMinX, partMinZ, x, y, z, radius)) continue
                        val coords = part.coords
                        val categories = part.categories
                        for (i in 0 until part.size) {
                            if (abs(coords[i * 3] - x) <= radius &&
                                abs(coords[i * 3 + 1] - y) <= radius &&
                                abs(coords[i * 3 + 2] - z) <= radius
                            ) perCategory[categories[i]]++
                        }
                    }
                }
            }
        }
        val counts = HashMap<SpawnCategory, Int>()
        for (id in perCategory.indices) if (perCategory[id] > 0) counts[categoryList[id]] = perCategory[id]
        return counts
    }

    fun countNear(types: Collection<String>, x: Double, y: Double, z: Double, radius: Double, stopAt: Int): Int {
        var count = 0
        for (i in 0 until pendingSize) {
            val type = pendingTypes[i]
            if (type != null && type in types && pendingInCube(i, x, y, z, radius) && ++count >= stopAt) return count
        }
        forEachNear(x, y, z, radius) { part, i ->
            val type = part.types[i]
            if (type != null && type in types && ++count >= stopAt) return count
        }
        return count
    }

    private inline fun forEachNear(x: Double, y: Double, z: Double, radius: Double, action: (Part, Int) -> Unit) {
        val lowSlab = slabOf(y - radius)
        val highSlab = slabOf(y + radius)
        for (cellX in cellOf(x - radius)..cellOf(x + radius)) {
            val cellMinX = (cellX shl CELL_SHIFT).toDouble()
            for (cellZ in cellOf(z - radius)..cellOf(z + radius)) {
                val cell = cells[cellKey(cellX, cellZ)] ?: continue
                val cellMinZ = (cellZ shl CELL_SHIFT).toDouble()
                val slabs = cell.slabs
                for (s in max(lowSlab - cell.slabBase, 0)..min(highSlab - cell.slabBase, slabs.size - 1)) {
                    val parts = (slabs[s] ?: continue).parts
                    for (p in parts.indices) {
                        val part = parts[p] ?: continue
                        if (part.outside(cellMinX + partX(p) * PART_SIZE, cellMinZ + partZ(p) * PART_SIZE, x, y, z, radius)) continue
                        val coords = part.coords
                        for (i in 0 until part.size) {
                            if (abs(coords[i * 3] - x) <= radius &&
                                abs(coords[i * 3 + 1] - y) <= radius &&
                                abs(coords[i * 3 + 2] - z) <= radius
                            ) action(part, i)
                        }
                    }
                }
            }
        }
    }

    // Only true when every mob in the part fails the per mob test, mobs lie in [partMin, partMin + PART_SIZE) horizontally
    private fun Part.outside(partMinX: Double, partMinZ: Double, x: Double, y: Double, z: Double, radius: Double): Boolean {
        val reach = radius + EDGE_MARGIN
        return partMinX > x + reach || partMinX + PART_SIZE < x - reach ||
            partMinZ > z + reach || partMinZ + PART_SIZE < z - reach ||
            minY > y + reach || maxY < y - reach
    }

    private fun cellOf(coord: Double): Int = floor(coord).toInt() shr CELL_SHIFT

    private fun slabOf(coord: Double): Int = floor(coord).toInt() shr SLAB_SHIFT

    private fun partOf(x: Double, z: Double): Int =
        ((floor(x).toInt() shr PART_SHIFT) and PART_MASK) or (((floor(z).toInt() shr PART_SHIFT) and PART_MASK) shl PART_BITS)

    private fun partX(part: Int): Int = part and PART_MASK

    private fun partZ(part: Int): Int = part shr PART_BITS

    private fun cellKey(cellX: Int, cellZ: Int): Long = (cellX.toLong() shl 32) or (cellZ.toLong() and 0xFFFFFFFFL)

    private companion object {
        // 64 block columns split into 16 block slabs: a 220 block cap radius visits about 64 columns.
        // Only slabs on the cube's faces are looked at per part, the rest are counted whole, even when a column's mobs span more height than the cube
        const val CELL_SHIFT = 6
        const val CELL_SIZE = (1 shl CELL_SHIFT).toDouble()
        const val SLAB_SHIFT = 4

        // Slabs on the cube's sides count their 16 block parts whole where they fit, so only parts the faces cut through scan mobs
        const val PART_SHIFT = 4
        const val PART_BITS = CELL_SHIFT - PART_SHIFT
        const val PARTS_PER_SIDE = 1 shl PART_BITS
        const val PART_MASK = PARTS_PER_SIDE - 1
        const val PART_SIZE = (1 shl PART_SHIFT).toDouble()
        const val INITIAL_PART_CAPACITY = 4
        const val EDGE_MARGIN = 1e-3
    }
}
