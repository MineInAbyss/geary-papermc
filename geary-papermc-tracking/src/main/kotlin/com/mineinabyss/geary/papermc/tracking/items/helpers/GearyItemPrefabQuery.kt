package com.mineinabyss.geary.papermc.tracking.items.helpers

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyItemPrefabQuery : GearyQuery() {
    val Pointer.key by get<PrefabKey>()
    val Pointer.isMobzy by family {
        has<SetItem>()
        has<Prefab>()
    }
    fun getKeys(): List<PrefabKey> = toList{ it.key }
}
