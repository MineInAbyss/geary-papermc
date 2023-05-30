package com.mineinabyss.geary.papermc.helpers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("test:some_data")
data class SomeData(val value: String) {
    companion object {
        const val SERIAL_NAME = "test:some_data"
    }
}
