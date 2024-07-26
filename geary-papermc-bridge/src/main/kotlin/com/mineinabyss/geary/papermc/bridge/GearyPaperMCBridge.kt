package com.mineinabyss.geary.papermc.bridge

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.bridge.events.entities.EntityDamageBridge
import com.mineinabyss.geary.papermc.bridge.events.entities.EntityLoadUnloadBridge
import com.mineinabyss.geary.papermc.bridge.events.entities.EntityShearedBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemRemovedBridge
import com.mineinabyss.geary.papermc.bridge.cooldowns.systems.clearOldCooldownsSystem
import com.mineinabyss.geary.papermc.bridge.cooldowns.systems.cooldownDisplaySystem
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.plugin.listeners

open class GearyPaperMCBridge {
    companion object : GearyAddonWithDefault<GearyPaperMCBridge> {
        override fun GearyPaperMCBridge.install() {
            geary.run {
                cooldownDisplaySystem()
                clearOldCooldownsSystem()
            }
            geary.pipeline.runOnOrAfter(GearyPhase.ENABLE) {
                gearyPaper.plugin.listeners(
                    EntityDamageBridge(),
                    EntityLoadUnloadBridge(),
                    EntityShearedBridge()
                )

                gearyPaper.plugin.listeners(
                    ItemInteractBridge(),
                    ItemRemovedBridge(),
                )
            }
        }

        override fun default() = GearyPaperMCBridge()
    }
}
