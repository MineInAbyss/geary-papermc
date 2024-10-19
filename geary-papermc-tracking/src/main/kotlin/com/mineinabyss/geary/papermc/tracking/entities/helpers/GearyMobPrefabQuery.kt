package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.entities.components.ShowInMobQueries
import com.mineinabyss.geary.papermc.tracking.entities.components.SpawnableByGeary
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.geary.systems.query.ShorthandQuery1
import com.mineinabyss.geary.systems.query.query

class GearyMobPrefabQuery(world: Geary) : GearyQuery(world) {
    private val mobQuery = family {
        has<Prefab>()
        has<ShowInMobQueries>()
    }

    val prefabs = cache(query<PrefabKey> { add(mobQuery) })
    val spawnablePrefabs = cache(query<PrefabKey> {
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


fun CachedQuery<ShorthandQuery1<PrefabKey>>.getKeys() = map { it.comp1 }
fun CachedQuery<ShorthandQuery1<PrefabKey>>.getKeyStrings() = map { it.comp1.toString() }
