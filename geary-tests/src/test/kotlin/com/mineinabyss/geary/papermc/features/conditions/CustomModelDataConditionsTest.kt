package com.mineinabyss.geary.papermc.features.conditions

import com.charleskorn.kaml.Yaml
import com.mineinabyss.geary.papermc.features.common.conditions.items.CustomModelDataConditions
import io.kotest.matchers.shouldBe
import net.kyori.adventure.util.TriState
import org.junit.jupiter.api.Test

class CustomModelDataConditionsTest {
    private fun decode(yaml: String) = Yaml.default.decodeFromString(CustomModelDataConditions.serializer(), yaml)

    @Test
    fun `should read single values`() {
        decode("strings: shred\nflags: true\nfloats: 1.0..2.0") shouldBe CustomModelDataConditions(
            strings = listOf("shred"),
            flags = listOf(TriState.TRUE),
            floats = listOf(1.0f..2.0f),
        )
    }

    @Test
    fun `should read lists, skipping unset indices`() {
        decode("strings: [ shred, cooked ]\nflags: [ not_set, true ]") shouldBe CustomModelDataConditions(
            strings = listOf("shred", "cooked"),
            flags = listOf(TriState.NOT_SET, TriState.TRUE),
        )
    }
}
