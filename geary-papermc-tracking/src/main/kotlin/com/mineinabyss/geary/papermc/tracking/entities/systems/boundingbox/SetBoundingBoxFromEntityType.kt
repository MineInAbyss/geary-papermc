package com.mineinabyss.geary.papermc.tracking.entities.systems.boundingbox

import com.mineinabyss.geary.components.relations.NoInherit
import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.tracking.entities.components.SetEntityType
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import org.bukkit.util.BoundingBox

fun GearyModule.setBoundingBoxFromEntityType() = listener(
    object : ListenerQuery() {
        val mobType by get<SetEntityType>()
        override fun ensure() {
            event.anySet(::mobType)
            this { not { has<BoundingBox>() } }
        }
    }
).exec {
    entity.addRelation<NoInherit, BoundingBox>()
    entity.set(BoundingBoxHelpers.getForEntityType(mobType.entityTypeFromRegistry))
}
