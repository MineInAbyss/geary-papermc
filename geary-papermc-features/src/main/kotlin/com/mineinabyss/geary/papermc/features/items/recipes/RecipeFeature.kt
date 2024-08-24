package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.gearyItems
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.prefabs
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.serialization.recipes.options.ingredientOptionsListener
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class RecipeFeature(val context: FeatureContext) : Feature(context) {
    private val recipes = geary.cache(query<SetRecipes, PrefabKey>())
    private val potionMixes = geary.cache(query<SetPotionMixes, PrefabKey>())

    override fun enable() {
        if (!context.isFirstEnable) {
            (recipes.entities().toSet() + potionMixes.entities().toSet()).forEach {
                prefabs.loader.reload(it)

            }
        }

        val autoDiscoveredRecipes = registerRecipes()
        registerPotionMixes()

        listeners(
            RecipeDiscoveryListener(autoDiscoveredRecipes),
            RecipeCraftingListener(),
        )
//        val recipeReader = MultiEntryYamlReader(
//            SetRecipes.serializer(), Yaml(
//                serializersModule = serializableComponents.serializers.module,
//                configuration = YamlConfiguration(
//                    strictMode = false
//                )
//            )
//        )
//        val recipes = recipeReader.decodeRecursive((plugin.dataPath / "recipes").createParentDirectories())
    }

    override fun disable() {
        recipes.forEach { (recipes, prefabKey) ->
            recipes.recipes.forEachIndexed { i, recipe ->
                val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
                Bukkit.removeRecipe(key)
            }
        }
    }

    private fun registerRecipes(): Set<NamespacedKey> {
        val discoveredRecipes = mutableSetOf<NamespacedKey>()

        recipes.forEach { (recipes, prefabKey) ->
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
                    Bukkit.getRecipe(key) ?: run {
                        val (bukkitRecipe, options) = recipe.toRecipeWithOptions(
                            key, result,
                            recipes.group,
                            recipes.category
                        )
                        ingredientOptionsListener.keyToOptions[key.asString()] = options
                        //TODO add resend option to recipe.registerRecipeWithOptions
                        Bukkit.addRecipe(bukkitRecipe, true)
                    }
                    if (recipes.discoverRecipes) discoveredRecipes += key
                }.onFailure {
                    geary.logger.w { "Failed to register recipe ${prefabKey.key} #$i, ${it.message}" }
                    geary.logger.v { it.stackTraceToString() }
                }
            }
        }
        return discoveredRecipes
    }

    /**
     * This is implemented separate from idofront recipes since they are handled differently by Minecraft.
     */
    private fun registerPotionMixes() = potionMixes.forEach { (potionMixes, prefabKey) ->
        val result = potionMixes.result?.toItemStackOrNull() ?: gearyItems.createItem(prefabKey)

        if (result != null) {
            potionMixes.potionmixes.forEachIndexed { i, potionmix ->
                val key = NamespacedKey(prefabKey.namespace, "${prefabKey.key}$i")
                gearyPaper.plugin.server.potionBrewer.removePotionMix(key)
                gearyPaper.plugin.server.potionBrewer.addPotionMix(potionmix.toPotionMix(key, result))
            }
        } else gearyPaper.logger.w { "PotionMix $prefabKey is missing result item" }
    }
}
