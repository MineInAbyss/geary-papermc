package com.mineinabyss.geary.papermc.bridge.helpers

import com.mineinabyss.geary.datatypes.Entity
import com.mineinabyss.geary.helpers.parent
import com.mineinabyss.geary.helpers.toGeary
import com.mineinabyss.geary.serialization.helpers.componentId

//TODO this should be handled within a serializer of sorts for GearyEntity
fun Entity.parseEntity(expression: String): Entity = when {
    expression.startsWith("parent") -> {
        val parent = (parent ?: error("Failed to read expression, entity had no parent: $expression"))
        if (expression.startsWith("parent."))
            parent.parseEntity(expression.removePrefix("parent."))
        else parent
    }

    expression.startsWith("lookup") -> {
        val innerExpr = expression.substringAfter('(').substringBefore(')')
        lookup(innerExpr) ?: error("Failed to find entity with $expression")
    }

    expression.contains(':') -> componentId(expression).toGeary()
    else -> error("Malformed expression for getting entity: $expression")
}
