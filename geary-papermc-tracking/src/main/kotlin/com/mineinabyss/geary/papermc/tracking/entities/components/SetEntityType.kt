package com.mineinabyss.geary.papermc.tracking.entities.components

import com.mineinabyss.idofront.nms.aliases.NMSEntityType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.world.entity.EntityType

@Serializable
@SerialName("geary:set.entity_type")
class SetEntityType(
    private val key: String,
    // TODO support this via custom entity types in the future
//    val mobCategory: MobCategory? = null,
) {
    @Transient
    val entityTypeFromRegistry: EntityType<*> = NMSEntityType
        .byString(key)
        .orElseGet { error("An entity type with key $key was not found.") }
}
