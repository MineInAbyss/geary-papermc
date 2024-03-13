package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyMobPrefabQuery : GearyQuery() {

    val key by get<PrefabKey>()
    override fun ensure() = this { add(mobQuery) }

    companion object {

        fun CachedQueryRunner<GearyMobPrefabQuery>.getKeys(): List<PrefabKey> = map { key }
        fun CachedQueryRunner<GearyMobPrefabQuery>.getKeyStrings(): List<String> = map { key.toString() }

        private val mobQuery = family {
            has<Prefab>()
            or {
                has<SetEntityType>()
                has<SetMythicMob>()
            }
        }

        fun isMob(entity: GearyEntity): Boolean {
            return entity.prefabs.any { it.type in mobQuery }
        }
    }
}

