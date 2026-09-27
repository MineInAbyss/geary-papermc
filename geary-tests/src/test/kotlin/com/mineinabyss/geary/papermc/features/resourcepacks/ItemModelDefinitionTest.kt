package com.mineinabyss.geary.papermc.features.resourcepacks

import com.charleskorn.kaml.PolymorphismStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.item.ConditionItemModel
import team.unnamed.creative.item.ReferenceItemModel
import team.unnamed.creative.item.SelectItemModel
import team.unnamed.creative.metadata.pack.FormatVersion
import team.unnamed.creative.metadata.pack.PackFormat

class ItemModelDefinitionTest {
    // Matches the polymorphism config geary's YamlFormat reads prefabs with
    private val yaml = Yaml(
        configuration = YamlConfiguration(
            encodeDefaults = false,
            polymorphismStyle = PolymorphismStyle.Property,
            polymorphismPropertyName = "type",
        )
    )

    private val format = PackFormat.format(FormatVersion.of(97), FormatVersion.of(99))

    private fun decode(config: String) = yaml.decodeFromString(ItemModelDefinitionSerializer, config.trimIndent())

    private fun resolve(definition: ItemModelDefinition, pack: ResourcePack = ResourcePack.resourcePack()) =
        ItemModelResolver(ModelBuilder(pack, format), Key.key("mineinabyss:fallback")).resolve(definition)

    @Test
    fun `should read a model key on its own`() {
        decode("mineinabyss:dishes/cut_meat") shouldBe
                ItemModelDefinition.Reference(model = Key.key("mineinabyss:dishes/cut_meat"))
    }

    @Test
    fun `should read untyped branches as what they build from`() {
        decode("texture: mineinabyss:dishes/cut_meat_raw") shouldBe
                ItemModelDefinition.Texture(texture = Key.key("mineinabyss:dishes/cut_meat_raw"))
        decode("bbmodel: mineinabyss:dishes/cut_meat") shouldBe
                ItemModelDefinition.BBModel(bbmodel = Key.key("mineinabyss:dishes/cut_meat"))
        decode("model: mineinabyss:dishes/cut_meat") shouldBe
                ItemModelDefinition.Reference(model = Key.key("mineinabyss:dishes/cut_meat"))
    }

    @Test
    fun `should build a condition on custom model data`() {
        val model = resolve(
            decode(
                """
                type: condition
                property: custom_model_data
                index: 0
                onTrue: { model: mineinabyss:dishes/cut_meat_cooked }
                onFalse: { model: mineinabyss:dishes/cut_meat_raw }
                """
            )
        )

        val condition = model.shouldBeInstanceOf<ConditionItemModel>()
        condition.onTrue().shouldBeInstanceOf<ReferenceItemModel>().model() shouldBe
                Key.key("mineinabyss:dishes/cut_meat_cooked")
        condition.onFalse().shouldBeInstanceOf<ReferenceItemModel>().model() shouldBe
                Key.key("mineinabyss:dishes/cut_meat_raw")
    }

    @Test
    fun `should generate a flat model for every texture branch`() {
        val pack = ResourcePack.resourcePack()
        val model = resolve(
            decode(
                """
                type: condition
                property: custom_model_data
                onTrue:
                  type: texture
                  texture: mineinabyss:dishes/cut_meat_cooked
                onFalse:
                  type: texture
                  texture: mineinabyss:dishes/cut_meat_raw
                  parentModel: minecraft:item/handheld
                """
            ),
            pack,
        )

        pack.models().map { it.key() }.toSet() shouldBe setOf(
            Key.key("mineinabyss:dishes/cut_meat_cooked"),
            Key.key("mineinabyss:dishes/cut_meat_raw"),
        )

        val raw = pack.model(Key.key("mineinabyss:dishes/cut_meat_raw")).shouldNotBeNull()
        raw.parent() shouldBe Key.key("minecraft:item/handheld")
        raw.textures().layers().single().key() shouldBe Key.key("mineinabyss:dishes/cut_meat_raw")

        pack.model(Key.key("mineinabyss:dishes/cut_meat_cooked")).shouldNotBeNull()
            .parent() shouldBe Key.key("minecraft:item/generated")

        // The branches point at the models that were just generated
        model.shouldBeInstanceOf<ConditionItemModel>().onTrue()
            .shouldBeInstanceOf<ReferenceItemModel>().model() shouldBe Key.key("mineinabyss:dishes/cut_meat_cooked")
    }

    @Test
    fun `should build a select taking single or listed case values`() {
        val model = resolve(
            decode(
                """
                type: select
                property: custom_model_data
                index: 1
                cases:
                - when: shred
                  model: { texture: mineinabyss:dishes/cut_meat_raw }
                - when: [ stew, soup ]
                  model: mineinabyss:dishes/tosubachi
                fallback: { model: mineinabyss:dishes/cut_meat_cooked }
                """
            )
        )

        val select = model.shouldBeInstanceOf<SelectItemModel>()
        select.cases().map { case -> case.`when`().map { it.asString } } shouldBe
                listOf(listOf("shred"), listOf("stew", "soup"))
        select.fallback().shouldNotBeNull().shouldBeInstanceOf<ReferenceItemModel>().model() shouldBe
                Key.key("mineinabyss:dishes/cut_meat_cooked")
    }

    @Test
    fun `should fall back to the prefab model when a reference names nothing`() {
        resolve(decode("tints: []")).shouldBeInstanceOf<ReferenceItemModel>().model() shouldBe
                Key.key("mineinabyss:fallback")
    }
}
