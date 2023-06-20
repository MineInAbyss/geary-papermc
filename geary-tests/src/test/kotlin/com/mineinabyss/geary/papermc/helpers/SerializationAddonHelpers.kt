package com.mineinabyss.geary.papermc.helpers

import com.mineinabyss.geary.serialization.dsl.SerializableComponentsDSL


fun SerializableComponentsDSL.withTestSerializers() {
    components {
        component(SomeData.serializer())
    }
}
