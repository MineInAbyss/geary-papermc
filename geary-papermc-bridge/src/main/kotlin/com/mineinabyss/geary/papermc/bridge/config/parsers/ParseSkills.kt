package com.mineinabyss.geary.papermc.bridge.config.parsers

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.bridge.config.EventComponent
import com.mineinabyss.geary.papermc.bridge.config.Skills
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class ParseSkills : GearyListener() {
    private var Pointers.skillsComp by get<Skills>().removable().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        skillsComp?.skills?.forEach { skill ->
            val skillEntity = skill.entity
            val eventComponent = skillEntity.get<EventComponent>() ?: run {
                geary.logger.w("Skill defined without an event component. It won't trigger!")
                return
            }
            if (eventComponent.data != null)
                target.entity.setRelation(eventComponent.type, skillEntity.id, eventComponent.data)
            else target.entity.addRelation(eventComponent.type, skillEntity.id)
        }
        skillsComp = null
    }
}
