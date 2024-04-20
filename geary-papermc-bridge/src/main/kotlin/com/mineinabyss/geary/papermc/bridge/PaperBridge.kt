package com.mineinabyss.geary.papermc.bridge

import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.bridge.systems.createCooldownDisplaySystem

class PaperBridge {
    companion object : GearyAddonWithDefault<PaperBridge> {
        override fun PaperBridge.install(): Unit = geary.run {
            geary {
                autoscan(this::class.java.classLoader, "com.mineinabyss.geary.papermc.bridge") {
                    systems()
                }
            }

            createCooldownDisplaySystem()
        }

        override fun default() = PaperBridge()
    }
}
