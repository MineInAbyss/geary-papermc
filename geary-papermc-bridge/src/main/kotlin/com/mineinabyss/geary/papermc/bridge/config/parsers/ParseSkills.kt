package com.mineinabyss.geary.papermc.bridge.config.parsers

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.papermc.bridge.config.Skills
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class ParseSkills : GearyListener() {
    private var Pointers.skillsComp by get<Skills>().removable().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        skillsComp?.skills?.forEach { skill ->
            val skillEntity = entity {
                setAll(skill.run)
            }
            if (skill.eventComponent.data != null)
                target.entity.setRelation(skill.eventComponent.type, skillEntity.id, skill.eventComponent.data)
            else target.entity.addRelation(skill.eventComponent.type, skillEntity.id)
        }
        skillsComp = null
    }
}
