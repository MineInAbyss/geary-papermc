package com.mineinabyss.geary.papermc.mythicmobs

import com.mineinabyss.geary.addons.GearyPhase
import com.mineinabyss.geary.addons.dsl.GearyAddonWithDefault
import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.observers.queries.QueryGroupedBy
import com.mineinabyss.geary.observers.queries.cacheGroupedBy
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.mythicmobs.events.runMMSkillAction
import com.mineinabyss.geary.papermc.mythicmobs.items.MythicMobDropListener
import com.mineinabyss.geary.papermc.mythicmobs.spawning.*
import com.mineinabyss.geary.systems.query.ShorthandQuery1
import com.mineinabyss.geary.systems.query.query
import com.mineinabyss.idofront.di.DI
import com.mineinabyss.idofront.plugin.listeners

val mobzyMythicMobs: MythicMobsSupport by DI.observe()

interface MythicMobsSupport {
    val mythicMobBinds: QueryGroupedBy<String, ShorthandQuery1<BindToMythicMob>>

    companion object : GearyAddonWithDefault<MythicMobsSupport> {
        override fun default() = object : MythicMobsSupport {
            override val mythicMobBinds = geary.cacheGroupedBy(query<BindToMythicMob>()) { (type) ->
                entity.addRelation<NoInherit, BindToMythicMob>()
                type.id
            }
        }

        override fun MythicMobsSupport.install(): Unit = geary.run {
            runMMSkillAction()
            mythicMobSpawner()
            markMMAsCustomMob()
            markBindMMAsCustomMob()

            pipeline.runOnOrAfter(GearyPhase.ENABLE) {
                gearyPaper.plugin.listeners(
                    MythicMobDropListener(),
                    BindToMythicMobSystem(),
                )
            }
        }
    }
}
