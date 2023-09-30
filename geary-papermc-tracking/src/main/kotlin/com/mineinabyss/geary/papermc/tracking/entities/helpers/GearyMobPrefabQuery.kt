package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyMobPrefabQuery : GearyQuery() {
    val Pointer.key by get<PrefabKey>()
    val Pointer.isMobzy by family {
        has<Prefab>()
        or {
            has<SetEntityType>()
            has<SetMythicMob>()
        }
    }
    fun getKeys(): List<PrefabKey> = toList { it.key }
}

