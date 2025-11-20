package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingModule
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.serialization.recipes.options.ingredientOptionsListener
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

//TODO functions for hot-reloading individual recipes
class RecipeManager(
    private val world: Geary,
    private val logger: ComponentLogger,
    private val plugin: Plugin,
    private val gearyItems: ItemTrackingModule,
) : AutoCloseable {
    internal val recipesQuery by lazy { with(world) { cache(query<SetRecipes, PrefabKey>()) } }
    internal val potionMixes by lazy { with(world) { cache(query<SetPotionMixes, PrefabKey>()) } }

    var loadedRecipes: LoadedRecipes = LoadedRecipes()
        private set

    /**
     * Registers recipes for any entities matched by [recipesQuery].
     *
     * If a recipe already exists, it is skipped and not re-registered due to clientside lag when resending all recipes.
     */
    fun registerRecipes(): LoadedRecipes {
        val discoverable = mutableSetOf<NamespacedKey>()
        val all = mutableSetOf<NamespacedKey>()
        recipesQuery.forEach { (recipes, prefabKey) ->
            val result = getResultOrNull(prefabKey, recipes) ?: return@forEach

            recipes.removeRecipes.forEach { recipe ->
                runCatching {
                    Bukkit.removeRecipe(NamespacedKey.fromString(recipe)!!)
                }.onFailure {
                    logger.w { "Failed to remove recipe $recipe in ${prefabKey.key}, ${it.message}" }
                    logger.v { it.stackTraceToString() }
                }
            }

            recipes.recipes.forEachIndexed { i, recipe ->
                runCatching {
                    val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
                    // Skip recipes that were already registered, resending all causes large lag spike to clients
                    Bukkit.getRecipe(key) ?: run {
                        val (bukkitRecipe, options) = recipe.toRecipeWithOptions(key, result, recipes.group, recipes.category)!!
                        ingredientOptionsListener.keyToOptions[key.asString()] = options
                        //TODO add resend option to recipe.registerRecipeWithOptions
                        Bukkit.addRecipe(bukkitRecipe, true)
                    }
                    all += key
                    if (recipes.discoverRecipes) discoverable += key
                }.onFailure {
                    logger.w { "Failed to register recipe ${prefabKey.key} #$i, ${it.message}" }
                    logger.v { it.stackTraceToString() }
                }
            }
        }
        return LoadedRecipes(
            all,
            discoverable,
        ).also { loadedRecipes = it }
    }

    /**
     * Registers potion mix recipes for any entities matched by [potionMixes].
     *
     * If a recipe already exists, it is skipped and not re-registered due to clientside lag when resending all recipes.
     */
    fun registerPotionMixes() = potionMixes.forEach { (potionMixes, prefabKey) ->
        val result = potionMixes.result?.toItemStackOrNull() ?: gearyItems.createItem(prefabKey)
        val brewer = plugin.server.potionBrewer

        if (result != null) {
            potionMixes.potionmixes.forEachIndexed { i, potionmix ->
                val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
                // Skip recipes that were already registered, resending all causes large lag spike to clients
                try {
                    brewer.addPotionMix(potionmix.toPotionMix(key, result))
                } catch (_: IllegalArgumentException) {
                    // Thrown when recipe already registered
                    return@forEachIndexed
                }
            }
        } else logger.w { "PotionMix $prefabKey is missing result item" }
    }

    private fun getResultOrNull(prefabKey: PrefabKey, recipes: SetRecipes): ItemStack? {
        val result: ItemStack? = runCatching {
            recipes.result?.toItemStackOrNull() ?: gearyItems.createItem(prefabKey)
        }.onFailure {
            logger.w { "Recipe ${prefabKey.key} failed to load its result item" }
            logger.v { it.stackTraceToString() }
            return null
        }.getOrNull()

        if (result == null) {
            logger.w { "Recipe ${prefabKey.key} is missing result item" }
        }

        return result
    }

    override fun close() {
        recipesQuery.close()
        potionMixes.close()
    }

    data class LoadedRecipes(
        val recipes: Set<NamespacedKey> = setOf(),
        val discoverable: Set<NamespacedKey> = setOf(),
    )
}