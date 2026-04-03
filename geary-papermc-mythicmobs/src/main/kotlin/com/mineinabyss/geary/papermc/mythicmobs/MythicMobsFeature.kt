package com.mineinabyss.geary.papermc.mythicmobs

import com.mineinabyss.features.feature
import com.mineinabyss.features.get
import com.mineinabyss.geary.addons.world
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.mythicmobs.actions.runMMSkillAction
import com.mineinabyss.geary.papermc.mythicmobs.items.MythicMobDropListener
import com.mineinabyss.geary.papermc.mythicmobs.skills.MythicPrefabsListeners
import com.mineinabyss.geary.papermc.mythicmobs.spawning.markMMAsCustomMob
import com.mineinabyss.geary.papermc.mythicmobs.spawning.mythicMobSpawner
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.plugins
import org.kodein.di.bindSingletonOf

val MythicMobsFeature = feature("mythicMobs") {
    dependsOn {
        condition { get<GearyPaperConfig>().minecraftFeatures }
        plugins("MythicMobs")
    }

    dependencies {
        bindSingletonOf(::MythicMobDropListener)
        bindSingletonOf(::MythicPrefabsListeners)
    }

    onEnable {
        world {
            runMMSkillAction()
            mythicMobSpawner()
            markMMAsCustomMob()
        }
        listeners(
            get<MythicMobDropListener>(),
            get<MythicPrefabsListeners>(),
        )
    }
}
