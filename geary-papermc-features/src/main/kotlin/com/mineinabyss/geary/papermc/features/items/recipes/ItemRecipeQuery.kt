package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.GearyQuery
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class ItemRecipeQuery : GearyQuery() {
    val recipes by get<SetRecipes>()
    val prefabKey by get<PrefabKey>()
}

fun CachedQuery<ItemRecipeQuery>.registerRecipes(): Set<NamespacedKey> {
    val discoveredRecipes = mutableSetOf<NamespacedKey>()

    forEach {
        val result: ItemStack? = runCatching {
            recipes.result?.toItemStackOrNull() ?: gearyItems.createItem(prefabKey)
        }.getOrNull()

        if (result == null) {
            geary.logger.w { "Recipe ${prefabKey.key} is missing result item" }
            return@forEach
        }

        recipes.removeRecipes.forEach { recipe ->
            runCatching {
                Bukkit.removeRecipe(NamespacedKey.fromString(recipe)!!)
            }.onFailure {
                geary.logger.w { "Failed to remove recipe $recipe in ${prefabKey.key}, ${it.message}" }
                geary.logger.v { it.stackTraceToString() }
            }
        }

        recipes.recipes.forEachIndexed { i, recipe ->
            runCatching {
                val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
                // Register recipe only if not present
                Bukkit.getRecipe(key) ?: recipe.registerRecipeWithOptions(key, result, recipes.group, recipes.category)
                if (recipes.discoverRecipes) discoveredRecipes += key
            }.onFailure {
                geary.logger.w { "Failed to register recipe ${prefabKey.key} #$i, ${it.message}" }
                geary.logger.v { it.stackTraceToString() }
            }
        }
    }
    return discoveredRecipes
}
