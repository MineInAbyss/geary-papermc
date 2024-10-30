package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.geary.serialization.SerializableComponentsBuilder
import com.mineinabyss.geary.serialization.helpers.withSerialName
import com.mineinabyss.idofront.serialization.UUIDSerializer
import java.util.*

fun SerializableComponentsBuilder.withUUIDSerializer() {
    components {
        component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
    }
}
