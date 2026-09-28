package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.idofront.util.associateFastNotNull
import com.mineinabyss.idofront.util.associateFastWith
import org.bukkit.inventory.ItemStack

internal object RecipeMatcher {
    const val GRID_WIDTH = 3
    private const val SMALL_GRID_WIDTH = 2

    data class SlotUse(val ingredient: RecipeIngredient, val amount: Int)

    fun filledSlots(matrix: Array<ItemStack?>): List<Int> = matrix.indices.filterNot { matrix[it].isNullOrEmpty() }

    internal fun <T : Any> grid(shape: List<String>, at: (Char) -> T?): List<T?> = (0 until GRID_WIDTH).flatMap { row ->
        (0 until GRID_WIDTH).map { column -> shape.getOrNull(row)?.getOrNull(column)?.let(at) }
    }

    internal class Shape(private val grid: List<RecipeIngredient?>) {
        private val occupied = grid.indices.filter { grid[it] != null }
        private val top = occupied.minOfOrNull { it / GRID_WIDTH } ?: 0
        private val left = occupied.minOfOrNull { it % GRID_WIDTH } ?: 0
        val height = occupied.maxOfOrNull { it / GRID_WIDTH }?.let { it - top + 1 } ?: 0
        val width = occupied.maxOfOrNull { it % GRID_WIDTH }?.let { it - left + 1 } ?: 0

        fun ingredientAt(row: Int, column: Int, mirrored: Boolean): RecipeIngredient? {
            val shapeColumn = if (mirrored) width - 1 - column else column
            return grid[(top + row) * GRID_WIDTH + left + shapeColumn]
        }
    }

    internal fun matchShaped(shape: Shape, matrix: Array<ItemStack?>): Map<Int, SlotUse>? {
        val width = if (matrix.size == GRID_WIDTH * GRID_WIDTH) GRID_WIDTH else SMALL_GRID_WIDTH
        val height = matrix.size / width
        if (shape.width == 0 || shape.width > width || shape.height > height) return null

        for (mirrored in listOf(false, true)) for (rowOffset in 0..height - shape.height) for (columnOffset in 0..width - shape.width)
            matchShapedAt(shape, matrix, width, rowOffset, columnOffset, mirrored)?.let { return it }

        return null
    }

    private fun matchShapedAt(shape: Shape, matrix: Array<ItemStack?>, width: Int, rowOffset: Int, columnOffset: Int, mirrored: Boolean): Map<Int, SlotUse>? {
        val matched = HashMap<Int, SlotUse>()
        for (slot in matrix.indices) {
            val row = slot / width - rowOffset
            val column = slot % width - columnOffset
            val inShape = row in 0 until shape.height && column in 0 until shape.width
            val ingredient = if (inShape) shape.ingredientAt(row, column, mirrored) else null

            when (ingredient) {
                null -> if (!matrix[slot].isNullOrEmpty()) return null
                else -> {
                    if (!ingredient.canUse(matrix[slot])) return null
                    matched[slot] = SlotUse(ingredient, ingredient.amount)
                }
            }
        }
        return matched
    }

    internal fun matchShapeless(ingredients: List<RecipeIngredient>, matrix: Array<ItemStack?>): Map<Int, SlotUse>? {
        val slots = filledSlots(matrix)
        if (slots.size < ingredients.size) return null

        val owner = IntArray(matrix.size) { -1 }
        val slotCount = IntArray(ingredients.size)
        val itemCount = IntArray(ingredients.size)
        val usable = slots.map { slot -> ingredients.indices.filter { ingredients[it].canUse(matrix[slot], ignoreAmount = true) } }
        val itemsFrom = IntArray(slots.size + 1).also { left ->
            for (index in slots.indices.reversed()) left[index] = left[index + 1] + matrix[slots[index]]!!.amount
        }

        // Prunes branches that can no longer give every ingredient a slot and its amount, the search is exponential otherwise
        fun canStillSatisfy(index: Int): Boolean {
            val unassigned = ingredients.indices.count { slotCount[it] == 0 }
            val missing = ingredients.indices.sumOf { maxOf(0, ingredients[it].amount - itemCount[it]) }
            return unassigned <= slots.size - index && missing <= itemsFrom[index]
        }

        fun assign(index: Int): Boolean {
            if (!canStillSatisfy(index)) return false
            if (index == slots.size) return true

            val slot = slots[index]
            val amount = matrix[slot]!!.amount
            for (i in usable[index]) {
                if (slotCount[i] >= ingredients[i].amount) continue
                owner[slot] = i
                slotCount[i]++
                itemCount[i] += amount
                if (assign(index + 1)) return true
                itemCount[i] -= amount
                slotCount[i]--
                owner[slot] = -1
            }
            return false
        }

        if (!assign(0)) return null

        return buildMap {
            ingredients.forEachIndexed { i, ingredient ->
                var remaining = ingredient.amount - slotCount[i]
                slots.filter { owner[it] == i }.forEach { slot ->
                    val extra = minOf(remaining, matrix[slot]!!.amount - 1)
                    remaining -= extra
                    put(slot, SlotUse(ingredient, 1 + extra))
                }
            }
        }
    }
}
