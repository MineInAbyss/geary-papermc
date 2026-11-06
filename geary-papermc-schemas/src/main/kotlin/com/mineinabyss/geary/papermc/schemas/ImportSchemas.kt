package com.mineinabyss.geary.papermc.schemas

import com.mineinabyss.geary.papermc.schemas.GearyPrefabSchemas.mergeGearyPrefabs
import com.mineinabyss.jsonschema.writer.SchemaWriter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Imports `schemas.zip` bundles attached to plugin releases into a config repo, then rebuilds the merged prefab schema and editor mappings.
 * Arguments are the config repo root followed by the bundles to import
 */
fun main(args: Array<String>) {
    val root = Path(args.firstOrNull() ?: error("Pass the config repo to import schemas into as the first argument"))
    SchemaWriter(root).apply {
        args.drop(1).forEach { importBundle(Path(it)) }
        mergeGearyPrefabs()
        flush()
    }
}

@OptIn(ExperimentalSerializationApi::class)
private val settingsJson = Json {
    allowComments = true
    allowTrailingComma = true
}

@OptIn(ExperimentalPathApi::class)
fun SchemaWriter.importBundle(bundle: Path) = FileSystems.newFileSystem(bundle).use { zip ->
    val bundleRoot = zip.getPath("/")
    val bundleSchemas = bundleRoot / ".idea" / "schemas"
    // Each bundle carries a merged prefab schema built from its own fragment only, the real one is rebuilt from every fragment
    val partialPrefabs = bundleSchemas / "${GearyPrefabSchemas.SCHEMA_ID}.json"
    val schemaFiles = if (bundleSchemas.exists()) bundleSchemas.walk().filter { it != partialPrefabs }.toList() else emptyList()

    // Directories are replaced whole so schemas a plugin stopped writing are removed along with their mappings
    schemaFiles.map { bundleSchemas.relativize(it).first().toString() }.distinct()
        .filter { bundleSchemas.resolve(it).isDirectory() }
        .forEach { (schemasDir / it).deleteRecursively() }
    schemaFiles.forEach { it.copyInto(bundleSchemas, schemasDir) }

    val bundleFragments = bundleRoot / ".idea" / "schema-fragments"
    if (bundleFragments.exists()) bundleFragments.walk().forEach { it.copyInto(bundleFragments, root / ".idea" / "schema-fragments") }

    val settings = bundleRoot / ".vscode" / "settings.json"
    if (settings.notExists()) return@use
    val schemasPrefix = root.relativize(schemasDir).joinToString("/") + "/"
    settingsJson.parseToJsonElement(settings.readText()).jsonObject["yaml.schemas"]?.jsonObject?.forEach { (path, globs) ->
        if (!path.startsWith(schemasPrefix) || path == "$schemasPrefix${GearyPrefabSchemas.SCHEMA_ID}.json") return@forEach
        val fileMatch = when (globs) {
            is JsonArray -> globs.map { it.jsonPrimitive.content }
            is JsonPrimitive -> listOf(globs.content)
            else -> return@forEach
        }
        map(path.removePrefix(schemasPrefix).removeSuffix(".json"), fileMatch)
    }
}

private fun Path.copyInto(from: Path, to: Path) {
    val target = to.resolve(from.relativize(this).toString())
    target.createParentDirectories()
    copyTo(target, overwrite = true)
}
