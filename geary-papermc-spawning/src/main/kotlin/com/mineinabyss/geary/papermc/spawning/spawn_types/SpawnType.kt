package com.mineinabyss.geary.papermc.spawning.spawn_types

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.events.call
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.bukkit.Location

@Serializable(with = SpawnType.Serializer::class)
interface SpawnType {
    val key: String
    val category: SpawnCategory

    fun spawnAt(location: Location)

    object Serializer : InnerSerializer<String, SpawnType>(
        serialName = "geary:spawn_type",
        inner = String.serializer(),
        transform = { getType(it) ?: error("Unknown spawn type $it") },
        inverseTransform = { it.key }
    )

    companion object {
        fun getType(type: String): SpawnType? {
            val event = GearyReadTypeEvent(type)
            event.call()
            return event.spawnType
        }
    }

    object None : SpawnType {
        override val key = "none"
        override val category = SpawnCategory("none")

        override fun spawnAt(location: Location) {}
    }
}
