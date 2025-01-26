package com.mineinabyss.geary.papermc.features

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.papermc.features.common.cooldowns.clearOldCooldownsSystem
import com.mineinabyss.geary.papermc.features.common.cooldowns.cooldownDisplaySystem
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityDamageBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityLoadUnloadBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityShearedBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemRemovedBridge
import com.mineinabyss.geary.papermc.features.entities.pathfinders.addPathfindersSystem
import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackGenerator
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.onPluginEnable
import com.mineinabyss.idofront.plugin.listeners

val GearyPaperMCFeatures = createAddon("Geary Paper Features") {
    systems {
        cooldownDisplaySystem()
        clearOldCooldownsSystem()
        addPathfindersSystem()
    }

    entities {
        ResourcePackGenerator(geary).generateResourcePack()
    }

    onPluginEnable {
        gearyPaper.plugin.listeners(
            EntityDamageBridge(),
            EntityLoadUnloadBridge(),
            EntityShearedBridge(),
        )

        gearyPaper.plugin.listeners(
            ItemInteractBridge(),
            ItemRemovedBridge(),
        )
    }
}
