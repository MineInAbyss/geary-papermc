package com.mineinabyss.geary.papermc.config

import com.mineinabyss.geary.annotations.optin.UnsafeAccessors
import com.mineinabyss.geary.datatypes.GearyEntity
import com.mineinabyss.geary.helpers.entity
import com.mineinabyss.geary.modules.TestEngineModule
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.bridge.PaperBridge
import com.mineinabyss.geary.papermc.bridge.config.EventComponent
import com.mineinabyss.geary.papermc.bridge.config.Skills
import com.mineinabyss.geary.papermc.bridge.config.inputs.Input
import com.mineinabyss.geary.papermc.bridge.config.inputs.Variables
import com.mineinabyss.geary.papermc.bridge.events.EventHelpers
import com.mineinabyss.geary.prefabs.Prefabs
import com.mineinabyss.geary.prefabs.configuration.components.ChildrenOnPrefab
import com.mineinabyss.geary.serialization.dsl.serializableComponents
import com.mineinabyss.geary.serialization.dsl.serialization
import com.mineinabyss.geary.serialization.formats.YamlFormat
import com.mineinabyss.geary.serialization.serializers.GearyEntitySerializer
import com.mineinabyss.geary.serialization.serializers.SerializableGearyEntity
import com.mineinabyss.geary.systems.GearyListener
import com.mineinabyss.geary.systems.builders.listener
import com.mineinabyss.geary.systems.query.ListenerQuery
import com.mineinabyss.idofront.di.DI
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test

class VariablesConfigTest {
    init {
        geary(TestEngineModule) {
            serialization {
                components {
                    component(Variables.serializer())
                    component(Int.serializer())
                    component(Skills.serializer())
                    component(TestComp.serializer())
                    component(TestCompEntity.serializer())
                    component(TestEvent.serializer())
                    component(EventComponent.serializer())
                    component(GenerateInt.serializer())
                    component(ChildrenOnPrefab.serializer())
                }
            }
            install(Prefabs)
            install(PaperBridge)
        }
        geary.pipeline.runStartupTasks()
    }

    val yamlFormat = YamlFormat(serializableComponents.serializers.module)
    val serializer = GearyEntitySerializer

    @Serializable
    @SerialName("geary:on.test")
    class TestEvent()

    @Serializable
    @SerialName("geary:test_comp")
    class TestComp(val input: Input<Int>)

    @Serializable
    @SerialName("geary:test_comp_entity")
    class TestCompEntity(val entity: Input<SerializableGearyEntity>)

    @Serializable
    @SerialName("geary:generate_int")
    class GenerateInt(val generate: Int)

    @OptIn(UnsafeAccessors::class)
    fun testReadCorrectly(
        @Language("yaml") config: String
    ) {
        // arrange
        val prefab = yamlFormat.decodeFromString(serializer, config)
        var result: Int? = null

        geary.listener(object : ListenerQuery() {
            val input1 by source.get<TestComp>()
        }).exec {
            result = input1.input.get(this)
        }

        geary.listener(object : ListenerQuery() {
            val comp by source.get<GenerateInt>()
        }).exec {
            event.entity.set(comp.generate)
        }

        // act
        EventHelpers.runSkill<TestEvent>(entity { extend(prefab) })

        // assert
        result shouldBe 1
    }

    @Test
    fun `should read inline input correctly`() {
        // arrange
        val config = """
            namespaces: [ geary ]
            skills:
                - event: on.test
                  testComp:
                    input: 1
        """.trimIndent()
        testReadCorrectly(config)
    }

    @Test
    fun `should read variable reference input correctly`() {
        val config = """
            namespaces: [ geary ]
            skills:
                - event: on.test
                  vars:
                    - kotlin.Int test: 1
                  testComp:
                    input: ${'$'}test
        """.trimIndent()
        testReadCorrectly(config)
    }

    @Test
    fun `should read inline derived input correctly`() {
        val config = """
            namespaces: [ geary ]
            skills:
                - event: on.test
                  vars:
                    - kotlin.Int test: 1
                  testComp:
                    input:
                      ${'$'}derived:
                        generateInt:
                          generate: 1
        """.trimIndent()
        testReadCorrectly(config)
    }

    @Test
    fun `should read referenced derived input correctly`() {
        val config = """
            namespaces: [ geary ]
            skills:
                - event: on.test
                  vars:
                    - derived kotlin.Int test:
                          generateInt:
                            generate: 1
                  testComp:
                    input: ${'$'}test
        """.trimIndent()
        testReadCorrectly(config)
    }

    //TODO decide on how child lookups should work
//    @Test
//    fun `should read entity lookup correctly`() {
//        val config = """
//            namespaces: [ geary ]
//            children:
//              child: {}
//            skills:
//                - event: on.test
//                  testCompEntity:
//                    entity: ${'$'}lookup(child)
//        """.trimIndent()
//
//        val prefab = yamlFormat.decodeFromString(serializer, config)
//
//        var result: GearyEntity? = null
//        val skillSystem = object : GearyListener() {
//            val Pointers.comp by get<TestCompEntity>().on(source)
//
//            override fun Pointers.handle() {
//                result = comp.entity.get(this)
//            }
//        }
//        geary.pipeline.addSystems(skillSystem)
//
//        // act
//        EventHelpers.runSkill<TestEvent>(entity { extend(prefab) })
//
//        // assert
//        result.shouldNotBeNull()
//    }

    @Test
    fun `should read entity variable reference correctly`() {
        val config = """
            namespaces: [ geary ]
            skills:
                - event: on.test
                  vars:
                    - entity test: {}
                  testCompEntity:
                    entity: ${'$'}test
        """.trimIndent()

        val prefab = yamlFormat.decodeFromString(serializer, config)

        var result: GearyEntity? = null

        geary.listener(object : ListenerQuery() {
            val comp by source.get<TestCompEntity>()
        }).exec {
            result = comp.entity.get(this)
        }

        // act
        EventHelpers.runSkill<TestEvent>(entity { extend(prefab) })

        // assert
        result.shouldNotBeNull()
    }

    @AfterAll
    fun clearMocks() {
        DI.clear()
    }
}
