package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.idofront.features.feature
import org.koin.core.module.dsl.scopedOf

val ResourcepackGeneratorFeature = feature("resourcepack") {
    dependsOn {
        condition { get<GearyPaperConfig>().resourcePack.generate }
    }

    scopedModule {
        scopedOf(::ResourcePackGenerator)
    }

    onEnable {
        val generator = get<ResourcePackGenerator>()
        addCloseables(generator)
        get<ResourcePackGenerator>().generateResourcePack()
    }
}
