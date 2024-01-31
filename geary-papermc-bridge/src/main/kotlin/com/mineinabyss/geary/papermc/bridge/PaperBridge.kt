package com.mineinabyss.geary.papermc.bridge

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfigModule
import com.mineinabyss.geary.papermc.bridge.actions.*
import com.mineinabyss.geary.papermc.bridge.conditions.*
import com.mineinabyss.geary.papermc.bridge.conditions.location.BlockConditionChecker
import com.mineinabyss.geary.papermc.bridge.conditions.location.HeightConditionChecker
import com.mineinabyss.geary.papermc.bridge.conditions.location.LightConditionChecker
import com.mineinabyss.geary.papermc.bridge.conditions.location.TimeConditionChecker
import com.mineinabyss.geary.papermc.bridge.config.parsers.CreateDefaultSkills
import com.mineinabyss.geary.papermc.bridge.config.parsers.ParseSkills
import com.mineinabyss.geary.papermc.bridge.events.entities.*
import com.mineinabyss.geary.papermc.bridge.events.items.ItemBreakBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemConsumeBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.bridge.mythicmobs.RunMMSkillSystem
import com.mineinabyss.geary.papermc.bridge.readers.ReadLocationSystem
import com.mineinabyss.geary.papermc.bridge.readers.ReadTargetBlockSystem
import com.mineinabyss.geary.papermc.bridge.targetselectors.NearbyEntitiesSelector
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners

class PaperBridge {
    companion object : GearyAddonWithDefault<PaperBridge> {
        override fun PaperBridge.install() {
            geary {
                autoscan(this::class.java.classLoader, "com.mineinabyss.geary.papermc.bridge") {
                    systems()
                }
            }
            geary.pipeline.addSystems(
                BlockConditionChecker(),
                ChanceChecker(),
                EntityConditionsChecker(),
                HealthConditionChecker(),
                HeightConditionChecker(),
                LightConditionChecker(),
                TimeConditionChecker(),
                ExplosionSystem(),
                ParseSkills(),
                CreateDefaultSkills(),
                DoSpawnSystem(),
                ReadTargetBlockSystem(),
                RunMMSkillSystem(),
                DoDamageSystem(),
                NearbyEntitiesSelector(),
                ReadLocationSystem(),
                DoKnockbackSystem(),
            )
            geary.pipeline.addSystems(
                CooldownChecker(),
                PlayerConditionsChecker(),
            )

            geary.pipeline.runOnOrAfter(GearyPhase.ENABLE) {
                DI.getOrNull<GearyPaperConfigModule>() ?: return@runOnOrAfter
                gearyPaper.plugin.listeners(
                    ItemBreakBridge(),
                    ItemConsumeBridge(),
                    ItemInteractBridge(),
                    EntitySpawnBridge(),
                )
                gearyPaper.plugin.listeners(
                    EntityDeathBridge(),
                    EntityDamageOtherBridge(),
                    EntityDamagedBridge(),
                    EntityShearedBridge(),
                )
            }
        }

        override fun default() = PaperBridge()
    }
}
