package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Test

class PreservedComponentsTest : RecipeTestBase() {
    private fun preserving(vararg types: DataComponentType) = ingredient(Material.IRON_PICKAXE, preserved = types.toList())

    private fun copy(result: ItemStack, vararg sources: Pair<RecipeIngredient, ItemStack?>) = PreservedComponents.copyOnto(result, sources.toList())

    @Test
    fun `parses component names regardless of case or namespace, dropping ones it cannot copy with a warning`() {
        listOf("DAMAGE", "minecraft:enchantments", "damage", "custom_data", "nope", "not a key").toPreservedComponents(context()) shouldContainExactly
            listOf(DataComponentTypes.DAMAGE, DataComponentTypes.ENCHANTMENTS)
        warnings shouldHaveSize 3
        warnings.first() shouldContain "custom_data"
    }

    @Test
    fun `copies a preserved component onto the result, keeping the result's own when the source lacks it`() {
        val name = preserving(DataComponentTypes.CUSTOM_NAME)

        copy(stack(Material.DIAMOND_PICKAXE), name to named(Material.IRON_PICKAXE, "Old")) shouldBe named(Material.DIAMOND_PICKAXE, "Old")
        copy(named(Material.DIAMOND_PICKAXE, "Configured"), name to stack(Material.IRON_PICKAXE)).name() shouldBe Component.text("Configured")
    }

    @Test
    fun `merges enchantments keeping the highest level of each`() {
        val source = enchanted(Material.IRON_SWORD, Enchantment.SHARPNESS to 5, Enchantment.UNBREAKING to 1)
        val result = enchanted(Material.DIAMOND_SWORD, Enchantment.SHARPNESS to 1, Enchantment.UNBREAKING to 3, Enchantment.LOOTING to 2)

        copy(result, preserving(DataComponentTypes.ENCHANTMENTS) to source).enchantmentLevels()!! shouldContainExactly
            mapOf(Enchantment.SHARPNESS to 5, Enchantment.UNBREAKING to 3, Enchantment.LOOTING to 2)
    }

    @Test
    fun `caps copied damage one below the result's durability`() {
        val damage = preserving(DataComponentTypes.DAMAGE)

        copy(stack(Material.GOLDEN_PICKAXE), damage to damaged(Material.IRON_PICKAXE, 20)).damage() shouldBe 20
        copy(stack(Material.GOLDEN_PICKAXE), damage to damaged(Material.IRON_PICKAXE, 200)).damage() shouldBe maxDamage(Material.GOLDEN_PICKAXE) - 1
        copy(stack(Material.STICK), damage to damaged(Material.IRON_PICKAXE, 20)).hasData(DataComponentTypes.DAMAGE) shouldBe false
    }
}
