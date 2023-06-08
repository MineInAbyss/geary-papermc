package com.mineinabyss.geary.papermc.tracking.items.helpers

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.items.components.SetItem
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyItemPrefabQuery : GearyQuery() {
    val TargetScope.key by get<PrefabKey>()
    val TargetScope.isMobzy by family {
        has<SetItem>()
        has<Prefab>()
    }
    fun getKeys(): Sequence<PrefabKey> = asSequence().run { map { it.key } }
}
