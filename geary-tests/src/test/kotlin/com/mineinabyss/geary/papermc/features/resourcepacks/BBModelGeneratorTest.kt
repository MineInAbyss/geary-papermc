package com.mineinabyss.geary.papermc.features.resourcepacks

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.FormatVersion
import team.unnamed.creative.metadata.pack.PackFormat
import java.util.Base64

class BBModelGeneratorTest {
    private val format = PackFormat.format(FormatVersion.of(97), FormatVersion.of(99))
    private val modelKey = Key.key("mineinabyss:dishes/cut_meat")

    /** A 1x1 png, enough for the header the animation check reads */
    private val png = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="
    )

    private fun bbmodel(textureName: String = "cut_meat") = """
        {
          "resolution": { "width": 32, "height": 32 },
          "textures": [
            { "name": "$textureName.png", "source": "data:image/png;base64,${Base64.getEncoder().encodeToString(png)}" }
          ],
          "elements": [
            {
              "type": "cube",
              "uuid": "abc",
              "from": [0, 0, 0],
              "to": [16, 16, 16],
              "rotation": [22.5, 0, 0],
              "origin": [8, 8, 8],
              "faces": { "north": { "texture": 0, "uv": [0, 0, 16, 16] } }
            },
            { "type": "locator", "uuid": "def" }
          ],
          "outliner": [ "abc" ],
          "gui_light": "front"
        }
    """.trimIndent()

    private fun packWith(path: String, content: String) = ResourcePack.resourcePack().apply {
        unknownFile(path, Writable.stringUtf8(content))
    }

    @Test
    fun `should convert a bbmodel into a model and its textures`() {
        val pack = packWith("assets/mineinabyss/models/dishes/cut_meat.bbmodel", bbmodel())

        BBModelGenerator(pack, format).generate(modelKey) shouldBe modelKey

        val model = pack.model(modelKey).shouldNotBeNull()
        model.textures().variables().keys shouldContainExactly setOf("0")
        model.elements().size shouldBe 1 // the locator is dropped

        // Textures land under the model's own path
        pack.texture(Key.key("mineinabyss:dishes/cut_meat/cut_meat.png")).shouldNotBeNull()
        model.textures().variables()["0"]?.key() shouldBe Key.key("mineinabyss:dishes/cut_meat/cut_meat")
        // The raw source file is not something the client can read
        pack.unknownFiles().keys.shouldContainExactly(emptySet())
    }

    @Test
    fun `should normalize uv against the bbmodel's own resolution`() {
        val pack = packWith("assets/mineinabyss/models/dishes/cut_meat.bbmodel", bbmodel())
        BBModelGenerator(pack, format).generate(modelKey)

        // 16 across a 32 wide texture is half way, written as 8 of 16 in the model json, which creative holds as 0.5
        val uv = pack.model(modelKey).shouldNotBeNull().elements().single().faces().values.single().uv()
        uv.shouldNotBeNull().to().x() shouldBe 0.5f
    }

    @Test
    fun `should convert a bbmodel found outside the models folder`() {
        val pack = packWith("assets/mineinabyss/bbmodels/dishes/cut_meat.bbmodel", bbmodel())

        BBModelGenerator(pack, format).generate(modelKey) shouldBe modelKey
        pack.model(modelKey).shouldNotBeNull()
    }

    @Test
    fun `should convert each bbmodel once`() {
        val pack = packWith("assets/mineinabyss/models/dishes/cut_meat.bbmodel", bbmodel())
        val generator = BBModelGenerator(pack, format)

        generator.generate(modelKey)
        // The source file is gone after the first pass, a second lookup would fail without the cache
        generator.generate(modelKey) shouldBe modelKey
    }
}
