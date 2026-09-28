package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.bukkit.Material
import org.junit.jupiter.api.Test

class CraftingTableListenerTest : RecipeServerTest() {
    private val twoBedrock = "{ type: shaped, items: { B: { type: minecraft:bedrock, amount: 2 } }, shape: [ B ] }"
    private val oneBedrock = "{ type: shaped, items: { B: minecraft:bedrock }, shape: [ B ] }"

    private fun loadSet(vararg recipes: String, set: String = ""): RecipeManager {
        recipeSet("test:knife", "{ result: minecraft:emerald, $set recipes: [ ${recipes.joinToString()} ] }")
        return registered()
    }

    private fun table(vararg recipes: String, player: TestPlayer = TestPlayer()) = CraftingTable(loadSet(*recipes), player)

    @Test
    fun `shows the result of a recipe only its fallback matching finds`() {
        val table = table("{ type: shapeless, items: [ { type: minecraft:bedrock, amount: 2 } ] }")

        table.place(stack(Material.BEDROCK), stack(Material.BEDROCK)) shouldBe stack(Material.EMERALD)
        table.place(stack(Material.BEDROCK)).shouldBeNull()
    }

    @Test
    fun `hides the result from players without the set's permission`() {
        val manager = loadSet(oneBedrock, set = "permission: mia.knife,")

        CraftingTable(manager).place(stack(Material.BEDROCK)).shouldBeNull()
        CraftingTable(manager, TestPlayer(permissions = setOf("mia.knife"))).place(stack(Material.BEDROCK)) shouldBe stack(Material.EMERALD)
    }

    @Test
    fun `keeps an item denied in vanilla recipes out of them`() {
        denyInVanilla("test:cursed")
        val table = table()

        table.place(stack(Material.COAL), null, null, stack(Material.STICK)) shouldBe stack(Material.TORCH, 4)
        table.place(stack(Material.COAL), null, null, prefabItem("test:cursed").withType(Material.STICK)).shouldBeNull()
    }

    @Test
    fun `crafts a recipe consuming custom amounts itself, recording the craft like vanilla`() {
        val table = table(twoBedrock)
        table.place(stack(Material.BEDROCK, 3))

        table.click().isCancelled shouldBe true

        table.matrix[0] shouldBe stack(Material.BEDROCK)
        table.cursor.item shouldBe stack(Material.EMERALD)
        table.player.crafted shouldContainExactly listOf(recipeKey("test:knife"))
    }

    @Test
    fun `keeps, damages and replaces ingredients as configured, giving replacements that do not fit to the player`() {
        val table = table(
            """
            { type: shapeless, items: [
                { item: minecraft:bedrock, keep: true },
                { type: minecraft:shears, damage: 2 },
                { item: minecraft:barrier, amount: 2, replacement: minecraft:bowl }
            ] }
            """.trimIndent()
        )
        table.place(stack(Material.BEDROCK), stack(Material.SHEARS), stack(Material.BARRIER, 2))
        table.click()

        table.matrix[0] shouldBe stack(Material.BEDROCK)
        table.matrix[1]!!.damage() shouldBe 2
        table.matrix[2] shouldBe stack(Material.BOWL, 2)
        table.cursor.item shouldBe stack(Material.EMERALD)

        table.place(stack(Material.BEDROCK), stack(Material.SHEARS), stack(Material.BARRIER, 5))
        table.click()

        table.matrix[2] shouldBe stack(Material.BARRIER, 3)
        table.player.count(Material.BOWL) shouldBe 2
    }

    @Test
    fun `repeats a shift click until the ingredients run out`() {
        val table = table(twoBedrock)
        table.place(stack(Material.BEDROCK, 7))

        table.shiftClick()

        table.matrix[0] shouldBe stack(Material.BEDROCK)
        table.player.count(Material.EMERALD) shouldBe 3
        table.cursor.item.isEmpty shouldBe true
    }

    @Test
    fun `stops a shift click once the inventory is full`() {
        val player = TestPlayer().apply { repeat(36) { storage.setItem(it, stack(Material.DIRT, 64)) } }
        val table = table(twoBedrock, player = player)
        table.place(stack(Material.BEDROCK, 4))

        table.shiftClick().isCancelled shouldBe true

        table.matrix[0] shouldBe stack(Material.BEDROCK, 4)
        player.dropped.shouldBeEmpty()
    }

    @Test
    fun `cancels taking a result geary showed for a recipe that is gone after a reload`() {
        val manager = loadSet("{ type: shapeless, items: [ { type: minecraft:bedrock, amount: 2 } ] }")
        val table = CraftingTable(manager)
        table.place(stack(Material.BEDROCK), stack(Material.BEDROCK))

        manager.close()
        removeRecipeSets()
        table.reload(registered())

        table.click().isCancelled shouldBe true
    }
}
