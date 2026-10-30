package com.mineinabyss.geary.papermc.features.items.recipes

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemArmorTrim
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.trim.ArmorTrim
import org.bukkit.inventory.meta.trim.TrimMaterial
import org.bukkit.inventory.meta.trim.TrimPattern
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.SmithingTrimRecipe
import org.bukkit.inventory.SmokingRecipe
import org.junit.jupiter.api.Test

class RecipeDefinitionTest : RecipeTestBase() {
    private fun crafterOnly(items: String) = "{ type: crafter, items: $items, shape: [ K ] }"

    @Test
    fun `warns about each ingredient crafters cannot consume`() {
        listOf(
            "{ K: { type: minecraft:shears, damage: 1 } }",
            "{ K: { item: minecraft:shears, keep: true } }",
            "{ K: { item: minecraft:honey_bottle, keep: true } }",
            "{ K: { item: minecraft:milk_bucket, replacement: minecraft:stick } }",
        ).forEach { loadCustom<ShapedCustomRecipe>(crafterOnly(it)) }

        warnings.filter { "crafters cannot do" in it } shouldHaveSize 4
    }

    @Test
    fun `does not warn about ingredients crafters consume like vanilla`() {
        listOf(
            "{ K: minecraft:stick }",
            "{ K: { item: minecraft:stick, amount: 2 } }",
            "{ K: { item: minecraft:stick, keep: true } }",
            "{ K: { item: minecraft:milk_bucket, replacement: minecraft:bucket } }",
            "{ K: { item: minecraft:stick, replacement: minecraft:bowl } }",
        ).forEach { loadCustom<ShapedCustomRecipe>(crafterOnly(it)) }

        warnings.shouldBeEmpty()
    }

    @Test
    fun `builds the recipe of each cooking station`() {
        fun cooking(text: String) = load(text).shouldNotBeNull().recipe
        cooking("{ type: furnace, input: minecraft:raw_iron, cookingTime: 5s }").shouldBeInstanceOf<FurnaceRecipe>().cookingTime shouldBe 100
        cooking("{ type: blasting, input: minecraft:raw_iron }").shouldBeInstanceOf<BlastingRecipe>()
        cooking("{ type: smoking, input: minecraft:beef }").shouldBeInstanceOf<SmokingRecipe>()
        cooking("{ type: campfire, input: minecraft:beef }").shouldBeInstanceOf<CampfireRecipe>()
    }

    @Test
    fun `builds a trim with the pattern it names, skipping one without a template or with an unknown pattern`() {
        val template = "template: minecraft:coast_armor_trim_smithing_template,"
        fun trim(template: String, pattern: String) =
            load("{ type: smithing_trim, $template input: minecraft:iron_chestplate, addition: minecraft:diamond, trimPattern: $pattern }")

        trim(template, "minecraft:coast").shouldNotBeNull().recipe.shouldBeInstanceOf<SmithingTrimRecipe>().trimPattern shouldBe
            RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN).get(NamespacedKey.minecraft("coast"))
        trim("", "minecraft:coast").shouldBeNull()
        trim(template, "test:nothing").shouldBeNull()
    }

    @Test
    fun `smithing transform without a result or addition outputs its input minus the removed components`() {
        val loaded = loadCustom<SmithingCustomRecipe>(
            "{ type: smithing_transform, template: minecraft:stick, input: { item: minecraft:iron_chestplate, remove: minecraft:trim } }",
            result = ItemStack.empty(),
        )
        loaded.inheritsBase shouldBe true
        loaded.ingredients.last().removed shouldBe listOf(RemovedComponentData(DataComponentTypes.TRIM))

        val trimmed = ItemStack(Material.IRON_CHESTPLATE).apply { setData(DataComponentTypes.TRIM, ItemArmorTrim.itemArmorTrim(ArmorTrim(TrimMaterial.GOLD, TrimPattern.COAST))) }
        loaded.resultFor(ItemStack(Material.DIAMOND), null, trimmed, null).apply {
            type shouldBe Material.IRON_CHESTPLATE
            hasData(DataComponentTypes.TRIM) shouldBe false
        }
    }
}
