package com.mineinabyss.geary.papermc.mythicmobs

import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.configureGeary
import com.mineinabyss.geary.papermc.mythicmobs.actions.runMMSkillAction
import com.mineinabyss.geary.papermc.mythicmobs.items.MythicMobDropListener
import com.mineinabyss.geary.papermc.mythicmobs.skills.MythicPrefabsListeners
import com.mineinabyss.geary.papermc.mythicmobs.spawning.markMMAsCustomMob
import com.mineinabyss.geary.papermc.mythicmobs.spawning.mythicMobSpawner
import com.mineinabyss.idofront.features.feature
import org.koin.core.module.dsl.scopedOf

val MythicMobsFeature = feature("mythicMobs") {
    dependsOn {
        condition { get<GearyPaperConfig>().minecraftFeatures }
        plugins("MythicMobs")
    }

    scopedModule {
        scopedOf(::MythicMobDropListener)
        scopedOf(::MythicPrefabsListeners)
    }

    configureGeary {
        onEnable {
            runMMSkillAction()
            mythicMobSpawner()
            markMMAsCustomMob()
        }
    }

    onEnable {
        listeners(
            get<MythicMobDropListener>(),
            get<MythicPrefabsListeners>(),
        )
    }
}
