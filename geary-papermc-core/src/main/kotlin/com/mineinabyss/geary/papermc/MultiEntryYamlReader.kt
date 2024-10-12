package com.mineinabyss.geary.papermc

import com.charleskorn.kaml.*
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer
import com.mineinabyss.geary.serialization.serializers.PolymorphicListAsMapSerializer.Companion.provideConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.overwriteWith
import java.nio.file.Path
import kotlin.io.path.*

class MultiEntryYamlReader<T>(
    val serializer: KSerializer<T>,
    val yamlFormat: Yaml,
) {
    @OptIn(ExperimentalPathApi::class)
    fun decodeRecursive(rootDir: Path): List<T> {
        val nodes = mutableMapOf<String, EntryWithNode<T>>()
        val entries = mutableListOf<T>()
        rootDir.walk()
            .filter { it.isRegularFile() && it.extension == "yml" }
            .forEach { path ->
                val spawnYaml = yamlFormat.parseToYamlNode(path.inputStream()).yamlMap
                val namespaces = spawnYaml.get<YamlList>("namespaces")?.items?.map { it.yamlScalar.content } ?: listOf()
                val module = yamlFormat.serializersModule.overwriteWith(SerializersModule {
                    provideConfig(PolymorphicListAsMapSerializer.Config<Any>(namespaces = namespaces))
                })
                val yamlWithNamespaces = Yaml(module, yamlFormat.configuration)
                spawnYaml.entries.forEach { (name, entry) ->
                    val nameStr = name.content
                    if (nameStr == "namespaces") return@forEach
                    runCatching {
                        val decoded = decodeEntry(yamlWithNamespaces, nodes, entry)
                        entries += decoded.entry
                        nodes[nameStr] = decoded
                    }
                        .onSuccess {
                            geary.logger.d { "Read entry $nameStr entry from $path" }
                        }
                        .onFailure {
                            geary.logger.w { "Failed to read entry $nameStr entry from $path" }
                            geary.logger.w { it.localizedMessage }
                            geary.logger.d { it.stackTraceToString() }
                        }
                }

            }
        return entries
    }

    data class EntryWithNode<T>(
        val entry: T,
        val node: YamlNode,
    )

    fun decodeEntry(
        yaml: Yaml,
        decodedEntries: Map<String, EntryWithNode<T>>,
        yamlNode: YamlNode,
    ): EntryWithNode<T> {
        val inherit = yamlNode.yamlMap.get<YamlNode>("inherit")
        val merged = if (inherit != null) {
            val inheritList = if (inherit is YamlList) inherit.items else listOf(inherit.yamlScalar)
            val inheritNodes = inheritList.mapNotNull { decodedEntries[it.yamlScalar.content]?.node }
            (inheritNodes + yamlNode).reduce(Companion::mergeYamlNodes)
        } else yamlNode
        return EntryWithNode(yaml.decodeFromYamlNode(serializer, merged), merged)
    }

    companion object {
        private val specialMergeTags = Regex("(\\\$inherit)|(\\\$remove)")

        fun mergeYamlNodes(original: YamlNode?, override: YamlNode): YamlNode = when {
            original is YamlMap && override is YamlMap -> {
                val mapEntries =
                    original.entries.entries.associate { it.key.content to (it.key to it.value) }.toMutableMap()
                override.entries.forEach { (key, node) ->
//                    if (key.content in mapEntries) {
                        mapEntries[key.content] = key to mergeYamlNodes(mapEntries[key.content]?.second, node)
//                    } else mapEntries[key.content] = key to node
                }
                YamlMap(
                    mapEntries.values.toMap(),
                    original.path
                )
            }

            original is YamlList && override is YamlList -> {
                val inheritKey = override.items.firstOrNull { (it as? YamlScalar)?.content == "\$inherit" }
                val removeTags = override.items.flatMap {
                    (it as? YamlScalar)?.content?.takeIf { it.startsWith("\$remove") }?.removePrefix("\$remove")
                        ?.trim()
                        ?.split(' ')
                        ?: emptyList()
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

            // If original is not a list, but we ask to inherit, remove these tags from the override
            override is YamlList -> {
                println("Overriding! $override")
                YamlList(override.items.filter { (it as? YamlScalar)?.content?.contains(specialMergeTags) != true }, override.path)
            }

            else -> override
        }

    }
}
