package com.mineinabyss.geary.papermc.configlang.systems

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.papermc.configlang.components.Apply
import com.mineinabyss.geary.papermc.configlang.components.ApplyBuilder
import com.mineinabyss.geary.papermc.configlang.helpers.parseEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers

class ParseApply : GearyListener() {
    private val Pointers.apply by get<ApplyBuilder>().whenSetOnTarget()

    @OptIn(UnsafeAccessors::class)
    override fun Pointers.handle() {
        val entity = target.entity
        try {
            entity.addRelation<Apply>(entity.parseEntity(apply.entityExpression))
        } finally {
            entity.remove<ApplyBuilder>()
        }
    }
}
