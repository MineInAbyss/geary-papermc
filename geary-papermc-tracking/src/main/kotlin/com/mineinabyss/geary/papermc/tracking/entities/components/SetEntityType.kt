package com.mineinabyss.geary.papermc.tracking.entities.components

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.nms.aliases.NMSEntityType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.world.entity.EntityType

@JvmInline
@Serializable
@SerialName("geary:set.entity_type")
value class SetEntityType(val key: String) {
    val entityTypeFromRegistry: EntityType<*>
        get() = NMSEntityType
            .byString(key)
            .orElseGet { error("An entity type with key $key was not found.") }
}

fun GearyModule.markSetEntityTypeAsCustomMob() = listener(object : ListenerQuery() {
    val entityType by get<SetEntityType>()
    override fun ensure() = event.anySet(::entityType)
}).exec {
    entity.add<CustomMob>()
}
