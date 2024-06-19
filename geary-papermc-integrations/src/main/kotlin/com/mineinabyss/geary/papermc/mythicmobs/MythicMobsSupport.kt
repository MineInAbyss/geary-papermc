package com.mineinabyss.geary.papermc.mythicmobs

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.queries.cacheGroupedBy
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.mythicmobs.events.runMMSkillAction
import com.mineinabyss.geary.papermc.mythicmobs.items.MythicMobDropListener
import com.mineinabyss.geary.papermc.mythicmobs.spawning.*
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.plugin.listeners

fun GearyModule.mythicMobsSupport() {
    val mythicMobBinds = geary.cacheGroupedBy(query<BindToMythicMob>()) { (type) ->
        entity.addRelation<NoInherit, BindToMythicMob>()
        type.id
    }

    runMMSkillAction()
    mythicMobSpawner()
    markMMAsCustomMob()
    markBindMMAsCustomMob()

    pipeline.runOnOrAfter(GearyPhase.ENABLE) {
        gearyPaper.plugin.listeners(
            MythicMobDropListener(),
            BindToMythicMobSystem(mythicMobBinds),
        )
    }
}
