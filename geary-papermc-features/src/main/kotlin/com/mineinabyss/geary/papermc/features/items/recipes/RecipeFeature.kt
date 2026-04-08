package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.features.items.CustomItemsFeature
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.features.listeners

val RecipeFeature = module("recipes") {
    require(get<GearyPaperConfig>().loading.recipes) { "Recipes must be enabled in config" }
    import(singleModule(CustomItemsFeature))

    //TODO we need world reference here to cache queries about prefabs, in the future we might switch to
    // having a dedicated world where prefabs get loaded into, which gets copied to each instance.
    val recipeManager by single { RecipeManager(gearyPaper.worldManager.global, get(), get(), get()) }
    val discoveryListener by single { new(::RecipeDiscoveryListener) }
    val craftingListener by single { new(::RecipeCraftingListener) }

    addCloseables(recipeManager)
    recipeManager.registerRecipes()
    recipeManager.registerPotionMixes()
    listeners(discoveryListener, craftingListener)
}
