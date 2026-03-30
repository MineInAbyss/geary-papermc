package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.idofront.features.addCloseables
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.listeners
import org.kodein.di.bindSingletonOf

val RecipeFeature = feature("recipes") {
    dependsOn {
        features(ItemTracking)
        condition { get<GearyPaperConfig>().loading.recipes }
    }

    dependencies {
        bindSingletonOf(::RecipeManager)
        bindSingletonOf(::RecipeDiscoveryListener)
        bindSingletonOf(::RecipeCraftingListener)
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
