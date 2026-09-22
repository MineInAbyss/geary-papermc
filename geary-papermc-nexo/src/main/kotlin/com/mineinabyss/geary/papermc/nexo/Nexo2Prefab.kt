package com.mineinabyss.geary.papermc.nexo

import com.mineinabyss.geary.prefabs.PrefabKey

/**
 * Prefab a Nexo id was registered from.
 *
 * [nexoId] flattens namespace and key into one string, which cannot be split back apart unambiguously,
 * so the pairing is recorded as prefabs register instead
 */
class Nexo2Prefab {
    private val byNexoId = mutableMapOf<String, PrefabKey>()

    operator fun get(nexoId: String): PrefabKey? = byNexoId[nexoId]

    operator fun set(nexoId: String, prefabKey: PrefabKey) {
        byNexoId[nexoId] = prefabKey
    }

    operator fun contains(nexoId: String): Boolean = nexoId in byNexoId

    /** In registration order, re-registering a key keeps its place */
    val prefabs: Collection<PrefabKey> get() = byNexoId.values
}
