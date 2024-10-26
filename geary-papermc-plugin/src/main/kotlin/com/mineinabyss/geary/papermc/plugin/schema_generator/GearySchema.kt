package com.mineinabyss.geary.papermc.plugin.schema_generator

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.SerializableComponentsModule
import com.mineinabyss.geary.serialization.SerializersByMap
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import dev.adamko.kxstsgen.KxsTsConfig
import dev.adamko.kxstsgen.core.TsDeclaration
import dev.adamko.kxstsgen.core.TsElementId
import kotlinx.serialization.KSerializer
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.writeText

inline fun String.isGearyPrefab(): Boolean {
    val namespaces =
        listOf("geary", "minecraft", "mineinabyss", "blocky", "bonfire", "climb", "chatty", "packy", "cosmetics")
    return namespaces.any { this.startsWith("$it:") }
}

class GearySchema(
    val output: Path,
    val serialization: SerializableComponentsModule,
) {
    fun generate() {
        // TODO automatically read these off geary
        val namespaces =
            listOf("geary", "minecraft", "mineinabyss", "blocky", "bonfire", "climb", "chatty", "packy", "cosmetics")
        val tsGenerator = CustomKxsTsGenerator(KxsTsConfig())
        val serializers = buildSet<KSerializer<*>> {
            add(BaseSerializableItemStack.serializer())
            addAll((serialization.serializers as SerializersByMap)
                .serialName2Component.keys
                .filter { it.isGearyPrefab() }
//                .map { ComponentSerializers.run { it.fromCamelCaseToSnakeCase() } }
                .map { serialization.serializers.getSerializerFor(it, GearyComponent::class) }
                .filterIsInstance<KSerializer<*>>()
            )
        }

        val prefabSerializers = serializers.filter { it.descriptor.serialName.isGearyPrefab() }

        // Handle Inner Serializers correctly (as though they are value classes)
        serializers.filterIsInstance<InnerSerializer<*, *>>().forEach { serializer ->
            tsGenerator.descriptorOverrides += serializer.descriptor to TsDeclaration.TsTypeAlias(
                id = TsElementId(serializer.serialName),
                typeRef = tsGenerator.typeRefConverter(serializer.inner.descriptor)
            )
        }

        val generated = buildString {
            appendLine(
                """
                export type EntityExpression = string;
                export type geary${'$'}entity = string;
                export type geary${'$'}uuid = string;
                export type geary${'$'}prefab_key = string;
                """.trimIndent()
            )
            appendLine("class Base {")
            val serialNames = prefabSerializers.map { it.descriptor.serialName }
            serialNames.forEach {
                val camelCase = it.fromSnakeCaseToCamelCase()
                appendLine("""    "$camelCase"?: ${it.replace(":", "$").replace(".", "$")};""")
            }
            appendLine("}")

            appendLine("class BaseOrString {")
            serialNames.forEach {
                val camelCase = it.fromSnakeCaseToCamelCase()
                appendLine("""    "$camelCase"?: ${it.replace(":", "$").replace(".", "$")} | string;""")
            }
            appendLine("}")

            appendLine(namespaces.fold(tsGenerator.generate(*serializers.toTypedArray())) { acc, namespace ->
                acc.replace("$namespace:", "$namespace$")
            })

            appendLine(
                """
                class ActionBase extends Base {
                    register?: string;
                    loop?: string;
                }
                """.trimIndent()
            )
        }
        val cleanup = generated
            // Map<String, Any> is usually the type we use to describe passing components into components
            .replace("{ [key: string]: Any }", "Base")
            // Remove prefix for any geary strings, it's auto included
            .replace("\"geary:", "\"")
            // BaseSerializableItemStack is allowed to be a string for shorthands like `geary:some_item`
            .replaceFirst(
                "export interface BaseSerializableItemStack {",
                """
                    type BaseSerializableItemStack = SerializableItemStack | string;

                    export interface SerializableItemStack {
                """.trimIndent()
            )
            // Literally don't know why this one thing has a question mark at the end when it shouldn't :skull:
            .replace("geary\$sound?", "geary\$sound")
            // Remove double declaration since we manually added
            .replaceFirst("export type BaseSerializableItemStack = any;", "")
            // with allows inline expressions that are strings
            .replace("export type geary\$with = Base;", "export type geary\$with = BaseOrString;")
            // Actions manually add some extra options like register
            .replaceFirst(
                "geary\$observe = { [key: string]: Base[] }",
                "geary\$observe = { [key: string]: ActionBase[] }"
            )

        // Return the values of any enums that were formatted as namespace$key to avoid TS naming issues
        val replaceEnumRefs = namespaces.fold(cleanup) { acc, namespace ->
            acc.replace("\"$namespace\$", "\"$namespace:")
        }
        output.writeText(replaceEnumRefs)
    }
}

inline fun String.fromSnakeCaseToCamelCase(): String {
    val before = substringBefore(":")
    val after = substringAfter(":")
    val split = after.split("_").let { list ->
        list.first() + list.drop(1).joinToString("") { it.capitalize() }
    }
    return "$before:${split}"
}
