package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.components.ShowInMobQueries
import com.mineinabyss.geary.papermc.tracking.entities.components.SpawnableByGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.builders.cache
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.geary.systems.query.ShorthandQuery1
import com.mineinabyss.geary.systems.query.query

class GearyMobPrefabQuery : GearyQuery() {
    private val mobQuery = family {
        has<Prefab>()
        has<ShowInMobQueries>()
    }

    val prefabs = geary.cache(query<PrefabKey> { add(mobQuery) })
    val spawnablePrefabs = geary.cache(query<PrefabKey> {
        has<Prefab>()
        has<SpawnableByGeary>()
    })

    fun isMob(entity: GearyEntity): Boolean {
        return entity.prefabs.any { it.type in mobQuery }
    }

    fun isMobPrefab(entity: GearyEntity): Boolean {
        return entity.type in mobQuery
    }
}


fun CachedQueryRunner<ShorthandQuery1<PrefabKey>>.getKeys() = map { it.comp1 }
fun CachedQueryRunner<ShorthandQuery1<PrefabKey>>.getKeyStrings() = map { it.comp1.toString() }
