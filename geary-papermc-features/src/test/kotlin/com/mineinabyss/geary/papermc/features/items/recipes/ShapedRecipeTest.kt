package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.bukkit.Material
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.recipe.CraftingBookCategory
import org.junit.jupiter.api.Test

class ShapedRecipeTest : RecipeServerTest() {
    @Test
    fun `lays its shape out on the grid and hands vanilla its category and amounts`() {
        val loaded = decodeRecipe("{ type: shaped, items: { K: { type: minecraft:stick, amount: 2 }, M: minecraft:oak_planks }, shape: [ ' K', 'KM' ] }")
            .load(context(category = "equipment")).shouldNotBeNull()
        val grid = loaded.custom.shouldBeInstanceOf<ShapedCustomRecipe>().grid
        val recipe = loaded.recipe.shouldBeInstanceOf<ShapedRecipe>()

        grid.indices.filter { grid[it] != null } shouldContainExactly listOf(1, 3, 4)
        grid[1] shouldBeSameInstanceAs grid[3]
        recipe.category shouldBe CraftingBookCategory.EQUIPMENT
        recipe.choiceMap.getValue('K').test(stack(Material.STICK, 1)) shouldBe false
        recipe.choiceMap.getValue('K').test(stack(Material.STICK, 2)) shouldBe true
    }

    @Test
    fun `rejects items and shapes that do not line up`() {
        listOf(
            "{ type: shaped, items: { KN: minecraft:stick, K: minecraft:dirt }, shape: [ K ] }",
            "{ type: shaped, items: { K: minecraft:stick }, shape: [ KM ] }",
            "{ type: shaped, items: { K: minecraft:stick, M: minecraft:dirt }, shape: [ K ] }",
            "{ type: shaped, items: { K: { tag: test:nothing } }, shape: [ K ] }",
        ).forEach { load(it).shouldBeNull() }
        warnings.size shouldBe 4
    }

    @Test
    fun `crafts its shape anywhere in the grid and mirrored, taking its amounts`() {
        recipeSet(
            "test:knife",
            "{ result: minecraft:emerald, recipes: [ { type: shaped, items: { B: { type: minecraft:bedrock, amount: 3 }, D: minecraft:dirt }, shape: [ 'BD' ] } ] }",
        )
        val table = CraftingTable(registered())

        table.place(stack(Material.BEDROCK, 3), null, null, stack(Material.DIRT)).shouldBeNull()
        table.place(stack(Material.DIRT), stack(Material.BEDROCK, 3)) shouldBe stack(Material.EMERALD)
        table.place(null, null, null, null, stack(Material.BEDROCK, 4), stack(Material.DIRT, 2)) shouldBe stack(Material.EMERALD)

        table.click()

        table.matrix[4] shouldBe stack(Material.BEDROCK)
        table.matrix[5] shouldBe stack(Material.DIRT)
    }
}
