package com.mineinabyss.geary.papermc.features.items.recipes

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack

internal sealed class CustomRecipe(
    val key: NamespacedKey,
    val result: ItemStack,
    val ingredients: List<RecipeIngredient>,
    val permission: String?,
) {
    open val isDiscoverable: Boolean get() = true
    val preservesComponents: Boolean = ingredients.any { it.preserved.isNotEmpty() }

    fun matchesPrefabIngredient(item: ItemStack?) = ingredients.any { it.isPrefab && it.matches(item) }

    fun hasPermission(sender: CommandSender) = permission == null || sender.hasPermission(permission)
}

internal sealed class GridRecipe(
    key: NamespacedKey,
    result: ItemStack,
    ingredients: List<RecipeIngredient>,
    permission: String?,
) : CustomRecipe(key, result, ingredients, permission) {
    open val isHandCraftable: Boolean get() = true
    open val needsFallbackMatching: Boolean get() = false
    open val rebuildsResult: Boolean get() = preservesComponents
    val isCustomConsumption: Boolean = ingredients.any { it.isCustomConsumption }
    val isRepeatable: Boolean = ingredients.none { it.consumption is Consumption.Damage } && ingredients.any { it.consumption != Consumption.Keep }

    abstract fun match(matrix: Array<ItemStack?>): Map<Int, RecipeMatcher.SlotUse>?

    open fun resultFor(matrix: Array<ItemStack?>): ItemStack {
        if (!preservesComponents) return result.clone()
        val matched = match(matrix) ?: return result.clone()
        val sources = matched.entries.sortedBy { it.key }.map { (slot, use) -> use.ingredient to matrix[slot] }
        return PreservedComponents.copyOnto(result, sources)
    }
}

/** [grid] is a flat 3x3 grid, cells outside the shape are null */
internal class ShapedCustomRecipe(
    key: NamespacedKey,
    result: ItemStack,
    val grid: List<RecipeIngredient?>,
    permission: String?,
    override val isHandCraftable: Boolean = true,
) : GridRecipe(key, result, grid.filterNotNull(), permission) {
    private val shape = RecipeMatcher.Shape(grid)

    override val isDiscoverable: Boolean get() = isHandCraftable

    override fun match(matrix: Array<ItemStack?>) = RecipeMatcher.matchShaped(shape, matrix)
}

internal class ShapelessCustomRecipe(
    key: NamespacedKey,
    result: ItemStack,
    ingredients: List<RecipeIngredient>,
    permission: String?,
) : GridRecipe(key, result, ingredients, permission) {
    // Registered with vanilla as one item per ingredient, so an amount spread over several slots never matches there
    override val needsFallbackMatching: Boolean = ingredients.any { it.amount > 1 }

    override fun match(matrix: Array<ItemStack?>) = RecipeMatcher.matchShapeless(ingredients, matrix)
}

internal class TransmuteCustomRecipe(
    key: NamespacedKey,
    result: ItemStack,
    private val input: RecipeIngredient,
    private val material: RecipeIngredient,
    permission: String?,
) : GridRecipe(key, result, listOf(input, material), permission) {
    // Vanilla copies every component of the input onto the result's material, not just the preserved ones
    override val rebuildsResult: Boolean get() = true

    // Vanilla refuses a transmute whose result would equal the input, which happens whenever both share a base material
    override val needsFallbackMatching: Boolean = input.display.type == result.type

    override fun match(matrix: Array<ItemStack?>) = RecipeMatcher.matchShapeless(ingredients, matrix)

    // Not taken from the matcher, overlapping predicates could make it pick another slot than the one vanilla assembled from
    override fun resultFor(matrix: Array<ItemStack?>): ItemStack {
        val filled = RecipeMatcher.filledSlots(matrix)
        val inputSlot = filled.firstOrNull { slot ->
            input.canUse(matrix[slot]) && filled.any { it != slot && material.canUse(matrix[it]) }
        } ?: return result.clone()
        val materialSlot = filled.first { it != inputSlot && material.canUse(matrix[it]) }
        return PreservedComponents.copyOnto(result, listOf(input to matrix[inputSlot], material to matrix[materialSlot]))
    }
}

internal class DyeCustomRecipe(
    key: NamespacedKey,
    private val target: RecipeIngredient,
    private val dye: RecipeIngredient,
) : GridRecipe(key, ItemStack.empty(), listOf(target, dye), permission = null) {
    // Vanilla first tries the recipe a grid matched last, which can be its own dye recipe for the target's material
    override val needsFallbackMatching: Boolean get() = true

    // Vanilla assembles onto the registered recipe's material instead of the target's own
    override val rebuildsResult: Boolean get() = true

    override fun match(matrix: Array<ItemStack?>): Map<Int, RecipeMatcher.SlotUse>? {
        val (targetSlot, dyeSlots) = slots(matrix) ?: return null
        return (dyeSlots + targetSlot).sorted().associateWith { RecipeMatcher.SlotUse(if (it == targetSlot) target else dye, 1) }
    }

    override fun resultFor(matrix: Array<ItemStack?>): ItemStack {
        val (targetSlot, dyeSlots) = slots(matrix) ?: return ItemStack.empty()
        return matrix[targetSlot]!!.dyedWith(dyeSlots.map { matrix[it]!! })
    }

    // Like vanilla's DyeRecipe, an item counts as the target before it counts as a dye
    private fun slots(matrix: Array<ItemStack?>): Pair<Int, List<Int>>? {
        val (targets, dyes) = RecipeMatcher.filledSlots(matrix).partition { target.matches(matrix[it]) }
        if (targets.size != 1 || dyes.isEmpty()) return null
        if (!dyes.all { slot -> matrix[slot]!!.let { dye.matches(it) && it.hasData(DataComponentTypes.DYE) } }) return null
        return targets.single() to dyes
    }
}

/** Run entirely by vanilla, kept so listeners can tell the recipe apart from vanilla ones and check its permission */
internal class PlainCustomRecipe(
    key: NamespacedKey,
    result: ItemStack,
    ingredients: List<RecipeIngredient>,
    permission: String?,
    override val isDiscoverable: Boolean = true,
) : CustomRecipe(key, result, ingredients, permission)

internal class SmithingCustomRecipe(
    key: NamespacedKey,
    result: ItemStack,
    private val template: RecipeIngredient?,
    private val base: RecipeIngredient,
    private val addition: RecipeIngredient,
    permission: String?,
) : CustomRecipe(key, result, listOfNotNull(template, base, addition), permission) {
    fun resultFor(assembled: ItemStack, templateItem: ItemStack?, baseItem: ItemStack?, additionItem: ItemStack?): ItemStack {
        val sources = listOfNotNull(template?.to(templateItem), base to baseItem, addition to additionItem)
        return PreservedComponents.copyOnto(assembled, sources)
    }
}

internal class AnvilCustomRecipe(
    key: NamespacedKey,
    result: ItemStack,
    val input: RecipeIngredient,
    val material: RecipeIngredient,
    val levelCost: Int,
    private val repair: Int,
    permission: String?,
) : CustomRecipe(key, result, listOf(input, material), permission) {
    override val isDiscoverable: Boolean get() = false

    class Craft(val recipe: AnvilCustomRecipe, val input: ItemStack, val material: ItemStack, val result: ItemStack, val levelCost: Int)

    fun craft(inputItem: ItemStack, materialItem: ItemStack, renameText: String?): Craft? {
        val result = resultFor(inputItem, materialItem) ?: return null
        return Craft(this, inputItem, materialItem, result, if (result.rename(inputItem, renameText)) levelCost + 1 else levelCost)
    }

    private fun resultFor(inputItem: ItemStack, materialItem: ItemStack): ItemStack? {
        if (repair <= 0) return PreservedComponents.copyOnto(result, listOf(input to inputItem, material to materialItem))

        val damage = inputItem.getData(DataComponentTypes.DAMAGE)?.takeIf { it > 0 } ?: return null
        return inputItem.clone().apply { setData(DataComponentTypes.DAMAGE, (damage - repair).coerceAtLeast(0)) }
    }

    // Mirrors AnvilMenu.createResult, which compares the name field against the input's name and charges a level for any change
    private fun ItemStack.rename(inputItem: ItemStack, text: String?): Boolean {
        if (text.isNullOrBlank()) {
            val inputName = inputItem.getData(DataComponentTypes.CUSTOM_NAME) ?: return false
            if (getData(DataComponentTypes.CUSTOM_NAME) == inputName) unsetData(DataComponentTypes.CUSTOM_NAME)
            return true
        }
        if (text == inputItem.hoverNameText()) return false
        setData(DataComponentTypes.CUSTOM_NAME, Component.text(text))
        return true
    }
}
