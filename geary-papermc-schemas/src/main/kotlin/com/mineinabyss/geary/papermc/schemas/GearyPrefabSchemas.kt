package com.mineinabyss.geary.papermc.schemas

import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.autoscan.ExcludeAutoScan
import com.mineinabyss.jsonschema.dsl.jsonSchema
import com.mineinabyss.jsonschema.generator.KotlinxSerializationJsonSchemaGenerator
import com.mineinabyss.jsonschema.writer.SchemaWriter
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.json.*
import kotlinx.serialization.serializerOrNull
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import kotlin.io.path.*

/**
 * Generates a Geary prefab schema split into one fragment per plugin.
 *
 * Each plugin writes the components found in its own packages to `.idea/schema-fragments/geary/<id>.json`,
 * then every fragment present is merged into `geary/prefabs.json`, so the result covers all plugins that have generated schemas
 */
object GearyPrefabSchemas {
    const val SCHEMA_ID = "geary/prefabs"
    val PREFAB_FILES = listOf("**/Geary/prefabs/**/*.yml")

    private const val COMPONENTS = "geary:components"
    private const val ACTION = "geary:action"
    private const val ENSURE = "geary:ensure"
    private const val RUN = "geary:run"
    private const val OBSERVE = "geary:observe"
    private const val COMPONENT_ID = "kotlinx.serialization.ContextualSerializer<ULong>"
    private const val YAML_NODE = "com.charleskorn.kaml.YamlNode"

    private val namespacedKey = Regex("^[a-z0-9_.-]+:[a-z0-9_./-]+$")
    private val snakeCase = Regex("_([a-z0-9])")

    private enum class Category { COMPONENTS, ACTIONS, CONDITIONS, EVENTS }

    /** Writes the fragment for components in [packages], then rebuilds the merged prefab schema */
    fun SchemaWriter.gearyPrefabs(
        id: String,
        packages: List<String>,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader,
        extraComponents: List<KSerializer<*>> = emptyList(),
    ) {
        val scanned = scanComponents(classLoader, packages)
            .plus(extraComponents.map { it to Category.COMPONENTS })
            .filter { (serializer, _) -> namespacedKey.matches(serializer.descriptor.serialName) }
            .distinctBy { (serializer, _) -> serializer.descriptor.serialName }
            .sortedBy { (serializer, _) -> serializer.descriptor.serialName }

        val fragmentsDir = root / ".idea" / "schema-fragments" / "geary"
        (fragmentsDir / "$id.json").createParentDirectories()
            .writeText(SchemaWriter.prettyJson.encodeToString(fragment(scanned)) + "\n")
        writeJson("$SCHEMA_ID.json", merge(fragmentsDir.listDirectoryEntries("*.json").sorted().map {
            Json.parseToJsonElement(it.readText()).jsonObject
        }))
        map(SCHEMA_ID, PREFAB_FILES)
    }

    @OptIn(InternalSerializationApi::class)
    private fun scanComponents(classLoader: ClassLoader, packages: List<String>): List<Pair<KSerializer<*>, Category>> {
        val reflections = Reflections(
            ConfigurationBuilder()
                .apply { packages.forEach { forPackage(it, classLoader) } }
                .filterInputsBy(FilterBuilder().apply { packages.forEach { includePackage(it) } })
                .setScanners(Scanners.TypesAnnotated)
        )
        return reflections
            .get(Scanners.TypesAnnotated.with(Serializable::class.java).asClass<Any>(classLoader))
            .filter { !it.isAnnotationPresent(ExcludeAutoScan::class.java) }
            .mapNotNull { clazz ->
                val serializer = runCatching { clazz.kotlin.serializerOrNull() }
                    .onFailure { println("Warn: Could not get serializer for ${clazz.name}: ${it.message}") }
                    .getOrNull() ?: return@mapNotNull null
                serializer to when {
                    Condition::class.java.isAssignableFrom(clazz) -> Category.CONDITIONS
                    Action::class.java.isAssignableFrom(clazz) -> Category.ACTIONS
                    // Events are plain marker classes, named On<Event> by convention
                    clazz.simpleName.matches(Regex("^On[A-Z].*")) && serializer.descriptor.elementsCount == 0 -> Category.EVENTS
                    else -> Category.COMPONENTS
                }
            }
    }

    @OptIn(InternalSerializationApi::class)
    private fun SchemaWriter.fragment(components: List<Pair<KSerializer<*>, Category>>): JsonObject {
        // The merged schema defines these, fragments only reference them
        val placeholders = setOf(COMPONENTS, ACTION, ENSURE, RUN, OBSERVE, COMPONENT_ID)
        val generator = KotlinxSerializationJsonSchemaGenerator(
            replaceDescriptors = { descriptor ->
                when {
                    descriptor.serialName in placeholders -> placeholder(descriptor.serialName)
                    descriptor.isComponentMap() -> placeholder(COMPONENTS)
                    else -> descriptor
                }
            },
            schemaDescriptors = generator.schemaDescriptors,
        )
        return jsonSchema {
            generator.provideRequestedDefinitions()
            rootProperty {
                Category.entries.forEach { category ->
                    property(category.name.lowercase()) {
                        components.filter { it.second == category }.forEach { (serializer, _) ->
                            val serialName = serializer.descriptor.serialName
                            property(serialName.toPrefabKey()) {
                                runCatching { with(generator) { applyClassDescriptor(serializer.descriptor) } }.onFailure {
                                    println("Warn: Could not generate schema for $serialName: ${it.rootCause()}")
                                    ref = null
                                    type = null
                                    description = "No schema available for $serialName"
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Throwable.rootCause(): String = generateSequence(this) { it.cause }.last().let { "${it::class.simpleName}: ${it.message}" }

    // SerializedComponents decode a raw YamlMap of component names to values
    private fun SerialDescriptor.isComponentMap() =
        kind == StructureKind.MAP && elementsCount == 2 && getElementDescriptor(1).serialName == YAML_NODE

    @OptIn(InternalSerializationApi::class)
    private fun placeholder(name: String) = buildSerialDescriptor(name, SerialKind.CONTEXTUAL)

    private fun merge(fragments: List<JsonObject>): JsonObject {
        fun category(name: String) = fragments
            .flatMap { it.properties(name).entries }
            .associate { it.key to it.value }
            .toSortedMap()

        val components = category("components")
        val actions = category("actions")
        val conditions = category("conditions")
        val events = category("events")
        val everyComponent = (components + actions + conditions + events).toSortedMap()

        val fragmentDefs = buildMap {
            fragments.forEach { fragment ->
                fragment["\$defs"]?.jsonObject?.forEach { (name, def) ->
                    // Fragments fill references they cannot resolve with a description-only placeholder, real definitions win
                    val isPlaceholder = def.jsonObject.keys == setOf("description")
                    if (name !in this || !isPlaceholder) put(name, def)
                }
            }
        }

        fun ref(name: String) = buildJsonObject { put("\$ref", "#/\$defs/$name") }
        fun JsonObjectBuilder.properties(vararg sources: Map<String, JsonElement>) = putJsonObject("properties") {
            sources.forEach { source -> source.forEach { (key, schema) -> put(key, schema) } }
        }

        val defs = fragmentDefs + mapOf(
            COMPONENTS to buildJsonObject {
                put("type", "object")
                put("description", "Components on this prefab, by component name")
                properties(everyComponent)
            },
            ACTION to buildJsonObject {
                put("type", "object")
                put("description", "An action, condition or component, along with options for how it runs")
                properties(actions, conditions, components, events, taskOptions(::ref))
            },
            ENSURE to buildJsonObject {
                put("type", "object")
                put("description", "Conditions that must pass, anything else is emitted as an event")
                properties(conditions, components)
            },
            RUN to buildJsonObject {
                put("type", "array")
                put("items", ref(ACTION))
            },
            OBSERVE to buildJsonObject {
                put("type", "object")
                put("description", "Actions to run when an event happens on this entity, by event name")
                properties(events.mapValues { ref(RUN) })
                put("additionalProperties", ref(RUN))
            },
            COMPONENT_ID to buildJsonObject {
                // Offers known component names while still accepting ones from plugins without a schema
                putJsonArray("anyOf") {
                    addJsonObject {
                        put("type", "string")
                        putJsonArray("enum") { everyComponent.keys.forEach { add(it) } }
                    }
                    addJsonObject { put("type", "string") }
                }
            },
        )

        return buildJsonObject {
            put("\$schema", "https://json-schema.org/draft/2020-12/schema")
            put("title", "Geary prefab")
            put("\$ref", "#/\$defs/$COMPONENTS")
            put("\$defs", JsonObject(defs.toSortedMap()))
        }
    }

    // Mirrors the options on geary's Task, which sit next to the action key
    private fun taskOptions(ref: (String) -> JsonObject): Map<String, JsonObject> = mapOf(
        "when" to buildJsonObject {
            put("description", "Conditions that must all pass for this action to run")
            put("type", "array")
            put("items", ref(ENSURE))
        },
        "register" to buildJsonObject {
            put("description", "Variable name to store the result of this action in")
            put("type", "string")
        },
        "onFail" to JsonObject(ref(RUN) + ("description" to JsonPrimitive("Actions to run if this action or its conditions fail"))),
        "with" to buildJsonObject {
            put("description", "Variables to set before running this action")
            put("type", "object")
        },
        "loop" to buildJsonObject {
            put("description", "Expression for a list, runs this action once per entry with the entry as 'it'")
        },
        "repeat" to buildJsonObject { put("type", "integer") },
        "repeatInterval" to buildJsonObject {
            put("description", "Delay between repeats")
            put("type", "string")
        },
    )

    private fun JsonObject.properties(category: String): JsonObject =
        this["properties"]?.jsonObject?.get(category)?.jsonObject?.get("properties")?.jsonObject ?: JsonObject(emptyMap())

    /** Keys as written in prefab files, the geary namespace is implied */
    fun String.toPrefabKey(): String {
        val namespace = substringBefore(":")
        val key = substringAfter(":").replace(snakeCase) { it.groupValues[1].uppercase() }
        return if (namespace == "geary") key else "$namespace:$key"
    }
}
