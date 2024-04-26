package com.mineinabyss.geary.papermc.tracking.blocks.helpers

import com.mineinabyss.geary.papermc.tracking.blocks.components.SetBlock
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.configuration.components.Prefab
import com.mineinabyss.geary.systems.query.CachedQueryRunner
import com.mineinabyss.geary.systems.query.GearyQuery


class GearyBlockPrefabQuery : GearyQuery() {
    val key by get<PrefabKey>()

    override fun ensure() = this {
        has<SetBlock>()
        has<Prefab>()
    }
}

fun CachedQueryRunner<GearyBlockPrefabQuery>.getKeys() = map { it.key }
