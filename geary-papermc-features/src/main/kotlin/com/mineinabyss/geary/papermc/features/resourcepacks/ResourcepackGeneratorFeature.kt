package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.features.addCloseables
import com.mineinabyss.features.feature
import com.mineinabyss.features.get
import com.mineinabyss.geary.papermc.GearyPaperConfig
import org.kodein.di.bindSingletonOf

val ResourcepackGeneratorFeature = feature("resourcepack") {
    dependsOn {
        condition { get<GearyPaperConfig>().resourcePack.generate }
    }

    dependencies {
        bindSingletonOf(::ResourcePackGenerator)
    }

    onEnable {
        val generator = get<ResourcePackGenerator>()
        addCloseables(generator)
        generator.generateResourcePack()
    }
}
