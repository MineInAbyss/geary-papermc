package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.idofront.features.feature
import org.koin.core.module.dsl.scopedOf

val RecipeFeature = feature("recipes") {
    dependsOn {
        features(ItemTracking)
        condition { get<GearyPaperConfig>().loading.recipes }
    }

    scopedModule {
        scopedOf(::RecipeManager)
        scopedOf(::RecipeDiscoveryListener)
        scopedOf(::RecipeCraftingListener)
    }

    onEnable {
        val context = get<RecipeManager>()
        addCloseables(context)
        context.registerRecipes()
        context.registerPotionMixes()

        listeners(
            get<RecipeDiscoveryListener>(),
            get<RecipeCraftingListener>(),
        )
    }
}
