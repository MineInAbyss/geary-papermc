package com.mineinabyss.geary.papermc.mythicmobs

import com.mineinabyss.geary.papermc.configure
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.mythicmobs.actions.runMMSkillAction
import com.mineinabyss.geary.papermc.mythicmobs.items.MythicMobDropListener
import com.mineinabyss.geary.papermc.mythicmobs.skills.MythicPrefabsListeners
import com.mineinabyss.geary.papermc.mythicmobs.spawning.markMMAsCustomMob
import com.mineinabyss.geary.papermc.mythicmobs.spawning.mythicMobSpawner
import com.mineinabyss.idofront.features.feature
import org.koin.core.module.dsl.scopedOf

val MythicMobsFeature = feature("mythicMobs") {
    dependsOn {
        plugins("MythicMobs")
    }

    scopedModule {
        scopedOf(::MythicMobDropListener)
        scopedOf(::MythicPrefabsListeners)
    }

    onLoad {
        gearyPaper.configure {
            geary.runMMSkillAction()
            geary.mythicMobSpawner()
            geary.markMMAsCustomMob()
        }
    }

    onEnable {
        listeners(
            get<MythicMobDropListener>(),
            get<MythicPrefabsListeners>(),
        )
    }
}
