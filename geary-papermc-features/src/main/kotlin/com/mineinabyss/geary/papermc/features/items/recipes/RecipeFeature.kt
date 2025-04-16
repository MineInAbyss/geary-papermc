package com.mineinabyss.geary.papermc.features.items.recipes

import com.github.shynixn.mccoroutine.bukkit.scope
import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.serialization.recipes.options.ingredientOptionsListener
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

class RecipeFeature(val context: FeatureContext) : Feature(context) {
    val world get() = gearyPaper.worldManager.global
    private val recipes by lazy { with(world) { cache(query<SetRecipes, PrefabKey>()) } }
    private val potionMixes by lazy { with(world) { cache(query<SetPotionMixes, PrefabKey>()) } }
    private val gearyItems by lazy { with(world) { getAddon(ItemTracking) } }
    override val logger: ComponentLogger get() = context.logger

    override fun enable() {
        if (!context.isFirstEnable) {
            (recipes.entities().toSet() + potionMixes.entities().toSet()).forEach {
                world.getAddon(Prefabs).loader.reload(it)
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
