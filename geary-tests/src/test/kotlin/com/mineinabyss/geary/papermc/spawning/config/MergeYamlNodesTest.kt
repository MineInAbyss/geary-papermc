package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.Yaml
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MergeYamlNodesTest {
    @Test
    fun `should correctly merge $remove in lists`() {
        val yaml = Yaml.default
        val original = yaml.parseToYamlNode(
            """
            test:
              - a: {}
              - b: {}
        """.trimIndent()
        )

        val override = yaml.parseToYamlNode(
            """
            test:
              - ${'$'}inherit
              - ${'$'}remove a
        """.trimIndent()
        )

        SpawnEntryReader.mergeYamlNodes(original, override).contentToString() shouldBe "{'test': [{'b': {}}]}"
    }
}
