package com.mineinabyss.geary.papermc.bridge.actions

import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.papermc.tracking.entities.helpers.spawnFromPrefab
import com.mineinabyss.geary.serialization.serializers.SerializableGearyEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.accessors.Pointers
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

class DoSpawnSystem : GearyListener() {
    private val Pointers.spawn by get<DoSpawnEntity>().on(source)

    override fun Pointers.handle() {
        spawn.at.get(this).spawnFromPrefab(spawn.entity.get(this))
    }
}
