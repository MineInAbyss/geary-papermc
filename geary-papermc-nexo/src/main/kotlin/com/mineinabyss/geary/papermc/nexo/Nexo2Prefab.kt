package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.prefabs.PrefabKey

/**
 * Prefab a Nexo id was registered from.
 *
 * [nexoId] flattens namespace and key into one string, which cannot be split back apart unambiguously,
 * so the pairing is recorded as prefabs register instead
 */
class Nexo2Prefab {
    private val prefabs = mutableMapOf<String, PrefabKey>()

    operator fun get(nexoId: String): PrefabKey? = prefabs[nexoId]

    operator fun set(nexoId: String, prefabKey: PrefabKey) {
        prefabs[nexoId] = prefabKey
    }

    operator fun contains(nexoId: String): Boolean = nexoId in prefabs
}
