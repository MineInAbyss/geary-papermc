package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.junit.jupiter.api.Test

class RecipeIngredientTest : RecipeTestBase() {
    private val pickaxeDurability = maxDamage(Material.IRON_PICKAXE)

    private fun pickaxeWithDurabilityLeft(left: Int) = damaged(Material.IRON_PICKAXE, pickaxeDurability - left)

    @Test
    fun `requires its amount unless told to ignore it`() {
        val sticks = ingredient(Material.STICK, amount = 3)

        sticks.canUse(stack(Material.STICK, 2)) shouldBe false
        sticks.canUse(stack(Material.STICK, 2), ignoreAmount = true) shouldBe true
        sticks.canUse(stack(Material.STICK, 3)) shouldBe true
        sticks.canUse(stack(Material.DIRT, 3)) shouldBe false
        sticks.canUse(null) shouldBe false
    }

    @Test
    fun `damages only items with enough durability left, ignoring unbreakable ones`() {
        val pickaxe = ingredient(Material.IRON_PICKAXE, consumption = Consumption.Damage(10))
        val unbreakable = pickaxeWithDurabilityLeft(1).apply { setData(DataComponentTypes.UNBREAKABLE) }

        pickaxe.canUse(pickaxeWithDurabilityLeft(10)) shouldBe true
        pickaxe.canUse(pickaxeWithDurabilityLeft(9)) shouldBe false
        pickaxe.canUse(unbreakable) shouldBe true
        pickaxe.consume(unbreakable, null).slot shouldBe unbreakable
        ingredient(Material.STICK, consumption = Consumption.Damage(1)).canUse(stack(Material.STICK)) shouldBe false
    }

    @Test
    fun `takes durability off the item, leaving the replacement once it breaks`() {
        val pickaxe = ingredient(Material.IRON_PICKAXE, consumption = Consumption.Damage(10))
        val replaced = ingredient(Material.IRON_PICKAXE, consumption = Consumption.Damage(10, stack(Material.STICK)))

        pickaxe.consume(damaged(Material.IRON_PICKAXE, 5), null) shouldBe RecipeIngredient.Consumed(damaged(Material.IRON_PICKAXE, 15), null)
        pickaxe.consume(pickaxeWithDurabilityLeft(10), null).slot.shouldBeNull()
        replaced.consume(pickaxeWithDurabilityLeft(10), null).slot shouldBe stack(Material.STICK)
        replaced.consume(pickaxeWithDurabilityLeft(20), null).slot?.type shouldBe Material.IRON_PICKAXE
    }

    @Test
    fun `takes the amount used`() {
        val sticks = ingredient(Material.STICK, amount = 3)

        sticks.consume(stack(Material.STICK, 5), null) shouldBe RecipeIngredient.Consumed(stack(Material.STICK, 2), null)
        sticks.consume(stack(Material.STICK, 5), null, take = 1).slot shouldBe stack(Material.STICK, 4)
        sticks.consume(stack(Material.STICK, 3), null).slot.shouldBeNull()
    }

    @Test
    fun `leaves the vanilla crafting remainder unless given a replacement`() {
        ingredient(Material.WATER_BUCKET).consume(stack(Material.WATER_BUCKET), null).slot shouldBe stack(Material.BUCKET)
        ingredient(Material.HONEY_BOTTLE).consume(stack(Material.HONEY_BOTTLE, 3), null) shouldBe
            RecipeIngredient.Consumed(stack(Material.HONEY_BOTTLE, 2), stack(Material.GLASS_BOTTLE))
        ingredient(Material.WATER_BUCKET, consumption = Consumption.Consume(stack(Material.STICK))).consume(stack(Material.WATER_BUCKET), null) shouldBe
            RecipeIngredient.Consumed(stack(Material.STICK), null)
    }

    @Test
    fun `gives one replacement per item taken, splitting what overflows the slot`() {
        val sticks = ingredient(Material.STICK, amount = 20, consumption = Consumption.Consume(stack(Material.BUCKET)))

        sticks.consume(stack(Material.STICK, 5), null, take = 2) shouldBe RecipeIngredient.Consumed(stack(Material.STICK, 3), stack(Material.BUCKET, 2))
        sticks.consume(stack(Material.STICK, 20), null) shouldBe RecipeIngredient.Consumed(stack(Material.BUCKET, 16), stack(Material.BUCKET, 4))
    }
}
