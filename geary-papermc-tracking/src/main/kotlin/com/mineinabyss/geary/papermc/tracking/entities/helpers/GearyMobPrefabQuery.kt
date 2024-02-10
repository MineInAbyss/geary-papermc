package com.mineinabyss.geary.papermc.tracking.entities.helpers

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.helpers.contains
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.papermc.tracking.entities.components.SetMythicMob
import com.mineinabyss.geary.papermc.tracking.entities.gearyMobs
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyMobPrefabQuery : GearyQuery() {
    private val mobQuery = family {
        has<Prefab>()
        or {
            has<SetEntityType>()
            has<SetMythicMob>()
        }
    }

    val Pointer.key by get<PrefabKey>()
    val Pointer.isMob by mobQuery

    fun getKeys(): List<PrefabKey> = toList { it.key }
    fun getKeyStrings(): List<String> = toList { it.key.toString() }

    fun isMob(entity: GearyEntity): Boolean {
        return entity.prefabs.any { it.type in mobQuery}
    }
}

