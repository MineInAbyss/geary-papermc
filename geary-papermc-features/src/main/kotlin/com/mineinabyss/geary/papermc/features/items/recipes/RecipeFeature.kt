package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.idofront.features.feature
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.singleOf

val RecipeFeature = feature("recipes") {
    dependsOn {
        condition { get<GearyPaperConfig>().loading.recipes }
    }

    globalModule {
        singleOf(::RecipeManager)
    }

    scopedModule {
        scopedOf(::RecipeDiscoveryListener)
        scopedOf(::RecipeCraftingListener)
    }

    onEnable {
        val context = get<RecipeManager>()
        autoClose(context)
        context.registerRecipes()
        context.registerPotionMixes()

        listeners(
            get<RecipeDiscoveryListener>(),
            get<RecipeCraftingListener>(),
        )
    }
}
