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

    private class Box(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val translation: Vector3Float = Vector3Float.ZERO, val scale: Float = 1f)

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
            addModel(modelKey, key(orientation, "thumb"), place(orientation, thumbBox(offset)))
            RangeDispatchItemModel.Entry.entry(offset.toFloat(), ItemModel.reference(modelKey))
        }
        val thumb = ItemModel.rangeDispatch(ItemNumericProperty.customModelData(0), 1f, thumbs, ItemModel.empty())
        val thumbOnLastSlot = SelectItemModel.Case._case(thumb, listOf(JsonPrimitive("end"), JsonPrimitive("single")))
        return ItemModel.composite(select(1, pieces), select(1, listOf(thumbOnLastSlot)))
    }

    private fun select(index: Int, cases: List<SelectItemModel.Case>) =
        ItemModel.select(ItemStringProperty.customModelData(index), cases, ItemModel.empty())

    // Boxes are laid out vertically, horizontal maps model (x, y) to (16 - y, x) to mirror them across the slot's diagonal like its textures
    private fun place(orientation: Orientation, box: Box): Box = when (orientation) {
        Orientation.VERTICAL -> box
        Orientation.HORIZONTAL -> Box(16 - box.y1, box.x0, 16 - box.y0, box.x1, Vector3Float(-box.translation.y(), box.translation.x(), 0f), box.scale)
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
            .display(mapOf(ItemTransform.Type.GUI to ItemTransform.transform(Vector3Float.ZERO, box.translation, Vector3Float(box.scale, box.scale, box.scale))))
            .build()
            .addTo(resourcePack)
    }

    companion object {
        private val PIECES = listOf("start", "middle", "end", "single")

        // Covers a full chest row, the farthest a 9 slot track moves its thumb
        private const val MAX_THUMB_OFFSET = 8 * 18 - 3

        private val TRACK = Box(-2f, -1f, 18f, 17f)

        private fun up(pixels: Float) = Vector3Float(0f, pixels, 0f)

        private fun thumbBox(offset: Int): Box = when {
            offset <= 80 -> Box(2f, 3f, 14f, 18f, up(offset.toFloat()))
            // Display translation is clamped to 80 px, so the element itself is raised 14 px
            offset <= 94 -> Box(2f, 17f, 14f, 32f, up(offset - 14f))
            // Elements are clamped to -16..32 as well, so the farthest offsets draw a third size thumb at scale 3 around the model centre
            else -> {
                val translation = (80 downTo 78).first { (offset - 5 - it) % 3 == 0 }
                val y = 8f + (offset - 5 - translation) / 3
                Box(6f, y, 10f, y + 5, up(translation.toFloat()), 3f)
            }
        }
    }
}
