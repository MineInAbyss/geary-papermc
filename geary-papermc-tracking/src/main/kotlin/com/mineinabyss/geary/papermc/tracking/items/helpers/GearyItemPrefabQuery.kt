package com.mineinabyss.geary.papermc.tracking.items.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.CachedQuery
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyItemPrefabQuery(world: Geary) : GearyQuery(world) {
    val key by get<PrefabKey>()
    override fun ensure() = this { add(itemQuery) }

    private val itemQuery = family {
        has<SetItem>()
        has<Prefab>()
    }

    fun isItem(entity: GearyEntity): Boolean {
        return entity.prefabs.any { it.type in itemQuery }
    }
}

fun CachedQuery<GearyItemPrefabQuery>.getKeys(): List<PrefabKey> = map { it.key }
