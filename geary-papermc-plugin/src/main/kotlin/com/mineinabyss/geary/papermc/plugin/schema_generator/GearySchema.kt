package com.mineinabyss.geary.papermc.plugin.schema_generator

import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.geary.serialization.SerializersByMap
import com.mineinabyss.geary.serialization.serializers.InnerSerializer
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import dev.adamko.kxstsgen.KxsTsConfig
import dev.adamko.kxstsgen.core.TsDeclaration
import dev.adamko.kxstsgen.core.TsElementId
import kotlinx.serialization.InternalSerializationApi
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
    val serialization: SerializableComponents,
) {

    @OptIn(InternalSerializationApi::class)
    fun generate() {
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

        // Avoid using the one in tsGenerator so it doesn't cache results
//        val idConv = TsElementIdConverter.Default
//        val mapConv =  TsMapTypeConverter.Default
//        val tempConverter = TsElementConverter.Default(
//            idConv, mapConv,
//            TsTypeRefConverter.Default(idConv, mapConv),
//        )

//        prefabSerializers.filter { it.descriptor.serialName.contains(".") }.forEach { serializer ->
//            val element = tempConverter(serializer.descriptor).first() as? TsDeclaration.TsInterface ?: return@forEach
//            tsGenerator.descriptorOverrides[serializer.descriptor] = element.copy(
//                id = TsElementId(
//                    serializer.descriptor.serialName.replace(".", "$")
//                )
//            )
//        }

        // Handle Inner Serializers correctly (as though they are value classes)
        serializers.filterIsInstance<InnerSerializer<*, *>>().forEach { serializer ->
            tsGenerator.descriptorOverrides += serializer.descriptor to TsDeclaration.TsTypeAlias(
                id = TsElementId(serializer.serialName),
                typeRef = tsGenerator.typeRefConverter(serializer.inner.descriptor)
            )
        }

        output.writeText(
            buildString {
                appendLine(
                    ts(
                        """
                    export type EntityExpression = string;
                    export type geary${'$'}entity = string;
                    export type geary${'$'}uuid = string;
                    export type geary${'$'}prefab_key = string;
                """.trimIndent()
                    )
                )
                appendLine("class Base {")
                prefabSerializers.map { it.descriptor.serialName }.forEach {
                    val camelCase = it.fromSnakeCaseToCamelCase()
                    appendLine("""    "$camelCase"?: ${it.replace(":", "$").replace(".", "$")};""")
                }
                appendLine("}")
                appendLine(namespaces.fold(tsGenerator.generate(*serializers.toTypedArray())) { acc, namespace ->
                    acc.replace("$namespace:", "$namespace$")
                })
            }
                // Map<String, Any> is usually the type we use to describe passing components into components
                .replace("{ [key: string]: Any }", "Base")
                .replace("\"geary:", "\"")
        )
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

inline fun ts(@Language("TypeScript") code: String) = code
