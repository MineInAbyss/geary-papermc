package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.modules.GearyModule
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.serialization.serializers.SerializableGearyEntity
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bukkit.Location

@Serializable
@SerialName("geary:spawn")
class DoSpawnEntity(
    val entity: Input<SerializableGearyEntity>,
    val at: Input<@Contextual Location>
)


fun GearyModule.createDoSpawnAction() = listener(
    object : ListenerQuery() {
        val spawn by source.get<DoSpawnEntity>()
    }
).exec {
    spawn.at.get(this).spawnFromPrefab(spawn.entity.get(this))
}
