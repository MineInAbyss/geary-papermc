package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.datatypes.family.family
import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.accessors.TargetScope
import com.mineinabyss.geary.systems.query.GearyQuery

class GearyBlockPrefabQuery : GearyQuery() {
    val TargetScope.key by get<PrefabKey>()
    val TargetScope.isMobzy by family {
        has<SetBlock>()
        has<Prefab>()
    }
    fun getKeys(): Sequence<PrefabKey> = asSequence().run { map { it.key } }
}
