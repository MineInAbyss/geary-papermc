package com.mineinabyss.geary.papermc.features.resourcepacks

import com.charleskorn.kaml.YamlInput
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import com.google.gson.JsonPrimitive
import com.mineinabyss.idofront.serialization.StringsSerializer
import com.mineinabyss.idofront.serialization.KeySerializer
import com.mineinabyss.idofront.serialization.TintSourceSerializer
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.item.RangeDispatchItemModel
import team.unnamed.creative.item.SelectItemModel
import team.unnamed.creative.item.property.ItemBooleanProperty
import team.unnamed.creative.item.property.ItemNumericProperty
import team.unnamed.creative.item.property.ItemStringProperty
import team.unnamed.creative.item.tint.TintSource

@Suppress("UnstableApiUsage")
@Serializable
sealed interface ItemModelDefinition {
    @Serializable
    @SerialName("reference")
    data class Reference(
        val model: @Serializable(KeySerializer::class) Key? = null,
        val tints: List<@Serializable(TintSourceSerializer::class) TintSource> = listOf(),
    ) : ItemModelDefinition

    /** Generates a flat model from [texture], placed at the texture's own path */
    @Serializable
    @SerialName("texture")
    data class Texture(
        val texture: @Serializable(KeySerializer::class) Key,
        val parentModel: @Serializable(KeySerializer::class) Key = Key.key("minecraft:item/generated"),
        val tints: List<@Serializable(TintSourceSerializer::class) TintSource> = listOf(),
    ) : ItemModelDefinition

    /** Extracts the model and its textures out of the .bbmodel at [bbmodel], see [BBModelGenerator] */
    @Serializable
    @SerialName("bbmodel")
    data class BBModel(
        val bbmodel: @Serializable(KeySerializer::class) Key,
        val tints: List<@Serializable(TintSourceSerializer::class) TintSource> = listOf(),
    ) : ItemModelDefinition

    @Serializable
    @SerialName("condition")
    data class Condition(
        val property: BooleanProperty,
        val index: Int = 0,
        val onTrue: @Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition,
        val onFalse: @Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition,
    ) : ItemModelDefinition

    @Serializable
    @SerialName("select")
    data class Select(
        val property: StringProperty,
        val index: Int = 0,
        val cases: List<Case>,
        val fallback: @Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition? = null,
    ) : ItemModelDefinition {
        @Serializable
        data class Case(
            @SerialName("when") val values: @Serializable(StringsSerializer::class) List<String>,
            val model: @Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition,
        )
    }

    @Serializable
    @SerialName("range_dispatch")
    data class RangeDispatch(
        val property: NumericProperty,
        val index: Int = 0,
        val scale: Float = 1f,
        val entries: List<Entry>,
        val fallback: @Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition? = null,
    ) : ItemModelDefinition {
        @Serializable
        data class Entry(
            val threshold: Float,
            val model: @Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition,
        )
    }

    @Serializable
    @SerialName("composite")
    data class Composite(
        val models: List<@Serializable(ItemModelDefinitionSerializer::class) ItemModelDefinition>,
    ) : ItemModelDefinition

    @Serializable
    @SerialName("empty")
    data object Empty : ItemModelDefinition

    @Serializable
    enum class BooleanProperty {
        @SerialName("custom_model_data") CUSTOM_MODEL_DATA,
        @SerialName("broken") BROKEN,
        @SerialName("damaged") DAMAGED,
        @SerialName("using_item") USING_ITEM,
        @SerialName("fishing_rod_cast") FISHING_ROD_CAST,
        @SerialName("selected") SELECTED,
        @SerialName("carried") CARRIED,
        @SerialName("bundle_has_selected_item") BUNDLE_HAS_SELECTED_ITEM,
        @SerialName("extended_view") EXTENDED_VIEW,
        @SerialName("view_entity") VIEW_ENTITY;

        fun toCreative(index: Int): ItemBooleanProperty = when (this) {
            CUSTOM_MODEL_DATA -> ItemBooleanProperty.customModelData(index)
            BROKEN -> ItemBooleanProperty.broken()
            DAMAGED -> ItemBooleanProperty.damaged()
            USING_ITEM -> ItemBooleanProperty.usingItem()
            FISHING_ROD_CAST -> ItemBooleanProperty.fishingRodCast()
            SELECTED -> ItemBooleanProperty.selected()
            CARRIED -> ItemBooleanProperty.carried()
            BUNDLE_HAS_SELECTED_ITEM -> ItemBooleanProperty.bundleHasSelectedItem()
            EXTENDED_VIEW -> ItemBooleanProperty.extendedView()
            VIEW_ENTITY -> ItemBooleanProperty.viewEntity()
        }
    }

    @Serializable
    enum class StringProperty {
        @SerialName("custom_model_data") CUSTOM_MODEL_DATA,
        @SerialName("charge_type") CHARGE_TYPE,
        @SerialName("display_context") DISPLAY_CONTEXT,
        @SerialName("main_hand") MAIN_HAND,
        @SerialName("trim_material") TRIM_MATERIAL,
        @SerialName("context_dimension") CONTEXT_DIMENSION,
        @SerialName("context_entity_type") CONTEXT_ENTITY_TYPE;

        fun toCreative(index: Int): ItemStringProperty = when (this) {
            CUSTOM_MODEL_DATA -> ItemStringProperty.customModelData(index)
            CHARGE_TYPE -> ItemStringProperty.chargeType()
            DISPLAY_CONTEXT -> ItemStringProperty.displayContext()
            MAIN_HAND -> ItemStringProperty.mainHand()
            TRIM_MATERIAL -> ItemStringProperty.trimMaterial()
            CONTEXT_DIMENSION -> ItemStringProperty.contextDimension()
            CONTEXT_ENTITY_TYPE -> ItemStringProperty.contextEntityType()
        }
    }

    @Serializable
    enum class NumericProperty {
        @SerialName("custom_model_data") CUSTOM_MODEL_DATA,
        @SerialName("cooldown") COOLDOWN,
        @SerialName("crossbow_pull") CROSSBOW_PULL,
        @SerialName("use_cycle") USE_CYCLE,
        @SerialName("use_duration") USE_DURATION,
        @SerialName("damage") DAMAGE,
        @SerialName("count") COUNT,
        @SerialName("bundle_fullness") BUNDLE_FULLNESS;

        fun toCreative(index: Int): ItemNumericProperty = when (this) {
            CUSTOM_MODEL_DATA -> ItemNumericProperty.customModelData(index)
            COOLDOWN -> ItemNumericProperty.cooldown()
            CROSSBOW_PULL -> ItemNumericProperty.crossbowPull()
            USE_CYCLE -> ItemNumericProperty.useCycle()
            USE_DURATION -> ItemNumericProperty.useDuration()
            DAMAGE -> ItemNumericProperty.damage()
            COUNT -> ItemNumericProperty.count()
            BUNDLE_FULLNESS -> ItemNumericProperty.bundleFullness()
        }
    }
}

/**
 * Reads an [ItemModelDefinition] from a model key on its own, an untyped branch naming what it builds from,
 * or the full tagged form.
 */
object ItemModelDefinitionSerializer : KSerializer<ItemModelDefinition> {
    private val polymorphic = ItemModelDefinition.serializer()

    override val descriptor: SerialDescriptor = ContextualSerializer(Any::class).descriptor

    override fun deserialize(decoder: Decoder): ItemModelDefinition {
        val input = decoder as? YamlInput ?: return decoder.decodeSerializableValue(polymorphic)
        val node = input.node
        if (node is YamlScalar) return ItemModelDefinition.Reference(model = Key.key(node.content))
        if (node !is YamlMap || node.get<YamlNode>("type") != null)
            return input.yaml.decodeFromYamlNode(polymorphic, node)

        val untyped = when {
            node.get<YamlNode>("texture") != null -> ItemModelDefinition.Texture.serializer()
            node.get<YamlNode>("bbmodel") != null -> ItemModelDefinition.BBModel.serializer()
            else -> ItemModelDefinition.Reference.serializer()
        }
        return input.yaml.decodeFromYamlNode(untyped, node)
    }

    override fun serialize(encoder: Encoder, value: ItemModelDefinition) =
        encoder.encodeSerializableValue(polymorphic, value)
}

/**
 * Turns [ItemModelDefinition]s into creative's [ItemModel]s, building and adding to the pack whatever
 * models and textures the [ItemModelDefinition.Texture] and [ItemModelDefinition.BBModel] branches stand for.
 */
class ItemModelResolver(
    private val builder: ModelBuilder,
    private val fallbackModel: Key,
) {
    fun resolve(definition: ItemModelDefinition): ItemModel = with(definition) {
        when (this) {
            is ItemModelDefinition.Reference -> ItemModel.reference(model ?: fallbackModel, tints)
            is ItemModelDefinition.Texture -> ItemModel.reference(builder.fromTexture(texture, parentModel), tints)
            is ItemModelDefinition.BBModel -> ItemModel.reference(builder.fromBBModel(bbmodel), tints)
            is ItemModelDefinition.Condition -> ItemModel.conditional(property.toCreative(index), resolve(onTrue), resolve(onFalse))
            is ItemModelDefinition.Composite -> ItemModel.composite(models.map(::resolve))
            is ItemModelDefinition.Select -> ItemModel.select(
                property.toCreative(index),
                cases.map { case -> SelectItemModel.Case._case(resolve(case.model), case.values.map(::JsonPrimitive)) },
                fallback?.let(::resolve),
            )
            is ItemModelDefinition.RangeDispatch -> ItemModel.rangeDispatch(
                property.toCreative(index),
                scale,
                entries.map { RangeDispatchItemModel.Entry.entry(it.threshold, resolve(it.model)) },
                fallback?.let(::resolve),
            )
            ItemModelDefinition.Empty -> ItemModel.empty()
        }
    }
}
