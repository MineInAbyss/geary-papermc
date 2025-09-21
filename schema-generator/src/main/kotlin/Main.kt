import com.charleskorn.kaml.YamlComment
import com.mineinabyss.geary.actions.actions.EnsureAction
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpreadSpawnConfig
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import io.github.smiley4.schemakenerator.core.CoreSteps.initial
import io.github.smiley4.schemakenerator.core.data.AnnotationData
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaAnnotationUtils.iterateProperties
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.compileReferencing
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.generateJsonSchema
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.merge
import io.github.smiley4.schemakenerator.jsonschema.JsonSchemaSteps.withTitle
import io.github.smiley4.schemakenerator.jsonschema.data.TitleType
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonObject
import io.github.smiley4.schemakenerator.jsonschema.jsonDsl.JsonTextValue
import io.github.smiley4.schemakenerator.serialization.SerializationSteps.analyzeTypeUsingKotlinxSerialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlin.io.path.*
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Serializable
data class TestConfig(
    @YamlComment("Default per player spawn cap limit when not explicitly set in playerCaps")
    val first: String,
)

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    generateSimpleConfigSchema(typeOf<GearyPaperConfig>(), "config")
    generateSimpleConfigSchema(typeOf<SpawnConfig>(), "spawns")
    generateSimpleConfigSchema(typeOf<EnsureAction>(), "spread_config")
//    writeFile("config", config)
//    writeFile("spawns", spawnsFile)
}

fun generateSimpleConfigSchema(type: KType, name: String) {
    val outputDir = Path("build/schemas").createDirectories()

    fun determineDescription(annotations: Collection<AnnotationData>): String? {
        return annotations
            .filter { it.name == YamlComment::class.qualifiedName }
            .map { (it.values["lines"] as Array<String>).joinToString("") }
            .firstOrNull()
    }
    fun writeFile(name: String, text: String) {
        (outputDir / "$name.json").also { if (it.notExists()) it.createFile() }.writeText(text)
    }

    return initial(type)
        .analyzeTypeUsingKotlinxSerialization {
//            redirect {
//                from<SpawnEntry>()
//                to<Map<String, String>>()
//            }
            redirect {
                from<IntRange>()
                to<Int>()
            }
//            redirect {
//                from<SpawnType>()
//                to<String>()
//            }
            redirect {
                from<EnsureAction>()
                to<String>()
            }
        }
        .generateJsonSchema {
            this.optionals = JsonSchemaSteps.RequiredHandling.NON_REQUIRED
        }
        .apply {
            this.entries.forEach { schema ->
                val json = schema.json
                if (json is JsonObject && json.properties["description"] == null) {
                    determineDescription(schema.typeData.annotations)?.also { description ->
                        json.properties["description"] = JsonTextValue(description)
                    }
                    iterateProperties(schema, typeDataById) { property, data, type ->
                        determineDescription(data.annotations + type.annotations)?.also { description ->
                            property.properties["description"] = JsonTextValue(description)
                        }
                    }
                }
            }
        }
        .withTitle(type = TitleType.SIMPLE)
        .compileReferencing()
        .merge()
        .json
        .prettyPrint()
        .let { writeFile(name, it) }
}