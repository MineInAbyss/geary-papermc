package com.mineinabyss.geary.papermc.tracking.items.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyItemPrefabQuery : GearyQuery() {
    private val itemQuery = family {
        has<SetItem>()
        has<Prefab>()
    }

    val Pointer.key by get<PrefabKey>()
    val Pointer.isItem by itemQuery

    fun getKeys(): List<PrefabKey> = toList{ it.key }

    fun isItem(entity: GearyEntity): Boolean {
        return entity.prefabs.any { it.type in itemQuery }
    }
}
