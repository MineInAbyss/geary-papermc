package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.geary.serialization.SerializableComponentsModule
import com.mineinabyss.geary.serialization.helpers.withSerialName
import com.mineinabyss.idofront.serialization.UUIDSerializer
import java.util.*

fun SerializableComponentsModule.withUUIDSerializer() {
    registerComponentSerializers(
        UUID::class to UUIDSerializer.withSerialName("geary:uuid")
    )
}
