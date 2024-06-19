package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.application.onPluginEnable
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.idofront.plugin.listeners

fun GearyModule.itemRecipes() {
    trackPotionMixes()

    val recipeQuery = cache(ItemRecipeQuery())

    onPluginEnable {
        val recipes = recipeQuery.registerRecipes()
        listeners(
            RecipeDiscoveryListener(recipes),
            RecipeCraftingListener(),
        )
    }
}
