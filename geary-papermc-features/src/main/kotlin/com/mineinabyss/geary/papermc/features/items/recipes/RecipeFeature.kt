package com.mineinabyss.geary.papermc.features.items.recipes

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.features.items.CustomItemsFeature
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.plugin
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay

val RecipeFeature = module("recipes") {
    require(get<GearyPaperConfig>().loading.recipes) { "Recipes must be enabled in config" }
    import(singleModule(CustomItemsFeature))

    //TODO we need world reference here to cache queries about prefabs, in the future we might switch to
    // having a dedicated world where prefabs get loaded into, which gets copied to each instance.
    val recipeManager by single {
        val world = gearyPaper.worldManager.global
        RecipeManager(world, get(), get(), world.getAddon(ItemTracking))
    }
    val discoveryListener by single { new(::RecipeDiscoveryListener) }
    val craftingListener by single { new(::RecipeCraftingListener) }

    addCloseables(recipeManager)
    listeners(discoveryListener, craftingListener)

    plugin.launch {
        delay(1.ticks) // Allow other plugins to register recipes
        recipeManager.registerRecipes()
        recipeManager.registerPotionMixes()
    }
}
