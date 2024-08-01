package com.mineinabyss.geary.papermc.features

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityDamageBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityLoadUnloadBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityShearedBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemRemovedBridge
import com.mineinabyss.geary.papermc.features.common.cooldowns.clearOldCooldownsSystem
import com.mineinabyss.geary.papermc.features.common.cooldowns.cooldownDisplaySystem
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.plugin.listeners

open class GearyPaperMCFeatures {
    companion object : GearyAddonWithDefault<GearyPaperMCFeatures> {
        override fun GearyPaperMCFeatures.install() {
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

        override fun default() = GearyPaperMCFeatures()
    }
}
