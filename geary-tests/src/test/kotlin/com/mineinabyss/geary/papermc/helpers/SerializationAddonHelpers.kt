package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.serialization.SerializableComponentsBuilder

fun SerializableComponentsBuilder.withTestSerializers() {
    components {
        component(SomeData.serializer())
    }
}
