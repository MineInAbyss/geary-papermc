package com.mineinabyss.geary.papermc.features.entities

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.features.common.actions.ShulkerBulletHitListener
import com.mineinabyss.geary.papermc.features.common.cooldowns.clearOldCooldownsSystem
import com.mineinabyss.geary.papermc.features.common.cooldowns.cooldownDisplaySystem
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityDamageBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityLoadUnloadBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EntityShearedBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.features.common.event_bridge.items.ItemRemovedBridge
import com.mineinabyss.geary.papermc.features.configureGeary
import com.mineinabyss.geary.papermc.features.entities.bucketable.BucketableListener
import com.mineinabyss.geary.papermc.features.entities.commands.mobs
import com.mineinabyss.geary.papermc.features.entities.displayname.ShowDisplayNameOnKillerListener
import com.mineinabyss.geary.papermc.features.entities.pathfinders.addPathfindersSystem
import com.mineinabyss.geary.papermc.features.entities.prevent.PreventEventsFeature
import com.mineinabyss.geary.papermc.features.entities.sounds.AmbientSoundsFeature
import com.mineinabyss.geary.papermc.features.entities.taming.TamingListener
import com.mineinabyss.geary.papermc.tracking.entities.components.markBindEntityTypeAsCustomMob
import com.mineinabyss.geary.papermc.tracking.entities.components.markSetEntityTypeAsCustomMob
import com.mineinabyss.geary.papermc.tracking.entities.systems.attemptspawn.createAttemptSpawnListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.removevanillamobs.RemoveVanillaMobsListener
import com.mineinabyss.geary.papermc.tracking.entities.systems.updatemobtype.ConvertEntityTypesListener
import com.mineinabyss.idofront.features.feature

val MinecraftFeatures = feature("minecraft-features") {
    dependsOn {
        condition { get<GearyPaperConfig>().minecraftFeatures }
    }

    install(
        AmbientSoundsFeature,
        PreventEventsFeature,
    )

    onEnable {
        configureGeary {
            autoClose(
                cooldownDisplaySystem(),
                clearOldCooldownsSystem(),
                addPathfindersSystem(),
                createAttemptSpawnListener(),
                markSetEntityTypeAsCustomMob(),
                markBindEntityTypeAsCustomMob(),
            )
        }

        listeners(
            ConvertEntityTypesListener(),
            RemoveVanillaMobsListener(),
        )

        listeners(
            EntityDamageBridge(),
            EntityLoadUnloadBridge(),
            EntityShearedBridge(),
            ShulkerBulletHitListener(),
        )

        listeners(
            ItemInteractBridge(),
            ItemRemovedBridge(),
        )

        listeners(
            BucketableListener(),
            ShowDisplayNameOnKillerListener(),
            TamingListener(),
        )
    }

    mainCommand {
        mobs()
    }
}
