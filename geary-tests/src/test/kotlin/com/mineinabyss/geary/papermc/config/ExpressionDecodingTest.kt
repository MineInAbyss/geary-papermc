package com.mineinabyss.geary.papermc.config

import com.mineinabyss.geary.actions.expressions.Expression
import com.mineinabyss.geary.serialization.formats.YamlFormat
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class ExpressionDecodingTest {
    @Serializable
    data class TestData(
        val name: Expression<String>,
        val age: Expression<Int>,
        val regular: String,
    )

    @Test
    fun `should correctly decode`() {
        val input = """
        {
            "age": "{{ test }}",
            "name": "variable",
            "regular": "{{ asdf }}"
        }
        """.trimIndent()
        Yaml.<TestData>(input) shouldBe TestData(
            name = Expression.Fixed("variable"),
            age = Expression.Evaluate("test"),
            regular = "{{ asdf }}"
        )
    }
}
