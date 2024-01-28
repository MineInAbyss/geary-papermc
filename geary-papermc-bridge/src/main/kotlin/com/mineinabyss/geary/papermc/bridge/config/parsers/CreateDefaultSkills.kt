package com.mineinabyss.geary.papermc.bridge.config.parsers

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.datatypes.EntityId
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.papermc.bridge.config.OnEvent
import com.mineinabyss.geary.papermc.bridge.config.Skill
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class CreateDefaultSkills : GearyListener() {
    private val Pointers.prefab by get<PrefabKey>().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val events = mutableMapOf<EntityId, Entity>()
        target.entity.type.forEach { component ->
            val data = target.entity.get(component)
            val onEvents = component.toGeary().getRelations<OnEvent?, Any?>().map { it.target }
            onEvents.forEach { event ->
                events.getOrPut(event) { entity() }.apply {
                    if(data != null) set(data, component)
                    else add(component)
                }
            }
        }
        events.forEach { (event, entity) ->
            entity.set(Skill(execute = entity))
            target.entity.addRelation(event, entity.id)
        }
    }
}
