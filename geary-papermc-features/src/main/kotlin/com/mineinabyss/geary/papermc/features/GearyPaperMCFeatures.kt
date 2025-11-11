package com.mineinabyss.geary.papermc.features

import com.mineinabyss.geary.addons.dsl.createAddon
import com.mineinabyss.geary.papermc.features.common.actions.ShulkerBulletHitListener
import com.mineinabyss.geary.papermc.features.common.cooldowns.clearOldCooldownsSystem
import com.mineinabyss.geary.papermc.features.common.cooldowns.cooldownDisplaySystem
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityDamageBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityLoadUnloadBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityShearedBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemRemovedBridge
import com.mineinabyss.geary.papermc.features.entities.pathfinders.addPathfindersSystem
import com.mineinabyss.geary.papermc.features.items.resourcepacks.ResourcePackGenerator
import com.mineinabyss.geary.papermc.onPluginEnable
import com.mineinabyss.geary.papermc.tracking.entities.components.markBindEntityTypeAsCustomMob
import com.mineinabyss.geary.papermc.tracking.entities.components.markSetEntityTypeAsCustomMob
import com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn.createAttemptSpawnListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.removevanillamobs.RemoveVanillaMobsListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.ConvertEntityTypesListener
import com.mineinabyss.idofront.plugin.listeners

val GearyPaperMCFeatures = createAddon("Geary Paper Features") {
    systems {
        cooldownDisplaySystem()
        clearOldCooldownsSystem()
        addPathfindersSystem()
        createAttemptSpawnListener()
        markSetEntityTypeAsCustomMob()
        markBindEntityTypeAsCustomMob()
    }

    onPluginEnable {
        ResourcePackGenerator(geary).generateResourcePack()

        plugin.listeners(
            ConvertEntityTypesListener(this),
            RemoveVanillaMobsListener(this),
        )

        plugin.listeners(
            EntityDamageBridge(),
            EntityLoadUnloadBridge(),
            EntityShearedBridge(),
            ShulkerBulletHitListener(),
        )

        plugin.listeners(
            ItemInteractBridge(),
            ItemRemovedBridge(),
        )
    }
}
