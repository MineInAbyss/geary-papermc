import com.mineinabyss.geary.actions.Action
import com.mineinabyss.geary.actions.Condition
import com.mineinabyss.geary.actions.event_binds.EntityObservers
import com.mineinabyss.geary.actions.expressions.Expression.Serializer.ExpressionDescriptor
import com.mineinabyss.geary.autoscan.autoscan
import com.mineinabyss.geary.modules.ArchetypeEngineModule
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.features.common.event_bridge.entities.EventBridge
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.serialization.serialization
import com.mineinabyss.idofront.jsonschema.dsl.SchemaProperty
import com.mineinabyss.idofront.jsonschema.dsl.SchemaType.*
import com.mineinabyss.idofront.jsonschema.dsl.jsonSchema
import com.mineinabyss.idofront.jsonschema.generator.kotlinx_serialization.KotlinxSerializationJsonSchemaGenerator
import com.mineinabyss.idofront.serialization.BaseSerializableItemStack
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.getPolymorphicDescriptors
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.reflect.KClass

fun main() {
    val json = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    jsonSchema {
        val generator = KotlinxSerializationJsonSchemaGenerator(
            replaceDescriptors = {
                when (it) {
                    is ExpressionDescriptor -> it.innerDescriptor
                    else -> it
                }
            }
        )
//        definition() {
//            generator.applyClassDescriptor(DurationSerializer.descriptor)
//        }
//        com.mineinabyss.geary.actions.expressions.Expression.serializer(String.serializer()).descriptor
        val desc = BaseSerializableItemStack.serializer().descriptor
        definition("kotlinx.serialization.ContextualSerializer<BaseSerializableItemStack>", desc) {
            anyOf(
                { type = STRING },
                { generator.applyClassDescriptor(desc) }
            )
        }


//        var componentDescriptors: List<SerialDescriptor> = listOf()
        var module = SerializersModule { }
        geary(ArchetypeEngineModule()) {
//        /var/home/offz/projects/geary-papermc/geary-papermc-features/src/com.mineinabyss.idofront.jsonschema.generator.kotlinx_serialization.main/kotlin/com/mineinabyss/geary/papermc/features/common/actions
            autoscan(Geary::class.java.classLoader, "com.mineinabyss.geary.papermc", "com.mineinabyss.geary.actions") {
                components()
                subClassesOf<Action>()
                subClassesOf<Condition>()
                subClassesOf<EventBridge>()
                println("Autoscan ran")
            }
            serialization {
                module = SerializersModule { serializers.modules.forEach { include(it) } }
            }
        }

        fun getPolymorphicDescriptors(kClass: KClass<*>): List<SerialDescriptor> {
            return module
                .getPolymorphicDescriptors(PolymorphicSerializer(kClass).descriptor)
                .toList()
                .filter { PrefabKey.ofOrNull(it.serialName) != null }
        }

        val allDescriptors = getPolymorphicDescriptors(Any::class).toSet()
        val actions = getPolymorphicDescriptors(Action::class).toSet() - getPolymorphicDescriptors(Condition::class).toSet()
        val conditions = getPolymorphicDescriptors(Condition::class).toSet() - actions
        val eventBridges = getPolymorphicDescriptors(EventBridge::class).toSet()
        val componentDescriptors = allDescriptors - actions - conditions - eventBridges


        val regex = "_([a-z])".toRegex()
        fun String.toFormattedKey(): String {
            val key = PrefabKey.of(this)
            fun String.snakeCaseToCamelCase() = replace(regex) { it.groupValues[1].uppercase() }
            val name = (if (key.namespace == "geary") key.key else key.toString()).snakeCaseToCamelCase()
            return name
        }

        definition("geary:observe", EntityObservers.serializer().descriptor) {
            type = OBJECT
            additionalProperties {
                ref = $$"#/$defs/geary:run"
            }
            propertyNames {
                type = STRING
                enum += eventBridges.map { it.serialName.toFormattedKey() }
            }
        }

        definition("geary:run", null) {
            type = ARRAY
            items {
                ref = $$"#/$defs/geary:action"
            }
        }

        fun SchemaProperty.addPropertiesFromDescriptors(descriptors: Collection<SerialDescriptor>) {
            descriptors.forEach {
                val name = it.serialName.toFormattedKey()
                property(name) {
                    ref = $$"#/$defs/$${it.serialName}"
                }
            }
        }
        definition("geary:ensure", null, override = true) {
            addPropertiesFromDescriptors(conditions)
        }
        definition("geary:action", null, override = true) {
            addPropertiesFromDescriptors(actions)
        }

        definition("kotlinx.serialization.ContextualSerializer<Any>", null) {
            addPropertiesFromDescriptors(componentDescriptors)
        }

        definition("kotlinx.serialization.ContextualSerializer<ULong>", null) {
            type = STRING
            enum += allDescriptors.map { it.serialName.toFormattedKey() }
        }

        allDescriptors.forEach {
            definition(it.serialName, it) {
                generator.applyClassDescriptor(it)
            }
        }

        rootProperty {
            ref = $$"#/$defs/kotlinx.serialization.ContextualSerializer<Any>"
        }

    }.let {
        Path("/var/home/offz/projects/geary-papermc/build/schemas/test.json").writeText(json.encodeToString(it))
    }
}