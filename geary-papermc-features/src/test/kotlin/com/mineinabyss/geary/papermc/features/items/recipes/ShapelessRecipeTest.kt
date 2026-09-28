package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.junit.jupiter.api.Test

class ShapelessRecipeTest : RecipeServerTest() {
    private fun grid(vararg items: String) = loadCustom<GridRecipe>("{ type: shapeless, items: [ ${items.joinToString()} ] }")

    @Test
    fun `falls back to its own matching only for an amount`() {
        grid("{ type: minecraft:stick, amount: 2 }").needsFallbackMatching shouldBe true
        grid("minecraft:stick").needsFallbackMatching shouldBe false
        loadCustom<ShapedCustomRecipe>("{ type: shaped, items: { K: { type: minecraft:stick, amount: 2 } }, shape: [ K ] }")
            .needsFallbackMatching shouldBe false
    }

    @Test
    fun `takes a component several ingredients preserve from the earliest slot`() {
        val recipe = shapeless(
            *Array(2) { ingredient(Material.IRON_PICKAXE, preserved = listOf(DataComponentTypes.CUSTOM_NAME)) },
            result = stack(Material.DIAMOND_PICKAXE),
        )
        val matrix = smallMatrix(null, named(Material.IRON_PICKAXE, "Early"), null, named(Material.IRON_PICKAXE, "Late"))

        recipe.resultFor(matrix).name() shouldBe Component.text("Early")
    }

    @Test
    fun `crafts its ingredients in any slots, consuming an amount from the earliest first`() {
        recipeSet(
            "test:knife",
            "{ result: minecraft:emerald, recipes: [ { type: shapeless, items: [ { type: minecraft:bedrock, amount: 3 }, minecraft:dirt ] } ] }",
        )
        val table = CraftingTable(registered())

        table.place(stack(Material.DIRT), stack(Material.BEDROCK, 2)).shouldBeNull()
        table.place(stack(Material.BEDROCK, 2), null, null, null, null, null, null, stack(Material.DIRT), stack(Material.BEDROCK, 3)) shouldBe
            stack(Material.EMERALD)

        table.click()

        table.matrix[0].shouldBeNull()
        table.matrix[7].shouldBeNull()
        table.matrix[8] shouldBe stack(Material.BEDROCK, 2)
    }
}
