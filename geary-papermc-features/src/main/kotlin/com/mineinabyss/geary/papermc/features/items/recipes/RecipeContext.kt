package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.idofront.messaging.ComponentLogger
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

internal class RecipeContext(
    val world: Geary,
    val key: NamespacedKey,
    val result: ItemStack,
    val group: String,
    val category: String,
    val permission: String?,
    private val logger: ComponentLogger,
) {
    fun warn(message: String): Nothing? {
        logger.w { "Recipe $key $message" }
        return null
    }

    fun warnUnenforcedPermission(station: String) {
        if (permission != null) warn("sets a permission, which $station cannot check as there is no player")
    }

    fun warnConflictingPreserve(ingredients: List<RecipeIngredient>) {
        val conflicting = ingredients.distinct()
            .flatMap { it.preserved }.groupingBy { it }.eachCount()
            .filterValues { it > 1 }.keys.ifEmpty { return }
            .joinToString { it.key().value() }

        warn("preserves $conflicting off several ingredients, it is taken from the first of them in the grid or definition")
    }

    inline fun <reified E : Enum<E>> bookCategory(): E = enumValues<E>().find { it.name.equals(category, ignoreCase = true) }
        ?: enumValueOf<E>("MISC").also { warn("uses the book category $category, which ${E::class.simpleName} has no entry for") }
}

internal class LoadedRecipe(val recipe: Recipe?, val custom: CustomRecipe)
