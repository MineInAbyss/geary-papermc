package com.mineinabyss.geary.papermc.mythicmobs

import com.mineinabyss.geary.papermc.Feature
import com.mineinabyss.geary.papermc.FeatureContext
import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.mythicmobs.actions.runMMSkillAction
import com.mineinabyss.geary.papermc.mythicmobs.items.MythicMobDropListener
import com.mineinabyss.geary.papermc.mythicmobs.skills.MythicSkillRegisterListener
import com.mineinabyss.geary.papermc.mythicmobs.spawning.markMMAsCustomMob
import com.mineinabyss.geary.papermc.mythicmobs.spawning.mythicMobSpawner

class MythicMobsFeature(context: FeatureContext) : Feature(context) {
    init {
        pluginDeps("MythicMobs")
    }

    override fun enable() {
        gearyPaper.configure {
            geary.runMMSkillAction()
            geary.mythicMobSpawner()
            geary.markMMAsCustomMob()
        }

        listeners(
            MythicMobDropListener(),
            MythicSkillRegisterListener(),
        )
    }
}
