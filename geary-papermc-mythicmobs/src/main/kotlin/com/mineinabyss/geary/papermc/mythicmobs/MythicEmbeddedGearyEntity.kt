package com.mineinabyss.geary.papermc.mythicmobs

import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import com.mineinabyss.geary.actions.event_binds.EntityObservers
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.papermc.toEntityOrNull
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import io.lumine.mythic.api.mobs.MythicMob

object MythicEmbeddedGearyEntity {
    const val NODE_GEARY = "Geary"
    const val NODE_OBSERVE = "Observe"
    const val LOADED_ENTITY_KEY = "LoadedGearyEntity"

    /**
     * Gets the embedded prefab based on the mob name or parses and loads it.
     */
    fun getOrLoadEmbeddedPrefab(world: Geary, mob: MythicMob): GearyEntity? = with(world) {
        // Check for existing prefab
        val config = mob.config
        val loadedEntity = mob.config.getString(LOADED_ENTITY_KEY)
        if(loadedEntity != null) {
            if (loadedEntity == "none") return null
            val prefab = PrefabKey.ofOrNull(loadedEntity)?.toEntityOrNull()
            if (prefab != null) return prefab
        }

        mob.config.set(LOADED_ENTITY_KEY, "none") // If we return early, don't try to load again

        val prefabKey = PrefabKey.of("mythicmobs", config.key)

        // Load prefab
        // TODO create some helper functions for this in PrefabLoader
        val prefabs = getAddon(Prefabs)
        val yamlFormat = getAddon(SerializableComponents).formats["yml"] as? YamlFormat ?: return@with null
        val mobNode = yamlFormat.regularYaml.parseToYamlNode(config.file.readText())
            .yamlMap.get<YamlMap>(config.key) ?: return null

        // Get special nodes and decode them
        val gearyNode = mobNode.get<YamlMap>(NODE_GEARY)
        val observeNode = mobNode.get<YamlMap>(NODE_OBSERVE)

        if (gearyNode == null && observeNode == null) return@with null

        val serializer = PolymorphicListAsMapSerializer.ofComponents()
        val yaml = yamlFormat.regularYaml
        val components = gearyNode?.let { yaml.decodeFromYamlNode(serializer, it) }
        val observeComponent = observeNode?.let { yaml.decodeFromYamlNode(EntityObservers.serializer(), it) }

        // Register and return
        val newEntity = prefabKey.toEntityOrNull() ?: entity()
        newEntity.clear()
        val prefabEntity = entity {
            components?.forEach {
                set(it, it::class)
            }
            observeComponent?.let { set(it, it::class) }
            set(prefabKey)
        }
        mob.config.set(LOADED_ENTITY_KEY, prefabKey.toString())
        prefabs.manager.registerPrefab(prefabKey, prefabEntity)
        prefabEntity
    }
}
