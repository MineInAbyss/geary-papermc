import com.charleskorn.kaml.YamlComment
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import dev.adamko.kxstsgen.KxsTsConfig
import dev.adamko.kxstsgen.KxsTsGenerator
import dev.adamko.kxstsgen.core.TsDeclaration
import dev.adamko.kxstsgen.core.TsSourceCodeGenerator
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlin.io.path.*

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val outputDir = Path("build/schemas").createDirectories()
    fun writeFile(name: String, text: String) {
        (outputDir / "$name.ts").also { if (it.notExists()) it.createFile() }.writeText(text)
    }

    val tsGenerator = KxsTsGenerator()
    val config = tsGenerator.generate(SpawnConfig.serializer())
    val spawnsFile = tsGenerator.generate(SpawnEntry.serializer())

    writeFile("config", config)
    writeFile("spawns", spawnsFile)
}
