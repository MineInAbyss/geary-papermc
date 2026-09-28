package com.mineinabyss.geary.papermc.features.items.recipes

import com.charleskorn.kaml.Yaml
import com.mineinabyss.idofront.serialization.DurationSerializer
import com.mineinabyss.idofront.serialization.SerializableDataTypes
import com.mineinabyss.idofront.serialization.SerializableItemStack
import com.mineinabyss.idofront.serialization.StringsSerializer
import com.mineinabyss.idofront.time.inWholeTicks
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.BrewingRecipe
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.CookingRecipe
import org.bukkit.inventory.CraftingRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.SmithingTransformRecipe
import org.bukkit.inventory.SmithingTrimRecipe
import org.bukkit.inventory.SmokingRecipe
import org.bukkit.inventory.StonecuttingRecipe
import org.bukkit.inventory.TransmuteRecipe
import org.bukkit.inventory.recipe.CookingBookCategory
import org.bukkit.inventory.recipe.CraftingBookCategory
import org.bukkit.potion.PotionType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
sealed class RecipeDefinition {
    /** Overrides the result the recipe set gives every definition */
    val result: SerializableItemStack? = null

    internal open val requiresResult: Boolean get() = true

    internal abstract fun load(context: RecipeContext): LoadedRecipe?

    // Encoded as several item properties lack value equality, a freshly read definition would never equal the last one
    internal fun fingerprint(): Any = runCatching { Yaml.default.encodeToString(RecipeDefinition.serializer(), this) }.getOrDefault(this)
}

@Serializable
sealed class PatternRecipeDefinition : RecipeDefinition() {
    abstract val items: Map<String, SerializableRecipeIngredient>

    /** Up to 3 rows of up to 3 characters, a space leaves the cell empty */
    abstract val shape: List<String>

    protected abstract val isHandCraftable: Boolean

    override fun load(context: RecipeContext): LoadedRecipe? {
        items.keys.firstOrNull { it.length != 1 }?.let { return context.warn("uses the item key '$it', which must be a single character") }
        val letters = items.keys.map { it.single() }.toSet()
        val used = shape.joinToString("").toSet() - ' '
        if (!letters.containsAll(used)) return context.warn("uses ${(used - letters).joinToString()} in its shape without an item for it")
        if (!used.containsAll(letters)) return context.warn("has items for ${(letters - used).joinToString()}, which its shape does not use")

        val perLetter = items.entries.associate { (letter, item) ->
            letter.single() to (item.toIngredient(context, enforceAmount = true) ?: return null)
        }

        val recipe = ShapedRecipe(context.key, context.result).applyBook(context)
        recipe.shape(*shape.toTypedArray())
        perLetter.forEach { (letter, ingredient) -> recipe.setIngredient(letter, ingredient.choice) }

        val grid = RecipeMatcher.grid(shape, perLetter::get)
        if (!isHandCraftable) {
            context.warnUnenforcedPermission("crafters")
            grid.firstNotNullOfOrNull { it?.crafterLimitation() }?.let { context.warn("$it, which crafters cannot do, so it never crafts") }
        }
        val permission = context.permission.takeIf { isHandCraftable }
        return LoadedRecipe(recipe, ShapedCustomRecipe(context.key, context.result, grid, permission, isHandCraftable))
    }
}

@Serializable
@SerialName("shaped")
class ShapedRecipeDefinition(
    override val items: Map<String, SerializableRecipeIngredient>,
    override val shape: List<String>,
) : PatternRecipeDefinition() {
    override val isHandCraftable: Boolean get() = true
}

@Serializable
@SerialName("crafter")
class CrafterRecipeDefinition(
    override val items: Map<String, SerializableRecipeIngredient>,
    override val shape: List<String>,
) : PatternRecipeDefinition() {
    override val isHandCraftable: Boolean get() = false
}

@Serializable
@SerialName("shapeless")
class ShapelessRecipeDefinition(
    val items: List<SerializableRecipeIngredient>,
) : RecipeDefinition() {
    override fun load(context: RecipeContext): LoadedRecipe? {
        val ingredients = items.map { it.toIngredient(context) ?: return null }
        val recipe = ShapelessRecipe(context.key, context.result).applyBook(context)
        ingredients.forEach { recipe.addIngredient(it.choice) }
        return LoadedRecipe(recipe, ShapelessCustomRecipe(context.key, context.result, ingredients, context.permission))
    }
}

@Serializable
@SerialName("transmute")
class TransmuteRecipeDefinition(
    val input: SerializableRecipeIngredient,
    val material: SerializableRecipeIngredient,
    val preserve: @Serializable(StringsSerializer::class) List<String> = emptyList(),
) : RecipeDefinition() {
    override fun load(context: RecipeContext): LoadedRecipe? {
        val input = input.toPlainIngredient(context, "transmute", canPreserve = true)
            ?.preserving(preserve, context) ?: return null
        val material = material.toPlainIngredient(context, "transmute", canPreserve = true) ?: return null

        if (input.display.type == context.result.type) context.warn("transmutes into its input's base material, which only crafting tables can craft and crafters cannot")
        val recipe = TransmuteRecipe(context.key, context.result.type, input.choice, material.choice).applyBook(context)
        return LoadedRecipe(recipe, TransmuteCustomRecipe(context.key, context.result, input, material, context.permission))
    }
}

private val DYES = SerializableRecipeIngredient(SerializableItemStack(tag = NamespacedKey.minecraft("dyes")))

@Serializable
@SerialName("dye")
class DyeRecipeDefinition(
    val target: SerializableRecipeIngredient,
    val dye: SerializableRecipeIngredient = DYES,
) : RecipeDefinition() {
    override val requiresResult: Boolean get() = false

    override fun load(context: RecipeContext): LoadedRecipe? {
        val target = target.toPlainIngredient(context, "dye") ?: return null
        val dye = dye.toPlainIngredient(context, "dye") ?: return null
        if (!dye.display.hasData(DataComponentTypes.DYE)) return context.warn("uses ${dye.display.type} as a dye, which lacks the dye component vanilla requires")
        if (context.permission != null) context.warn("is a dye recipe, which like vanilla's is open to everyone, so the set's permission does not apply")

        val recipe = dyeRecipe(context.key, target, dye, context.group, context.bookCategory())
        return LoadedRecipe(recipe, DyeCustomRecipe(context.key, target, dye))
    }
}

@Serializable
sealed class CookingRecipeDefinition : RecipeDefinition() {
    abstract val input: SerializableRecipeIngredient
    abstract val experience: Float

    abstract val cookingTime: Duration

    protected val cookingTicks: Int get() = cookingTime.inWholeTicks.toInt()

    internal abstract fun create(context: RecipeContext, input: RecipeIngredient): CookingRecipe<*>

    override fun load(context: RecipeContext): LoadedRecipe? {
        val input = input.toPlainIngredient(context, "cooking") ?: return null
        val recipe = create(context, input).applyBook(context)
        context.warnUnenforcedPermission("furnaces and campfires")
        return LoadedRecipe(recipe, PlainCustomRecipe(context.key, context.result, listOf(input), context.permission))
    }
}

@Serializable
@SerialName("furnace")
class FurnaceRecipeDefinition(
    override val input: SerializableRecipeIngredient,
    override val experience: Float = 0f,
    override val cookingTime: @Serializable(DurationSerializer::class) Duration = 10.seconds,
) : CookingRecipeDefinition() {
    override fun create(context: RecipeContext, input: RecipeIngredient) =
        FurnaceRecipe(context.key, context.result, input.choice, experience, cookingTicks)
}

@Serializable
@SerialName("blasting")
class BlastingRecipeDefinition(
    override val input: SerializableRecipeIngredient,
    override val experience: Float = 0f,
    override val cookingTime: @Serializable(DurationSerializer::class) Duration = 5.seconds,
) : CookingRecipeDefinition() {
    override fun create(context: RecipeContext, input: RecipeIngredient) =
        BlastingRecipe(context.key, context.result, input.choice, experience, cookingTicks)
}

@Serializable
@SerialName("smoking")
class SmokingRecipeDefinition(
    override val input: SerializableRecipeIngredient,
    override val experience: Float = 0f,
    override val cookingTime: @Serializable(DurationSerializer::class) Duration = 5.seconds,
) : CookingRecipeDefinition() {
    override fun create(context: RecipeContext, input: RecipeIngredient) =
        SmokingRecipe(context.key, context.result, input.choice, experience, cookingTicks)
}

@Serializable
@SerialName("campfire")
class CampfireRecipeDefinition(
    override val input: SerializableRecipeIngredient,
    override val experience: Float = 0f,
    override val cookingTime: @Serializable(DurationSerializer::class) Duration = 30.seconds,
) : CookingRecipeDefinition() {
    override fun create(context: RecipeContext, input: RecipeIngredient) =
        CampfireRecipe(context.key, context.result, input.choice, experience, cookingTicks)
}

@Serializable
@SerialName("stonecutting")
class StonecuttingRecipeDefinition(
    val input: SerializableRecipeIngredient,
) : RecipeDefinition() {
    override fun load(context: RecipeContext): LoadedRecipe? {
        val input = input.toPlainIngredient(context, "stonecutting") ?: return null
        val recipe = StonecuttingRecipe(context.key, context.result, input.choice).apply { group = context.group }
        return LoadedRecipe(recipe, PlainCustomRecipe(context.key, context.result, listOf(input), context.permission))
    }
}

@Serializable
sealed class SmithingRecipeDefinition : RecipeDefinition() {
    abstract val input: SerializableRecipeIngredient
    abstract val template: SerializableRecipeIngredient?
    abstract val addition: SerializableRecipeIngredient

    protected abstract val canPreserve: Boolean

    internal abstract fun create(
        context: RecipeContext,
        template: RecipeIngredient?,
        base: RecipeIngredient,
        addition: RecipeIngredient,
    ): LoadedRecipe?

    override fun load(context: RecipeContext): LoadedRecipe? {
        val template = template?.let { it.toPlainIngredient(context, "smithing", canPreserve) ?: return null }
        val base = input.toPlainIngredient(context, "smithing", canPreserve) ?: return null
        val addition = addition.toPlainIngredient(context, "smithing", canPreserve) ?: return null
        return create(context, template, base, addition)
    }
}

/** [preserve] adds to what [copyNbt] carries over from the input */
@Serializable
@SerialName("smithing_transform")
class SmithingTransformRecipeDefinition(
    override val input: SerializableRecipeIngredient,
    override val template: SerializableRecipeIngredient? = null,
    override val addition: SerializableRecipeIngredient,
    val copyNbt: Boolean = false,
    val preserve: @Serializable(StringsSerializer::class) List<String> = emptyList(),
) : SmithingRecipeDefinition() {
    override val canPreserve: Boolean get() = true

    override fun create(
        context: RecipeContext,
        template: RecipeIngredient?,
        base: RecipeIngredient,
        addition: RecipeIngredient,
    ): LoadedRecipe {
        val preservingBase = base.preserving(preserve, context)
        val recipe = SmithingTransformRecipe(
            context.key,
            context.result,
            template?.choice ?: RecipeChoice.empty(),
            preservingBase.choice,
            addition.choice,
            copyNbt,
        )
        val custom = SmithingCustomRecipe(context.key, context.result, template, preservingBase, addition, context.permission)
        return LoadedRecipe(recipe, custom)
    }
}

@Serializable
@SerialName("smithing_trim")
class SmithingTrimRecipeDefinition(
    override val input: SerializableRecipeIngredient,
    override val template: SerializableRecipeIngredient? = null,
    override val addition: SerializableRecipeIngredient,
    val trimPattern: String,
    val copyNbt: Boolean = false,
) : SmithingRecipeDefinition() {
    override val canPreserve: Boolean get() = false
    override val requiresResult: Boolean get() = false

    override fun create(
        context: RecipeContext,
        template: RecipeIngredient?,
        base: RecipeIngredient,
        addition: RecipeIngredient,
    ): LoadedRecipe? {
        if (template == null) return context.warn("is a smithing trim without a template, which vanilla requires")
        val pattern = NamespacedKey.fromString(trimPattern)
            ?.let(RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN)::get)
            ?: return context.warn("specifies an unknown trim pattern $trimPattern")

        val recipe = SmithingTrimRecipe(context.key, template.choice, base.choice, addition.choice, pattern, copyNbt)
        return LoadedRecipe(recipe, SmithingCustomRecipe(context.key, context.result, template, base, addition, context.permission))
    }
}

private val WATER_BOTTLE = SerializableRecipeIngredient(
    SerializableItemStack(type = Material.POTION, potionContents = SerializableDataTypes.PotionContents(PotionType.WATER))
)

@Serializable
@SerialName("brewing")
class BrewingRecipeDefinition(
    val input: SerializableRecipeIngredient = WATER_BOTTLE,
    val ingredient: SerializableRecipeIngredient,
) : RecipeDefinition() {
    override fun load(context: RecipeContext): LoadedRecipe? {
        val input = input.toPlainIngredient(context, "brewing") ?: return null
        val ingredient = ingredient.toPlainIngredient(context, "brewing") ?: return null
        context.warnUnenforcedPermission("brewing stands")

        val recipe = BrewingRecipe(context.key, context.result, input.choice, ingredient.choice)
        val custom = PlainCustomRecipe(context.key, context.result, listOf(input, ingredient), context.permission, isDiscoverable = false)
        return LoadedRecipe(recipe, custom)
    }
}

@Serializable
@SerialName("anvil")
class AnvilRecipeDefinition(
    val input: SerializableRecipeIngredient,
    val material: SerializableRecipeIngredient,
    val cost: Int = 1,
    val repair: Int = 0,
    val preserve: @Serializable(StringsSerializer::class) List<String> = emptyList(),
) : RecipeDefinition() {
    override val requiresResult: Boolean get() = repair <= 0

    override fun load(context: RecipeContext): LoadedRecipe? {
        var input = input.toIngredient(context)?.preserving(preserve, context) ?: return null
        var material = material.toIngredient(context) ?: return null

        if (input.consumption != Consumption.Consume()) {
            context.warn("cannot damage, keep or replace its anvil input, those options are dropped")
            input = input.withOptions(consumption = Consumption.Consume())
        }
        if (repair > 0 && (input.preserved.isNotEmpty() || material.preserved.isNotEmpty())) {
            context.warn("repairs its input, which keeps every component already, so preserve is ignored")
            input = input.withOptions(preserved = emptyList())
            material = material.withOptions(preserved = emptyList())
        }

        if (cost < 0 || repair < 0) context.warn("has a negative cost or repair, which counts as 0")
        if (cost >= TOO_EXPENSIVE_COST) context.warn("costs $cost levels, anvils refuse $TOO_EXPENSIVE_COST or more outside creative")

        val custom = AnvilCustomRecipe(
            key = context.key,
            result = if (repair > 0) ItemStack.empty() else context.result,
            input = input,
            material = material,
            levelCost = cost.coerceAtLeast(0),
            repair = repair.coerceAtLeast(0),
            permission = context.permission,
        )
        return LoadedRecipe(null, custom)
    }
}

// Vanilla's default AnvilView.maximumRepairCost
private const val TOO_EXPENSIVE_COST = 40

private fun SerializableRecipeIngredient.toPlainIngredient(
    context: RecipeContext,
    station: String,
    canPreserve: Boolean = false,
): RecipeIngredient? {
    val ingredient = toIngredient(context) ?: return null
    if (ingredient.isCustomConsumption) {
        context.warn("is a $station recipe, which cannot consume custom amounts, durability or replacements")
    }
    if (!canPreserve && ingredient.preserved.isNotEmpty()) {
        context.warn("is a $station recipe, which cannot preserve components off an ingredient")
    }
    return ingredient.withOptions(
        amount = 1,
        consumption = Consumption.Consume(),
        preserved = if (canPreserve) ingredient.preserved else emptyList(),
    )
}

private fun RecipeIngredient.preserving(names: List<String>, context: RecipeContext): RecipeIngredient =
    if (names.isEmpty()) this else withOptions(preserved = (preserved + names.toPreservedComponents(context)).distinct())

private fun <T : CraftingRecipe> T.applyBook(context: RecipeContext) = apply {
    group = context.group
    category = context.bookCategory<CraftingBookCategory>()
}

private fun <T : CookingRecipe<*>> T.applyBook(context: RecipeContext) = apply {
    group = context.group
    category = context.bookCategory<CookingBookCategory>()
}

// What CrafterRecipeListener cannot make up for once vanilla shrinks every slot by one
private fun RecipeIngredient.crafterLimitation(): String? {
    val remainder = display.craftingRemainder()
    return when (val consumption = consumption) {
        is Consumption.Damage -> "damages an ingredient"
        Consumption.Keep -> when {
            display.maxStackSize == 1 -> "keeps an unstackable ingredient"
            remainder != null -> "keeps an ingredient that leaves a crafting remainder"
            else -> null
        }

        is Consumption.Consume -> {
            val replacement = consumption.replacement
            if (replacement != null && remainder != null && !replacement.isSimilar(remainder))
                "replaces an ingredient with something other than its crafting remainder"
            else null
        }
    }
}
