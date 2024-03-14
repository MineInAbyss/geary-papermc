package com.mineinabyss.geary.papermc.bridge.config.parsers

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.OnEvent
import com.mineinabyss.geary.papermc.bridge.config.Skill
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun GearyModule.createDefaultSkillsListener() = listener(
    object : ListenerQuery() {
        val prefab by get<PrefabKey>()
    }
).exec {
    val events = mutableMapOf<EntityId, Entity>()
    entity.type.forEach { component ->
        val data = entity.get(component)
        val onEvents = component.toGeary().getRelations<OnEvent?, Any?>().map { it.target }
        onEvents.forEach { event ->
            events.getOrPut(event) { entity() }.apply {
                if (data != null) set(data, component)
                else add(component)
            }
        }
    }
    events.forEach { (event, entity) ->
        entity.set(Skill(execute = entity))
        this.entity.addRelation(event, entity.id)
    }
}
