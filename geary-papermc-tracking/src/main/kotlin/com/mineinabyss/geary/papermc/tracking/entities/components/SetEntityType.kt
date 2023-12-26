package com.mineinabyss.geary.papermc.tracking.entities.components

import com.mineinabyss.idofront.nms.aliases.NMSEntityType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.world.entity.EntityType

@JvmInline
@Serializable
@SerialName("geary:set.entityType")
value class SetEntityType(
    val key: String,
) {
    val entityTypeFromRegistry: EntityType<*> get() = NMSEntityType
        .byString(key)
        .orElseGet { error("An entity type with key $key was not found.") }
}
