package com.mineinabyss.geary.papermc.features.resourcepacks

import com.github.shynixn.mccoroutine.bukkit.launch
import com.mineinabyss.dependencies.addCloseables
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.features.plugin
import com.mineinabyss.idofront.time.ticks
import kotlinx.coroutines.delay

val ResourcepackGeneratorFeature = module("resourcepack") {
    require(get<GearyPaperConfig>().resourcePack.generate) { "Resourcepack generation is disabled" }
    val generator by single { ResourcePackGenerator(gearyPaper.worldManager.global, get(), get()) }
    addCloseables(generator)
    plugin.launch {
        delay(1.ticks) // Allow other plugins to register resourcepack parts
        generator.generateResourcePack()
    }
}
