package com.mineinabyss.geary.papermc.tracking.items.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyItemPrefabQuery : GearyQuery() {

    val key by get<PrefabKey>()
    override fun ensure() = this { add(itemQuery) }

    companion object {
        private val itemQuery = family {
            has<SetItem>()
            has<Prefab>()
        }

        fun CachedQueryRunner<GearyItemPrefabQuery>.getKeys(): List<PrefabKey> = map { it.key }

        fun isItem(entity: GearyEntity): Boolean {
            return entity.prefabs.any { it.type in itemQuery }
        }
    }
}
