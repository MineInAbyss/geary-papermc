package com.mineinabyss.geary.papermc.bridge.config

import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import kotlinx.serialization.Serializable

@Serializable(with = SetTarget.Serializer::class)
class SetTarget(val inner: EventComponent) {
    @Transient
    val readerEntity: GearyEntity? = inner.data?.let { entity { set(it, it::class) } }

    class Serializer : InnerSerializer<EventComponent, SetTarget>(
        "geary:using",
        EventComponent.serializer(),
        { SetTarget(it) },
        { it.inner },
    )
}
