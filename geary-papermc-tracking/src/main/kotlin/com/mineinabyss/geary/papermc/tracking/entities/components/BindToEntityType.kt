package com.mineinabyss.geary.papermc.tracking.entities.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EntityType

@JvmInline
@Serializable
@SerialName("geary:bind.entity_type")
value class BindToEntityType(val key: String) {
    val entityTypeFromRegistry: EntityType<*>
        get() = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.tryParse(key))
}
