package com.mineinabyss.geary.papermc.bridge

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.helpers.component
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaperConfigModule
import com.mineinabyss.geary.papermc.bridge.actions.createDoDamageAction
import com.mineinabyss.geary.papermc.bridge.actions.createDoKnockbackAction
import com.mineinabyss.geary.papermc.bridge.actions.createDoSpawnAction
import com.mineinabyss.geary.papermc.bridge.actions.createExplosionAction
import com.mineinabyss.geary.papermc.bridge.conditions.*
import com.mineinabyss.geary.papermc.bridge.conditions.location.createBlockConditionChecker
import com.mineinabyss.geary.papermc.bridge.conditions.location.createHeightConditionChecker
import com.mineinabyss.geary.papermc.bridge.conditions.location.createLightConditionChecker
import com.mineinabyss.geary.papermc.bridge.conditions.location.createTimeConditionChecker
import com.mineinabyss.geary.papermc.bridge.config.OnEvent
import com.mineinabyss.geary.papermc.bridge.config.parsers.createDefaultSkillsListener
import com.mineinabyss.geary.papermc.bridge.config.parsers.createParseSkillsListener
import com.mineinabyss.geary.papermc.bridge.events.entities.*
import com.mineinabyss.geary.papermc.bridge.events.items.ItemBreakBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemConsumeBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemInteractBridge
import com.mineinabyss.geary.papermc.bridge.mythicmobs.createRunMMSkillAction
import com.mineinabyss.geary.papermc.bridge.readers.createLocationReader
import com.mineinabyss.geary.papermc.bridge.readers.createTargetBlockReader
import com.mineinabyss.geary.papermc.bridge.systems.createCooldownDisplaySystem
import com.mineinabyss.geary.papermc.bridge.targetselectors.createNearbyEntitiesSelector
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners

class PaperBridge {
    companion object : GearyAddonWithDefault<PaperBridge> {
        override fun PaperBridge.install() = geary.run {
            geary {
                autoscan(this::class.java.classLoader, "com.mineinabyss.geary.papermc.bridge") {
                    systems()
                }

                component<SetItem>().apply {
                    addRelation<OnEvent, OnSpawn>()
                }
            }
            createBlockConditionChecker()
            createChanceChecker()
            createEntityConditionsChecker()
            createHealthConditionChecker()
            createHeightConditionChecker()
            createLightConditionChecker()
            createTimeConditionChecker()
            createCooldownChecker()
            createPlayerConditionsChecker()

            createExplosionAction()
            createDoSpawnAction()
            createRunMMSkillAction()
            createDoDamageAction()
            createDoKnockbackAction()

            createParseSkillsListener()
            createDefaultSkillsListener()

            createTargetBlockReader()
            createLocationReader()
            createNearbyEntitiesSelector()

            createCooldownDisplaySystem()

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
