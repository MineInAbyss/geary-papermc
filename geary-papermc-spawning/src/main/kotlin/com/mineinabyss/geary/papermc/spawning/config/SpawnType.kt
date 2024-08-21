package com.mineinabyss.geary.papermc.spawning.config

import com.mineinabyss.geary.papermc.spawning.components.SpawnCategory
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
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
        transform = { getType(it) },
        inverseTransform = { it.key }
    )

    companion object {
        fun getType(type: String): SpawnType = TODO()
    }
}
