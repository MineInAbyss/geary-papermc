package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.serialization.SerializableComponentsModule

fun SerializableComponentsModule.withTestSerializers() {
    registerComponentSerializers(
        SomeData.serializer()
    )
}
