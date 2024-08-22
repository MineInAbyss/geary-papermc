package com.mineinabyss.geary.papermc.spawning.config

import com.charleskorn.kaml.*
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.spawning.spawn_types.SpawnType
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer.Companion.provideConfig
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import org.bukkit.plugin.Plugin
import kotlin.io.path.*

class SpawnEntryReader(
    val plugin: Plugin,
    val yamlFormat: Yaml,
) {
    val serializer = SpawnEntry.serializer()

    data class SpawnEntryWithNode(
        val entry: SpawnEntry,
        val node: YamlNode,
    )

    @OptIn(ExperimentalPathApi::class)
    fun readSpawnEntries(): List<SpawnEntry> {
        val rootDir = (plugin.dataPath / "spawns").createParentDirectories()
        val entries = mutableMapOf<String, SpawnEntryWithNode>()
        val spawns = mutableListOf<SpawnEntry>()
        rootDir.walk()
            .filter { it.isRegularFile() && it.extension == "yml" }
            .forEach { path ->
                val spawnYaml = yamlFormat.parseToYamlNode(path.inputStream()).yamlMap
                val namespaces = spawnYaml.get<YamlList>("namespaces")?.items?.map { it.yamlScalar.content } ?: listOf()
                val module = yamlFormat.serializersModule.overwriteWith(SerializersModule {
                    provideConfig(PolymorphicListAsMapSerializer.Config<Any>(namespaces = namespaces))
                })
                val yamlWithNamespaces = Yaml(module, yamlFormat.configuration)
                spawnYaml.entries.forEach { (name, spawn) ->
                    val nameStr = name.content
                    if (nameStr == "namespaces") return@forEach
                    runCatching {
                        val decoded = decodeSpawnEntry(yamlWithNamespaces, entries, spawn)
                        spawns += decoded.entry
                        entries[nameStr] = decoded
                    }
                        .onSuccess {
                            geary.logger.d { "Read spawn $nameStr entry from $path" }
                        }
                        .onFailure {
                            geary.logger.w { "Failed to read spawn $nameStr entry from $path" }
                            geary.logger.w { it.localizedMessage }
                            geary.logger.d { it.stackTraceToString() }
                        }
                }

            }
        return spawns.filter { it.type != SpawnType.None }
    }

    fun decodeSpawnEntry(
        yaml: Yaml,
        spawnEntries: Map<String, SpawnEntryWithNode>,
        yamlNode: YamlNode,
    ): SpawnEntryWithNode {
        val inherit = yamlNode.yamlMap.get<YamlNode>("inherit")
        val merged = if (inherit != null) {
            val inheritList = if (inherit is YamlList) inherit.items else listOf(inherit.yamlScalar)
            val inheritNodes = inheritList.mapNotNull { spawnEntries[it.yamlScalar.content]?.node }
            (inheritNodes + yamlNode).reduce(::mergeYamlNodes)
        } else yamlNode
        return SpawnEntryWithNode(
            yaml.decodeFromYamlNode(serializer, merged),
            merged,
        )
    }

    companion object {
        val specialMergeTags = Regex("(\\\$inherit)|(\\\$remove)")

        fun mergeYamlNodes(original: YamlNode, override: YamlNode): YamlNode = when {
            original is YamlMap && override is YamlMap -> {
                val mapEntries =
                    original.entries.entries.associate { it.key.content to (it.key to it.value) }.toMutableMap()
                override.entries.forEach { (key, node) ->
                    if (key.content in mapEntries) {
                        mapEntries[key.content] = key to mergeYamlNodes(mapEntries[key.content]!!.second, node)
                    } else mapEntries[key.content] = key to node
                }
                YamlMap(
                    mapEntries.values.toMap(),
                    original.path
                )
            }

            original is YamlList && override is YamlList -> {
                val inheritKey = override.items.firstOrNull { (it as? YamlScalar)?.content == "\$inherit" }
                val removeTags = override.items.mapNotNull {
                    (it as? YamlScalar)?.content?.takeIf { it.startsWith("\$remove") }?.removePrefix("\$remove")?.trim()
                }.toSet()

                if (inheritKey != null)
                    YamlList(original.items
                        .filter { (it as? YamlMap)?.entries?.any { it.key.content in removeTags } != true }
                        .plus(override.items.filter {
                            (it as? YamlScalar)?.content?.contains(specialMergeTags) != true
                        }), override.path
                    )
                else override
            }

            else -> override
        }

    }
}
