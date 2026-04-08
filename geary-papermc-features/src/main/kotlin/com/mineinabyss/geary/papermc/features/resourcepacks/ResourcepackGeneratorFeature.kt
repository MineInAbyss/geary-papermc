package com.mineinabyss.geary.papermc.features.resourcepacks

import com.mineinabyss.dependencies.*
import com.mineinabyss.geary.papermc.GearyPaperConfig

val ResourcepackGeneratorFeature = module("resourcepack") {
    require(get<GearyPaperConfig>().resourcePack.generate) { "Resourcepack generation is disabled" }
    val generator by single { new(::ResourcePackGenerator) }
    addCloseables(generator)
    generator.generateResourcePack()
}
