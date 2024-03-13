package com.mineinabyss.geary.papermc.bridge.config.parsers

import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.bridge.config.Skills
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery

fun GearyModule.createParseSkillsListener() = listener(
    object : ListenerQuery() {
        val skillsComp by get<Skills>()
        override fun ensure() = event.anySet(::skillsComp)
    }
).exec {
    skillsComp.skills.forEach { skill ->
        val skillEntity = entity {
            set(skill)
        }

        val eventComponent = skill.event ?: run {
            geary.logger.w("Skill defined without an event component. It won't trigger!")
            return@exec
        }
        if (eventComponent.data != null)
            entity.setRelation(eventComponent.type, skillEntity.id, eventComponent.data)
        else entity.addRelation(eventComponent.type, skillEntity.id)
    }
    entity.remove<Skills>()
}
