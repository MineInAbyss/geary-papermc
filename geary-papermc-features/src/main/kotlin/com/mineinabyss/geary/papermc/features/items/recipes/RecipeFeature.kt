package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.dependencies.addCloseables
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.import
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.dependencies.singleModule
import com.mineinabyss.geary.papermc.ExternalItemLoading
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.PrefabLoading
import com.mineinabyss.geary.papermc.features.items.CustomItemsFeature
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.idofront.config.SingleConfig
import com.mineinabyss.idofront.features.launch
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await

val RecipeFeature = module("recipes") {
    require(get<GearyPaperConfig>().loading.recipes) { "Recipes must be enabled in config" }
    import(singleModule(CustomItemsFeature))

    //TODO we need world reference here to cache queries about prefabs, in the future we might switch to
    // having a dedicated world where prefabs get loaded into, which gets copied to each instance.
    val recipeManager by single {
        val world = gearyPaper.worldManager.global
        val config = get<SingleConfig<GearyPaperConfig>>()
        RecipeManager(
            world = world,
            logger = get(),
            gearyItems = world.getAddon(ItemTracking),
            autoDiscoverVanillaRecipes = get<GearyPaperConfig>().items.autoDiscoverVanillaRecipes,
            // Read when the manager closes, after a reload has read the config again
            isStillEnabled = { config.getCachedOrRead().loading.recipes },
        )
    }

    addCloseables(recipeManager)
    listeners(
        new(::RecipeDiscoveryListener),
        new(::RecipeReloadListener),
        new(::CraftingTableRecipeListener),
        new(::CrafterRecipeListener),
        new(::SmithingRecipeListener),
        new(::StonecutterRecipeListener),
        new(::AnvilRecipeListener),
    )

    launch {
        delay(1.ticks) // Allow other plugins to register recipes
        PrefabLoading.awaitLoaded().await()
        ExternalItemLoading.awaitLoaded().await()
        recipeManager.registerRecipes()
    }
}
