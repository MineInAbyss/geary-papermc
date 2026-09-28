package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.inventory.RecipeChoice
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SerializableRecipeIngredientTest : RecipeTestBase() {
    @Nested
    inner class Decoding {
        @Test
        fun `reads prefab and material shorthands`() {
            decodeIngredient("test:knife").item.prefab shouldBe "test:knife"
            decodeIngredient("minecraft:stick").item.type shouldBe Material.STICK
        }

        @Test
        fun `reads amount and damage of a short item map as ingredient options, but keeps them on an item under an item key`() {
            val short = decodeIngredient("{ type: minecraft:shears, amount: 2, damage: 1 }")
            short.amount shouldBe 2
            short.damage shouldBe 1
            short.item.damage.shouldBeNull()

            val nested = decodeIngredient("item: { type: minecraft:shears, damage: 1 }")
            nested.item.damage shouldBe 1
            nested.damage shouldBe 0

            decodeIngredient("item: { type: minecraft:stick, amount: 3 }").amount shouldBe 3
            decodeIngredient("{ item: { type: minecraft:stick, amount: 3 }, amount: 2 }").amount shouldBe 2
        }
    }

    @Nested
    inner class Loading {
        @Test
        fun `matches a plain material by type and hands vanilla an item type choice`() {
            val sticks = loadIngredient("{ type: minecraft:stick, amount: 2 }").shouldNotBeNull()

            sticks.amount shouldBe 2
            sticks.matches(named(Material.STICK, "Named")) shouldBe true
            sticks.matches(stack(Material.DIRT)) shouldBe false
            sticks.choice.shouldBeInstanceOf<RecipeChoice.ItemTypeChoice>()
        }

        @Test
        fun `matches any item in a tag, narrowed down by the other item properties`() {
            val planks = loadIngredient("{ tag: minecraft:planks }").shouldNotBeNull()
            planks.matches(stack(Material.BIRCH_PLANKS)) shouldBe true
            planks.matches(stack(Material.STICK)) shouldBe false
            planks.choice.shouldBeInstanceOf<RecipeChoice.ItemTypeChoice>()

            val fancy = loadIngredient("{ tag: minecraft:planks, customName: Fancy }").shouldNotBeNull()
            fancy.matches(named(Material.OAK_PLANKS, "Fancy")) shouldBe true
            fancy.matches(stack(Material.OAK_PLANKS)) shouldBe false
            fancy.choice.shouldBeInstanceOf<RecipeChoice.PredicateChoice>()
        }

        @Test
        fun `matches a prefab by its key rather than its material or other data`() {
            val knife = loadIngredient("test:knife").shouldNotBeNull()

            knife.isPrefab shouldBe true
            knife.matches(prefabItem("test:knife").apply { setData(DataComponentTypes.DAMAGE, 5) }) shouldBe true
            knife.matches(stack(Material.IRON_SWORD)) shouldBe false
        }

        @Test
        fun `maps its options to a consumption mode`() {
            fun consumption(text: String) = loadIngredient(text).shouldNotBeNull().consumption

            consumption("minecraft:stick") shouldBe Consumption.Consume()
            consumption("{ item: minecraft:stick, amount: 0, damage: -2 }") shouldBe Consumption.Consume()
            consumption("{ item: minecraft:stick, keep: true }") shouldBe Consumption.Keep
            consumption("{ item: minecraft:stick, replacement: minecraft:bucket }") shouldBe Consumption.Consume(stack(Material.BUCKET))
            consumption("{ item: minecraft:shears, damage: 2, replacement: minecraft:stick }") shouldBe Consumption.Damage(2, stack(Material.STICK))
            warnings.size shouldBe 0
            consumption("{ item: minecraft:shears, keep: true, damage: 1 }") shouldBe Consumption.Keep
            warnings.size shouldBe 1
        }

        @Test
        fun `rejects conflicting options`() {
            loadIngredient("{ type: minecraft:shears, amount: 2, damage: 1 }").shouldBeNull()
            loadIngredient("{ type: minecraft:stick, damage: 1 }").shouldBeNull()
            warnings.size shouldBe 2
        }
    }
}
