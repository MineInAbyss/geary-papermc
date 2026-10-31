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
import team.unnamed.creative.item.CompositeItemModel
import team.unnamed.creative.item.ConditionItemModel
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.item.RangeDispatchItemModel
import team.unnamed.creative.item.ReferenceItemModel
import team.unnamed.creative.item.SelectItemModel
import team.unnamed.creative.item.property.ItemBooleanProperty
import team.unnamed.creative.item.tint.TintSource
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

    private fun resolve(config: String, pack: ResourcePack = ResourcePack.resourcePack()) = resolve(decode(config), pack)

    private fun ItemModel.reference() = shouldBeInstanceOf<ReferenceItemModel>().model()

    @Test
    fun `should read a model key on its own`() {
        resolve("mineinabyss:dishes/cut_meat").reference() shouldBe Key.key("mineinabyss:dishes/cut_meat")
    }

    @Test
    fun `should read untyped branches as what they build from`() {
        val pack = ResourcePack.resourcePack()
        resolve("texture: mineinabyss:dishes/cut_meat_raw", pack).reference() shouldBe
                Key.key("mineinabyss:dishes/cut_meat_raw")
        pack.model(Key.key("mineinabyss:dishes/cut_meat_raw")).shouldNotBeNull()
        resolve("model: mineinabyss:dishes/cut_meat").reference() shouldBe Key.key("mineinabyss:dishes/cut_meat")
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
        condition.condition() shouldBe ItemBooleanProperty.customModelData(0)
        condition.onTrue().reference() shouldBe Key.key("mineinabyss:dishes/cut_meat_cooked")
        condition.onFalse().reference() shouldBe Key.key("mineinabyss:dishes/cut_meat_raw")
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
        model.shouldBeInstanceOf<ConditionItemModel>().onTrue().reference() shouldBe
                Key.key("mineinabyss:dishes/cut_meat_cooked")
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
        select.fallback().shouldNotBeNull().reference() shouldBe Key.key("mineinabyss:dishes/cut_meat_cooked")
    }

    @Test
    fun `should fall back to the prefab model when a reference names nothing`() {
        resolve("tints: []").reference() shouldBe Key.key("mineinabyss:fallback")
    }

    @Test
    fun `should pass vanilla properties and tints through to creative`() {
        val model = resolve(
            """
            type: composite
            models:
            - type: condition
              property: has_component
              component: dyed_color
              onTrue:
                type: texture
                texture: mineinabyss:gear/armor/orphan_chestplate_dyeable
                tints:
                - type: dye
                  default: 16777215
              onFalse: { texture: mineinabyss:gear/armor/orphan_chestplate }
            - type: range_dispatch
              property: count
              entries:
              - threshold: 2
                model: mineinabyss:dishes/stack
            """
        )

        val (condition, range) = model.shouldBeInstanceOf<CompositeItemModel>().models()
        condition.shouldBeInstanceOf<ConditionItemModel>().condition() shouldBe ItemBooleanProperty.hasComponent("dyed_color")
        val dyeable = condition.onTrue().shouldBeInstanceOf<ReferenceItemModel>()
        dyeable.model() shouldBe Key.key("mineinabyss:gear/armor/orphan_chestplate_dyeable")
        dyeable.tints() shouldBe listOf(TintSource.dye(16777215))
        range.shouldBeInstanceOf<RangeDispatchItemModel>().entries().single().model().reference() shouldBe
                Key.key("mineinabyss:dishes/stack")
    }

    private fun decodeBuilder(config: String) = yaml.decodeFromString(ItemModelBuilder.Serializer, config.trimIndent())

    @Test
    fun `should read a builder's model inline at the root`() {
        val builder = decodeBuilder(
            """
            key: mineinabyss:misc/music_disc
            type: composite
            models:
            - texture: mineinabyss:misc/music_disc
            - texture: mineinabyss:misc/music_disc_overlay
              tints:
              - type: constant
                value: 12042394
            """
        )

        builder.key shouldBe Key.key("mineinabyss:misc/music_disc")
        val (base, overlay) = resolve(builder.model).shouldBeInstanceOf<CompositeItemModel>().models()
        base.reference() shouldBe Key.key("mineinabyss:misc/music_disc")
        overlay.shouldBeInstanceOf<ReferenceItemModel>().tints() shouldBe listOf(TintSource.constant(12042394))
    }

    @Test
    fun `should still read a builder's model nested under model`() {
        val builder = decodeBuilder(
            """
            key: mineinabyss:misc/music_disc
            model:
              type: condition
              property: custom_model_data
              onTrue: mineinabyss:dishes/cut_meat_cooked
              onFalse: mineinabyss:dishes/cut_meat_raw
            """
        )

        builder.key shouldBe Key.key("mineinabyss:misc/music_disc")
        resolve(builder.model).shouldBeInstanceOf<ConditionItemModel>()
        resolve(decodeBuilder("model: mineinabyss:dishes/cut_meat").model).reference() shouldBe
                Key.key("mineinabyss:dishes/cut_meat")
    }

    @Test
    fun `should read case and entry models written inline`() {
        val model = resolve(
            decode(
                """
                type: select
                property: display_context
                cases:
                - when: gui
                  texture: mineinabyss:dishes/cut_meat_raw
                - when: [ head, hand ]
                  type: range_dispatch
                  property: count
                  entries:
                  - threshold: 2
                    type: model
                    model: mineinabyss:dishes/stack
                    tints:
                    - type: constant
                      value: 16737369
                  - threshold: 4
                    model: mineinabyss:dishes/big_stack
                fallback: mineinabyss:dishes/cut_meat_cooked
                """
            )
        )

        val (gui, held) = model.shouldBeInstanceOf<SelectItemModel>().cases()
        gui.model().reference() shouldBe Key.key("mineinabyss:dishes/cut_meat_raw")
        val (stack, bigStack) = held.model().shouldBeInstanceOf<RangeDispatchItemModel>().entries()
        stack.threshold() shouldBe 2f
        stack.model().shouldBeInstanceOf<ReferenceItemModel>().tints() shouldBe listOf(TintSource.constant(16737369))
        stack.model().reference() shouldBe Key.key("mineinabyss:dishes/stack")
        bigStack.model().reference() shouldBe Key.key("mineinabyss:dishes/big_stack")
    }

    @Test
    fun `should read a single tint without a list`() {
        resolve(
            """
            texture: mineinabyss:misc/music_disc_overlay
            tints: { type: constant, value: 12042394 }
            """
        ).shouldBeInstanceOf<ReferenceItemModel>().tints() shouldBe listOf(TintSource.constant(12042394))
    }
}
