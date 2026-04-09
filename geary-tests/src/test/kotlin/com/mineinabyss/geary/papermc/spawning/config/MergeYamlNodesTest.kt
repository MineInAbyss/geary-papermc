package com.mineinabyss.geary.papermc.spawning.config

//TODO move tests to idofront
class MergeYamlNodesTest {
//    private fun testMerge(
//        @Language("yaml") original: String,
//        @Language("yaml") override: String,
//        @Language("yaml") expect: String,
//    ) {
//        val yaml = Yaml.default
//        val originalNode = yaml.parseToYamlNode(original)
//        val overrideNode = yaml.parseToYamlNode(override)
//        MultiEntryYamlReader.mergeYamlNodes(originalNode, overrideNode).contentToString() shouldBe expect
//    }
//
//    @Test
//    fun `should correctly merge $remove in lists`() = testMerge(
//        original = """
//            test:
//              - a: {}
//              - b: {}
//            """.trimIndent(),
//        override = """
//            test:
//              - ${'$'}inherit
//              - ${'$'}remove a
//            """.trimIndent(),
//        expect = "{'test': [{'b': {}}]}"
//    )
//
//    @Test
//    fun `should ignore list $inherit when it doesn't exist on the parent node`() = testMerge(
//        original = """
//            test: {}
//            """.trimIndent(),
//        override = """
//            test:
//              conditions:
//                  - ${'$'}inherit
//                  - a key
//                  - ${'$'}remove a
//            """.trimIndent(),
//        expect = "{'test': {'conditions': ['a key']}}",
//    )
}
