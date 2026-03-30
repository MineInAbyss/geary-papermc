package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.idofront.features.addCloseables
import com.mineinabyss.idofront.features.feature
import com.mineinabyss.idofront.features.get
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
        get<ResourcePackGenerator>().generateResourcePack()
    }
}
