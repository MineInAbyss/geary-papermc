package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.Handler
import com.mineinabyss.geary.papermc.commons.events.configurable.components.Apply
import com.mineinabyss.geary.papermc.commons.events.configurable.components.ApplyBuilder
import com.mineinabyss.geary.papermc.configlang.helpers.parseEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.TargetScope

class ParseApply : GearyListener() {
    private val TargetScope.apply by onSet<ApplyBuilder>()

    @Handler
    private fun TargetScope.convertToRelation() {
        try {
            entity.addRelation<Apply>(entity.parseEntity(apply.entityExpression))
        } finally {
            entity.remove<ApplyBuilder>()
        }
    }
}
