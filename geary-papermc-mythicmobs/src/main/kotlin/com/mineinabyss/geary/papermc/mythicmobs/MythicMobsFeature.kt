package com.mineinabyss.geary.papermc.mythicmobs

import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.gearyWorld
import com.mineinabyss.geary.papermc.mythicmobs.actions.runMMSkillAction
import com.mineinabyss.geary.papermc.mythicmobs.items.MythicMobDropListener
import com.mineinabyss.geary.papermc.mythicmobs.skills.MythicPrefabsListeners
import com.mineinabyss.geary.papermc.mythicmobs.spawning.markMMAsCustomMob
import com.mineinabyss.geary.papermc.mythicmobs.spawning.mythicMobSpawner
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.requirePlugins

val MythicMobsFeature = module("mythicMobs") {
    require(get<GearyPaperConfig>().minecraftFeatures) { "Minecraft features are disabled" }
    requirePlugins("MythicMobs")

    val dropListener by single { new(::MythicMobDropListener) }
    val prefabListener by single { new(::MythicPrefabsListeners) }

    gearyWorld {
        runMMSkillAction()
        mythicMobSpawner()
        markMMAsCustomMob()
    }
    listeners(dropListener, prefabListener)
}
