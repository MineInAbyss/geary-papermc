package com.mineinabyss.geary.papermc.features.entities.pathfinders

import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

@Serializable(with = Pathfinders.Serializer::class)
data class Pathfinders(
    val pathfinders: List<PathfinderWrapper>,
) {
    object Serializer : InnerSerializer<List<PathfinderWrapper>, Pathfinders>(
        serialName = "geary:pathfinders",
        inner = ListSerializer(PathfinderWrapper.serializer()),
        transform = { Pathfinders(it) },
        inverseTransform = { it.pathfinders }
    )
}
