package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.serialization.recipes.options.ingredientOptionsListener
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.singleOf

class RecipeContext(
    val world: Geary,
    val logger: ComponentLogger,
    val plugin: Plugin,
) {
    internal val recipesQuery by lazy { with(world) { cache(query<SetRecipes, PrefabKey>()) } }
    internal val potionMixes by lazy { with(world) { cache(query<SetPotionMixes, PrefabKey>()) } }
    internal val gearyItems by lazy { with(world) { getAddon(ItemTracking) } }

    val recipes = registerRecipes()

    private fun registerRecipes(): Set<NamespacedKey> {
        val discoveredRecipes = mutableSetOf<NamespacedKey>()
        recipesQuery.forEach { (recipes, prefabKey) ->
            val result: ItemStack? = runCatching {
                recipes.result?.toItemStackOrNull() ?: gearyItems.createItem(prefabKey)
            }.getOrNull()

            if (result == null) {
                logger.w { "Recipe ${prefabKey.key} is missing result item" }
                return@forEach
            }

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
                    // Register recipe only if not present
                    Bukkit.getRecipe(key) ?: run {
                        val (bukkitRecipe, options) = recipe.toRecipeWithOptions(key, result, recipes.group, recipes.category)!!
                        ingredientOptionsListener.keyToOptions[key.asString()] = options
                        //TODO add resend option to recipe.registerRecipeWithOptions
                        Bukkit.addRecipe(bukkitRecipe, true)
                    }
                    if (recipes.discoverRecipes) discoveredRecipes += key
                }.onFailure {
                    logger.w { "Failed to register recipe ${prefabKey.key} #$i, ${it.message}" }
                    logger.v { it.stackTraceToString() }
                }
            }
        }
        return discoveredRecipes
    }

    /**
     * This is implemented separate from idofront recipes since they are handled differently by Minecraft.
     */
    internal fun registerPotionMixes() = potionMixes.forEach { (potionMixes, prefabKey) ->
        val result = potionMixes.result?.toItemStackOrNull() ?: gearyItems.createItem(prefabKey)

        if (result != null) {
            potionMixes.potionmixes.forEachIndexed { i, potionmix ->
                val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
                plugin.server.potionBrewer.removePotionMix(key)
                plugin.server.potionBrewer.addPotionMix(potionmix.toPotionMix(key, result))
            }
        } else logger.w { "PotionMix $prefabKey is missing result item" }
    }
}

val RecipeFeature = feature("recipes") {
    globalModule {
        singleOf(::RecipeContext)
    }

    scopedModule {
        scopedOf(::RecipeDiscoveryListener)
        scopedOf(::RecipeCraftingListener)
    }

    onLoad {
        val context = get<RecipeContext>()
//        if (!context.isFirstEnable) {
//        (context.recipesQuery.entities().toSet() + context.potionMixes.entities().toSet()).forEach {
//            get<Geary>().getAddon(Prefabs).loader.reload(it)
//        }
//        }
    }

    onEnable {
        get<RecipeContext>().registerPotionMixes()

        listeners(
            get<RecipeDiscoveryListener>(),
            get<RecipeCraftingListener>(),
        )
    }

    onDisable {
//        get<RecipeContext>().recipes.forEach { (recipes, prefabKey) ->
//            recipes.recipes.forEachIndexed { i, recipe ->
//                val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
//                Bukkit.removeRecipe(key)
//            }
//        }
    }
}
