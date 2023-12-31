package com.mineinabyss.geary.papermc.bridge

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.bridge.actions.ExplosionSystem
import com.mineinabyss.geary.papermc.bridge.conditions.checkers.*
import com.mineinabyss.geary.papermc.bridge.config.parsers.CreateDefaultSkills
import com.mineinabyss.geary.papermc.bridge.config.parsers.ParseSkills
import com.mineinabyss.geary.papermc.bridge.events.EntityDeathBridge
import com.mineinabyss.geary.papermc.bridge.events.EntitySpawnBridge
import com.mineinabyss.geary.papermc.bridge.events.ShearedBridge
import com.mineinabyss.geary.papermc.bridge.systems.CooldownDisplaySystem
import com.mineinabyss.geary.papermc.bridge.systems.DeathBridge
import com.mineinabyss.geary.papermc.bridge.events.items.ItemActionsBridge
import com.mineinabyss.geary.papermc.bridge.systems.MobActionsBridge
import com.mineinabyss.geary.papermc.bridge.systems.apply.ApplyAttribute
import com.mineinabyss.geary.papermc.bridge.systems.apply.ApplyDamage
import com.mineinabyss.geary.papermc.bridge.systems.apply.ApplyPotionEffects
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.plugin.listeners

@Deprecated("Rework coming soon!")
class PaperBridge {
    companion object : GearyAddonWithDefault<PaperBridge> {
        override fun PaperBridge.install() {
            geary.pipeline.addSystems(
                CooldownDisplaySystem(),
                ApplyAttribute(),
                ApplyDamage(),
                ApplyPotionEffects(),
                BlockConditionChecker(),
                ChanceChecker(),
                EntityConditionsChecker(),
                HealthConditionChecker(),
                HeightConditionChecker(),
                LightConditionChecker(),
                PlayerConditionsChecker(),
                TimeConditionChecker(),
                ExplosionSystem(),
                ParseSkills(),
                CreateDefaultSkills(),
            )

            geary.pipeline.runOnOrAfter(GearyPhase.ENABLE) {
                gearyPaper.plugin.listeners(
                    DeathBridge(),
                    ItemActionsBridge(),
                    MobActionsBridge(),
                    EntitySpawnBridge(),
                )
                gearyPaper.plugin.listeners(
                    EntityDeathBridge(),
                    ShearedBridge()
                )

            }
        }

        override fun default() = PaperBridge()
    }
}
