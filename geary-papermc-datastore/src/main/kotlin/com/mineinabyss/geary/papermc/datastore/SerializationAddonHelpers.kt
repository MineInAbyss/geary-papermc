package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.geary.helpers.withSerialName
import com.mineinabyss.geary.serialization.dsl.SerializableComponentsDSL
import com.mineinabyss.idofront.serialization.UUIDSerializer
import java.util.*

fun SerializableComponentsDSL.withUUIDSerializer() {
    components {
        component(UUID::class, UUIDSerializer.withSerialName("geary:uuid"))
    }
}
