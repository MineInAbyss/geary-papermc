package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery
import com.mineinabyss.idofront.messaging.logError

class GearyMobPrefabQuery : GearyQuery() {
    val TargetScope.key by get<PrefabKey>()
    val TargetScope.isMobzy by family {
        has<Prefab>()
        or {
            has<SetEntityType>()
            has<SetMythicMob>()
        }
    }
    fun getKeys(): Sequence<PrefabKey> = asSequence().run { map { it.key } }
}

