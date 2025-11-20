package com.mineinabyss.geary.papermc.features.items.recipes

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.idofront.features.feature
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.singleOf

val RecipeFeature = feature("recipes") {
    dependsOn {
        condition { get<GearyPaperConfig>().recipes }
    }

    globalModule {
        singleOf(::RecipeContext)
    }

    scopedModule {
        scopedOf(::RecipeDiscoveryListener)
        scopedOf(::RecipeCraftingListener)
    }

    onEnable {
        get<RecipeContext>().registerPotionMixes()

        listeners(
            get<RecipeDiscoveryListener>(),
            get<RecipeCraftingListener>(),
        )
    }
}
