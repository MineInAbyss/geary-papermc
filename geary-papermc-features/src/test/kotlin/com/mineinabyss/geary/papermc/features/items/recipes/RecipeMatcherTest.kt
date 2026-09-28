package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.bukkit.Material
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RecipeMatcherTest : RecipeTestBase() {
    private val stick = ingredient(Material.STICK)
    private val planks = ingredient(Material.OAK_PLANKS)

    private fun Map<Int, RecipeMatcher.SlotUse>.ingredients() = mapValues { it.value.ingredient }

    private fun Map<Int, RecipeMatcher.SlotUse>.amounts() = mapValues { it.value.amount }

    @Nested
    inner class Shaped {
        private val lShape = shaped("S", "SP", items = mapOf('S' to stick, 'P' to planks))

        @Test
        fun `matches at any offset, rejecting a stray item outside the shape`() {
            val matrix = arrayOf(
                null, null, null,
                null, stack(Material.STICK), null,
                null, stack(Material.STICK), stack(Material.OAK_PLANKS),
            )

            lShape.match(matrix).shouldNotBeNull().ingredients() shouldContainExactly mapOf(4 to stick, 7 to stick, 8 to planks)
            lShape.match(matrix.copyOf().also { it[0] = stack(Material.DIRT) }).shouldBeNull()
        }

        @Test
        fun `matches mirrored left to right, mapping each slot to its own ingredient, but not flipped upside down`() {
            val mirrored = arrayOf(
                null, stack(Material.STICK), null,
                stack(Material.OAK_PLANKS), stack(Material.STICK), null,
                null, null, null,
            )
            val flipped = arrayOf(
                stack(Material.STICK), stack(Material.OAK_PLANKS), null,
                stack(Material.STICK), null, null,
                null, null, null,
            )

            lShape.match(mirrored).shouldNotBeNull().ingredients() shouldContainExactly mapOf(1 to stick, 3 to planks, 4 to stick)
            lShape.match(flipped).shouldBeNull()
        }

        @Test
        fun `requires the ingredient amount in its slot`() {
            val recipe = shaped("S", items = mapOf('S' to ingredient(Material.STICK, amount = 2)))

            recipe.match(smallMatrix(stack(Material.STICK, 1))).shouldBeNull()
            recipe.match(smallMatrix(stack(Material.STICK, 5))).shouldNotBeNull().amounts() shouldContainExactly mapOf(0 to 2)
        }
    }

    @Nested
    inner class Shapeless {
        @Test
        fun `splits an amount over slots, draining earlier slots first`() {
            val sticks = ingredient(Material.STICK, amount = 3)
            val matrix = arrayOf(
                stack(Material.STICK, 2), null, stack(Material.OAK_PLANKS),
                null, stack(Material.STICK, 5), null,
                null, null, null,
            )

            val matched = shapeless(sticks, planks).match(matrix).shouldNotBeNull()
            matched.ingredients() shouldContainExactly mapOf(0 to sticks, 2 to planks, 4 to sticks)
            matched.amounts() shouldContainExactly mapOf(0 to 2, 2 to 1, 4 to 1)
        }

        @Test
        fun `rejects too few items, an unused slot or one item per ingredient spread wrongly`() {
            val recipe = shapeless(ingredient(Material.STICK, amount = 3), planks)

            recipe.match(smallMatrix(stack(Material.STICK, 2), stack(Material.OAK_PLANKS))).shouldBeNull()
            recipe.match(smallMatrix(stack(Material.STICK, 3), stack(Material.OAK_PLANKS), stack(Material.DIRT))).shouldBeNull()
            shapeless(stick).match(smallMatrix(stack(Material.STICK), stack(Material.STICK))).shouldBeNull()
            shapeless(stick, stick).match(smallMatrix(stack(Material.STICK, 2))).shouldBeNull()
        }

        @Test
        fun `backtracks when a greedy pick leaves a slot without an ingredient`() {
            val anyPlanks = predicateIngredient(Material.OAK_PLANKS) { it.type.name.endsWith("_PLANKS") }
            val oakOnly = ingredient(Material.OAK_PLANKS)

            val matched = shapeless(anyPlanks, oakOnly).match(smallMatrix(stack(Material.OAK_PLANKS), stack(Material.BIRCH_PLANKS)))

            matched.shouldNotBeNull().ingredients() shouldContainExactly mapOf(0 to oakOnly, 1 to anyPlanks)
        }
    }
}
