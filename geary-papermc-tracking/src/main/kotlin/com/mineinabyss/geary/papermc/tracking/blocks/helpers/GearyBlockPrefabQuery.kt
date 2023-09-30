package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.Pointer
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyBlockPrefabQuery : GearyQuery() {
    val Pointer.key by get<PrefabKey>()
    val Pointer.isMobzy by family {
        has<SetBlock>()
        has<Prefab>()
    }
    fun getKeys(): List<PrefabKey> = toList { it.key }
}
