package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.gearyPaper

class NexoFeature(context: FeatureContext) : Feature(context) {
    init {
        pluginDeps("nexo")
    }

    override fun enable() {
        gearyPaper.configure {
            geary.markAsNexoFurniture()
            geary.markAsNexoItem()
        }

        listeners(

        )
    }
}