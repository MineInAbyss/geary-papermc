@file:Suppress("UnstableApiUsage")

package com.mineinabyss.geary.papermc.features.resourcepacks

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.kyori.adventure.key.Key
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.Metadata
import team.unnamed.creative.metadata.animation.AnimationMeta
import team.unnamed.creative.metadata.pack.PackFormat
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import team.unnamed.creative.texture.Texture
import java.util.Base64

class BBModelGenerator(
    private val pack: ResourcePack,
    private val format: PackFormat,
) {
    private val generated = mutableSetOf<Key>()

    /** Converts the .bbmodel at [key], returning the key of the model it generated */
    fun generate(key: Key): Key {
        // The source file is removed once converted, so a second pass would find nothing
        if (key in generated) return key
        val (path, file) = findFile(key) ?: error("No .bbmodel file found for $key")
        val bbmodel = JsonParser.parseString(file.toUTF8String()).asJsonObject

        val (modelJson, textures) = parse(key, bbmodel)
        ModelSerializer.INSTANCE.deserializeFromJson(modelJson, key, format).addTo(pack)
        textures.forEach { (textureKey, texture) ->
            val meta = texture.animation?.let { Metadata.metadata().addPart(it).build() } ?: Metadata.empty()
            pack.texture(Texture.texture(textureKey, Writable.bytes(texture.data), meta))
        }

        pack.removeUnknownFile(path)
        generated += key
        return key
    }

    /**
     * .bbmodel files sit where their model would, `assets/<namespace>/models/<path>.bbmodel`,
     * though anything whose path resolves to the same key is accepted
     */
    private fun findFile(key: Key): Pair<String, Writable>? {
        val expected = "assets/${key.namespace()}/models/${key.value()}.bbmodel"
        pack.unknownFile(expected)?.let { return expected to it }
        return pack.unknownFiles().entries
            .firstOrNull { (path, _) -> path.endsWith(".bbmodel") && keyOf(path) == key }
            ?.toPair()
    }

    private fun keyOf(path: String): Key? = runCatching {
        val namespace = path.substringAfter("assets/").substringBefore("/")
        val value = path.substringAfter("assets/$namespace/").substringAfter("/").substringBeforeLast(".")
        Key.key(namespace, value)
    }.getOrNull()

    private class BBTexture(val data: ByteArray, val animation: AnimationMeta?)

    private fun parse(modelKey: Key, bbmodel: JsonObject): Pair<JsonObject, Map<Key, BBTexture>> {
        val resolution = bbmodel.getAsJsonObject("resolution")
        val textureWidth = resolution?.int("width") ?: 16
        val textureHeight = resolution?.int("height") ?: 16

        val textureMap = JsonObject()
        val textures = mutableMapOf<Key, BBTexture>()
        bbmodel.array("textures")?.forEachIndexed { index, element ->
            val texture = element.asJsonObject
            val source = texture.string("source") ?: return@forEachIndexed
            val base64 = source.substringAfter("data:image/png;base64,").takeUnless { it == source } ?: return@forEachIndexed
            val bytes = Base64.getDecoder().decode(base64)

            val textureKey = textureKey(modelKey, texture, index)
            textureMap.addProperty(index.toString(), textureKey.asString())
            textures[Key.key(textureKey.namespace(), textureKey.value() + ".png")] =
                BBTexture(bytes, parseAnimation(texture, bytes))
        }

        val modelJson = JsonObject()
        modelJson.add("textures", textureMap)

        bbmodel.getAsJsonArray("elements")?.let {
            modelJson.add("elements", convertElements(it, textureWidth, textureHeight))
        }
        bbmodel.getAsJsonObject("display")?.let { modelJson.add("display", it) }

        // Blockbench stores gui_light top-level, under unhandled_root_fields, or as the front_gui_light flag
        val guiLight = bbmodel.string("gui_light")
            ?: bbmodel.getAsJsonObject("unhandled_root_fields")?.string("gui_light")
            ?: bbmodel.boolean("front_gui_light")?.let { if (it) "front" else "side" }
        if (guiLight in GUI_LIGHTS) modelJson.addProperty("gui_light", guiLight)

        bbmodel.array("outliner")?.also { outliner ->
            val groups = convertGroups(outliner, bbmodel.getAsJsonArray("elements") ?: JsonArray())
            if (groups.size() > 0) modelJson.add("groups", groups)
        }

        return modelJson to textures
    }

    /**
     * Textures land under the model's own path unless the .bbmodel names a folder of its own,
     * which keeps one model's textures together and apart from another's of the same name
     */
    private fun textureKey(modelKey: Key, texture: JsonObject, index: Int): Key {
        val name = texture.string("name")?.removeSuffix(".png") ?: "texture_$index"
        val namespace = texture.string("namespace")?.takeIf { it.isNotBlank() } ?: modelKey.namespace()
        val folder = texture.string("folder")?.takeIf { it.isNotBlank() }?.trim('/') ?: modelKey.value()
        return runCatching { Key.key(namespace, "$folder/$name") }
            .getOrElse { Key.key(modelKey.namespace(), "${modelKey.value()}/$name") }
    }

    private fun convertElements(elements: JsonArray, textureWidth: Int, textureHeight: Int): JsonArray {
        val converted = JsonArray()

        elements.map { it.asJsonObject }.filter { it.string("type") != "locator" }.forEach { element ->
            val out = JsonObject()
            out.add("from", element.array("from") ?: filled(0f))
            out.add("to", element.array("to") ?: filled(16f))
            element.get("shade")?.let { out.add("shade", it) }
            // Per-element block light, 0-15, added in 1.21.4. 0 is the default so it is left out
            element.int("light_emission")?.takeIf { it != 0 }?.let { out.addProperty("light_emission", it.coerceIn(0, 15)) }

            element.array("rotation")?.let { convertRotation(it, element) }?.let { out.add("rotation", it) }
            element.getAsJsonObject("faces")?.let {
                out.add("faces", convertFaces(it, element.boolean("box_uv") == true, textureWidth, textureHeight))
            }

            converted.add(out)
        }

        return converted
    }

    // The pack always targets 26.3, so rotations use the free x/y/z form added in 1.21.11 and never the single axis/angle one
    private fun convertRotation(rotation: JsonArray, element: JsonObject): JsonObject? {
        val (x, y, z) = rotation.map { it.asFloat }
        if (x == 0f && y == 0f && z == 0f) return null

        return JsonObject().apply {
            add("origin", element.array("origin") ?: filled(8f))
            addProperty("rescale", element.boolean("rescale") == true)
            addProperty("x", x)
            addProperty("y", y)
            addProperty("z", z)
        }
    }

    private fun convertFaces(faces: JsonObject, boxUv: Boolean, textureWidth: Int, textureHeight: Int): JsonObject {
        val converted = JsonObject()

        for (direction in DIRECTIONS) {
            val face = faces.getAsJsonObject(direction) ?: continue
            val texture = face.int("texture") ?: continue
            val out = JsonObject()
            out.addProperty("texture", "#$texture")

            face.array("uv")?.let { uv ->
                out.add("uv", if (boxUv) uv else normalizeUv(uv, textureWidth, textureHeight))
            }
            face.int("rotation")?.takeIf { it != 0 }?.let { out.addProperty("rotation", it) }
            face.get("tintindex")?.let { out.add("tintindex", it) }
            face.string("cullface")?.let { out.addProperty("cullface", it) }

            converted.add(direction, out)
        }

        return converted
    }

    private fun normalizeUv(uv: JsonArray, textureWidth: Int, textureHeight: Int) = JsonArray().apply {
        add(uv[0].asFloat * 16f / textureWidth)
        add(uv[1].asFloat * 16f / textureHeight)
        add(uv[2].asFloat * 16f / textureWidth)
        add(uv[3].asFloat * 16f / textureHeight)
    }

    private fun convertGroups(outliner: JsonArray, elements: JsonArray): JsonArray {
        val indexByUuid = elements.mapIndexedNotNull { index, element ->
            element.asJsonObject.string("uuid")?.let { it to index }
        }.toMap()

        return JsonArray().apply {
            outliner.forEach { entry ->
                when {
                    entry.isJsonPrimitive -> indexByUuid[entry.asString]?.let(::add)
                    entry.isJsonObject -> entry.asJsonObject.let { group ->
                        add(JsonObject().apply {
                            addProperty("name", group.string("name") ?: "group")
                            add("origin", group.array("origin") ?: filled(0f))
                            addProperty("color", group.int("color") ?: 0)
                            add("children", convertGroups(group.array("children") ?: JsonArray(), elements))
                        })
                    }
                }
            }
        }
    }

    /**
     * Builds the .mcmeta animation for a texture whose image holds several vertically stacked frames,
     * mirroring Blockbench's own animation detection.
     */
    private fun parseAnimation(texture: JsonObject, image: ByteArray): AnimationMeta? {
        val (width, height) = pngDimensions(image) ?: return null
        if (width <= 0 || height <= width || height % width != 0) return null
        val frameCount = height / width

        val frameTime = texture.int("frame_time")?.takeIf { it > 0 } ?: AnimationMeta.DEFAULT_FRAMETIME
        val interpolate = texture.boolean("frame_interpolate") ?: AnimationMeta.DEFAULT_INTERPOLATE
        val orderType = texture.string("frame_order_type")
        val frameOrder = texture.string("frame_order")?.trim()

        val frames = when {
            orderType == "custom" && !frameOrder.isNullOrBlank() ->
                frameOrder.split(WHITESPACE).mapNotNull { it.toIntOrNull()?.takeIf { frame -> frame in 0 until frameCount } }

            orderType == "backwards" -> (frameCount - 1 downTo 0).toList()
            orderType == "back_and_forth" -> (0 until frameCount) + (frameCount - 2 downTo 1)
            else -> (0 until frameCount).toList()
        }.ifEmpty { (0 until frameCount).toList() }

        return AnimationMeta.animation().frameTime(frameTime).interpolate(interpolate)
            .frames(*frames.toIntArray()).build()
    }

    /** Reads width and height out of a png's IHDR chunk without decoding the image */
    private fun pngDimensions(bytes: ByteArray): Pair<Int, Int>? {
        // 8 byte signature, 4 byte length, 4 byte "IHDR", then width and height as 4 byte big-endian ints
        if (bytes.size < 24) return null
        fun int32(offset: Int) = (bytes[offset].toInt() and 0xFF shl 24) or
                (bytes[offset + 1].toInt() and 0xFF shl 16) or
                (bytes[offset + 2].toInt() and 0xFF shl 8) or
                (bytes[offset + 3].toInt() and 0xFF)
        return int32(16) to int32(20)
    }

    private fun filled(value: Float) = JsonArray().apply { repeat(3) { add(value) } }

    private fun JsonObject.string(key: String) = get(key)?.takeIf { it.isJsonPrimitive }?.asString
    private fun JsonObject.int(key: String) = get(key)?.takeIf { it.isJsonPrimitive }?.asInt
    private fun JsonObject.boolean(key: String) = get(key)?.takeIf { it.isJsonPrimitive }?.asBoolean
    private fun JsonObject.array(key: String): JsonArray? = get(key)?.takeIf(JsonElement::isJsonArray)?.asJsonArray

    companion object {
        private val DIRECTIONS = arrayOf("north", "east", "south", "west", "up", "down")
        private val WHITESPACE = Regex("\\s+")
        private val GUI_LIGHTS = setOf("front", "side")
    }
}
