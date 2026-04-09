package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.dependencies.addCloseables
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.gearyPaper

val ResourcepackGeneratorFeature = module("resourcepack") {
    require(get<GearyPaperConfig>().resourcePack.generate) { "Resourcepack generation is disabled" }
    val generator by single { ResourcePackGenerator(gearyPaper.worldManager.global, get(), get()) }
    addCloseables(generator)
    generator.generateResourcePack()
}
