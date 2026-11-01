import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.schemas.GearyPrefabSchemas.gearyPrefabs
import com.mineinabyss.geary.papermc.spawning.config.SpawnConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnEntry
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsConfig
import com.mineinabyss.geary.papermc.spawning.config.SpreadEntityTypesConfig
import com.mineinabyss.jsonschema.writer.writeSchemas

fun main(args: Array<String>) = writeSchemas(args) {
    schema("geary/config", listOf("**/plugins/Geary/config.yml"), GearyPaperConfig.serializer())
    schema("geary/spawning", listOf("**/plugins/Geary/spawning.yml"), SpawnConfig.serializer())
    schema("geary/spread-config", listOf("**/plugins/Geary/spread_config.yml"), SpreadEntityTypesConfig.serializer())
    multiEntrySchema("geary/spawns", listOf("**/plugins/Geary/spawns/**/*.yml"), SpawnEntry.serializer())
    multiEntrySchema("geary/locations", listOf("**/plugins/Geary/locations/**/*.yml"), SpawnLocationsConfig.serializer())
    gearyPrefabs("geary-papermc", listOf("com.mineinabyss.geary"))
}
