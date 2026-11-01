package com.mineinabyss.geary.papermc.features.resourcepacks

import com.google.gson.JsonPrimitive
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.CubeFace
import team.unnamed.creative.base.Vector3Float
import team.unnamed.creative.item.Item
import team.unnamed.creative.item.ItemModel
import team.unnamed.creative.item.RangeDispatchItemModel
import team.unnamed.creative.item.SelectItemModel
import team.unnamed.creative.item.property.ItemBooleanProperty
import team.unnamed.creative.item.property.ItemNumericProperty
import team.unnamed.creative.item.property.ItemStringProperty
import team.unnamed.creative.model.Element
import team.unnamed.creative.model.ElementFace
import team.unnamed.creative.model.ItemTransform
import team.unnamed.creative.model.Model
import team.unnamed.creative.model.ModelTexture
import team.unnamed.creative.model.ModelTextures
import team.unnamed.creative.texture.TextureUV

/**
 * Builds the item definition and models for guiy's VerticalScrollbarTrack and HorizontalScrollbarTrack, see guiy's ScrollbarTrack.kt for the custom model data they set.
 * Textures are read from `<model>/<orientation>/<piece>` where piece is `start`, `middle`, `end`, `single` or `thumb`, so only the PNGs have to be in a pack
 */
class ScrollbarModels(private val resourcePack: ResourcePack, private val model: Key) {
    private enum class Orientation { VERTICAL, HORIZONTAL }

    private class Box(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val translation: Vector3Float = Vector3Float.ZERO)

    fun generate() {
        if (resourcePack.item(model) != null) return
        val orientations = Orientation.entries.map { orientation ->
            SelectItemModel.Case._case(track(orientation), JsonPrimitive(orientation.id))
        }
        resourcePack.item(Item.item(model, select(0, orientations), true, true))
    }

    private val Orientation.id get() = name.lowercase()

    private fun key(orientation: Orientation, path: String) = Key.key(model.namespace(), "${model.value()}/${orientation.id}/$path")

    private fun track(orientation: Orientation): ItemModel {
        val pieces = PIECES.map { piece ->
            val modelKey = key(orientation, piece)
            addModel(modelKey, key(orientation, piece), place(orientation, TRACK))
            SelectItemModel.Case._case(ItemModel.reference(modelKey), JsonPrimitive(piece))
        }
        val thumbs = (0..MAX_THUMB_OFFSET).map { offset ->
            val modelKey = key(orientation, "thumb/$offset")
            addModel(modelKey, key(orientation, "thumb"), place(orientation, THUMB.raised(offset.toFloat())))
            RangeDispatchItemModel.Entry.entry(offset.toFloat(), ItemModel.reference(modelKey))
        }
        val thumb = ItemModel.rangeDispatch(ItemNumericProperty.customModelData(0), 1f, thumbs, ItemModel.empty())
        return ItemModel.composite(select(1, pieces), ItemModel.conditional(ItemBooleanProperty.customModelData(0), thumb, ItemModel.empty()))
    }

    private fun select(index: Int, cases: List<SelectItemModel.Case>) =
        ItemModel.select(ItemStringProperty.customModelData(index), cases, ItemModel.empty())

    // Boxes are laid out vertically, horizontal maps model (x, y) to (16 - y, x) to mirror them across the slot's diagonal like its textures
    private fun place(orientation: Orientation, box: Box): Box = when (orientation) {
        Orientation.VERTICAL -> box
        Orientation.HORIZONTAL -> Box(16 - box.y1, box.x0, 16 - box.y0, box.x1, Vector3Float(-box.translation.y(), box.translation.x(), 0f))
    }

    private fun addModel(key: Key, texture: Key, box: Box) {
        Model.model()
            .key(key)
            .textures(ModelTextures.builder().variables(mapOf("face" to ModelTexture.ofKey(texture))).particle(ModelTexture.ofKey(texture)).build())
            .elements(
                Element.element()
                    .from(box.x0, box.y0, 8f)
                    .to(box.x1, box.y1, 8f)
                    .addFace(CubeFace.NORTH, ElementFace.face().uv(TextureUV.uv(1f, 0f, 0f, 1f)).texture("#face").build())
                    .addFace(CubeFace.SOUTH, ElementFace.face().uv(TextureUV.uv(0f, 0f, 1f, 1f)).texture("#face").build())
                    .build()
            )
            .guiLight(Model.GuiLight.FRONT)
            .display(mapOf(ItemTransform.Type.GUI to ItemTransform.transform(Vector3Float.ZERO, box.translation, Vector3Float.ONE)))
            .build()
            .addTo(resourcePack)
    }

    companion object {
        private val PIECES = listOf("start", "middle", "end", "single")

        // The thumb only moves within the 18 px slot pitch, past that guiy hands it to the next slot
        private const val MAX_THUMB_OFFSET = 17

        private val TRACK = Box(-2f, -1f, 18f, 17f)
        private val THUMB = Box(2f, 3f, 14f, 18f)

        private fun Box.raised(pixels: Float) = Box(x0, y0, x1, y1, Vector3Float(0f, pixels, 0f))
    }
}
