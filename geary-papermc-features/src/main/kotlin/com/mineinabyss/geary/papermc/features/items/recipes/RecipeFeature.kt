package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.features.addCloseables
import com.mineinabyss.features.feature
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.tracking.items.ItemTracking
import com.mineinabyss.idofront.features.listeners
import org.kodein.di.bindSingletonOf
import org.kodein.di.instance

val RecipeFeature = feature("recipes") {
    dependsOn {
        features(ItemTracking)
        condition { instance<GearyPaperConfig>().loading.recipes }
    }

    dependencies {
        bindSingletonOf(::RecipeManager)
        bindSingletonOf(::RecipeDiscoveryListener)
        bindSingletonOf(::RecipeCraftingListener)
    }

    onEnable {
        val context = instance<RecipeManager>()
        addCloseables(context)
        context.registerRecipes()
        context.registerPotionMixes()

        listeners(
            instance<RecipeDiscoveryListener>(),
            instance<RecipeCraftingListener>(),
        )
    }
}
