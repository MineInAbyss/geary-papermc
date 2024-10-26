package com.mineinabyss.geary.papermc.tracking.entities.components

import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.observe
import com.mineinabyss.geary.observers.events.OnSet
import com.mineinabyss.geary.systems.query.query
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

fun Geary.markSetEntityTypeAsCustomMob() = observe<OnSet>()
    .involving(query<SetEntityType>())
    .exec {
        entity.add<ShowInMobQueries>()
        entity.add<SpawnableByGeary>()
    }

fun Geary.markBindEntityTypeAsCustomMob() = observe<OnSet>()
    .involving(query<BindToEntityType>())
    .exec { entity.add<ShowInMobQueries>() }
