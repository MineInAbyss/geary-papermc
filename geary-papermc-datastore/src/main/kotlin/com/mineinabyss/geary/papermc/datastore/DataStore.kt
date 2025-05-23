package com.mineinabyss.geary.papermc.datastore

import com.mineinabyss.geary.components.relations.InstanceOf
import com.mineinabyss.geary.datatypes.GearyComponent
import com.mineinabyss.geary.datatypes.GearyEntityType
import com.mineinabyss.geary.datatypes.toRelation
import com.mineinabyss.geary.helpers.componentId
import com.mineinabyss.geary.modules.Geary
import com.mineinabyss.geary.modules.relationOf
import com.mineinabyss.geary.papermc.datastore.PaperDatastore.COMPONENTS_KEY
import com.mineinabyss.geary.papermc.datastore.PaperDatastore.PREFABS_KEY
import com.mineinabyss.geary.papermc.datastore.namespacedkey.*
import com.mineinabyss.geary.prefabs.PrefabKey
import com.mineinabyss.geary.prefabs.entityOfOrNull
import com.mineinabyss.geary.serialization.SerializableComponents
import io.papermc.paper.persistence.PersistentDataContainerView
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.SetSerializer
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.BYTE_ARRAY

@PublishedApi
internal val Geary.serializers get() = getAddon(SerializableComponents).serializers

@PublishedApi
internal val Geary.formats get() = getAddon(SerializableComponents).formats

/** Returns whether this [PersistentDataContainer] has a component [T] encoded in it. */
context(Geary)
inline fun <reified T : GearyComponent> PersistentDataContainer.has(): Boolean {
    return has(serializers.getNamespacedKeyFor<T>() ?: return false, BYTE_ARRAY)
}

context(Geary)
inline fun <reified T : GearyComponent> PersistentDataContainer.remove() {
    return remove(serializers.getNamespacedKeyFor<T>() ?: return)
}

/**
 * Encodes a component into this [PersistentDataContainer], where the serializer and key can automatically be found via
 * [Formats].
 */
context(Geary)
fun <T : GearyComponent> PersistentDataContainer.encode(
    value: T,
    serializer: SerializationStrategy<T> = ((serializers.getSerializerFor(value::class)
        ?: error("Serializer not registered for ${value::class.simpleName}")) as SerializationStrategy<T>),
    key: NamespacedKey = serializers.getSerialNameFor(value::class)?.toComponentKey()
        ?: error("SerialName  not registered for ${value::class.simpleName}"),
) {
    hasComponentsEncoded = true
    val encoded = formats.binaryFormat.encodeToByteArray(serializer, value)
    this[key, BYTE_ARRAY] = encoded
}

/**
 * Decodes a component of type [T] from this [PersistentDataContainer], where serializer and key are automatically
 * found via [Formats].
 */
context(Geary)
inline fun <reified T : GearyComponent> PersistentDataContainerView.decode(): T? {
    return decode(
        serializer = serializers.getSerializerFor(T::class) ?: return null,
        key = serializers.getSerialNameFor(T::class)?.toComponentKey() ?: return null
    )
}

/**
 * Decodes a component of type [T] from this [PersistentDataContainer] where the [serializer] may automatically be found
 * via [Formats] given a [key].
 */
context(Geary)
inline fun <reified T : GearyComponent> PersistentDataContainerView.decode(
    key: NamespacedKey,
    serializer: DeserializationStrategy<out T>? =
        serializers.getSerializerForNamespaced(key, T::class),
): T? {

    serializer ?: return null
    val encoded = get(key, BYTE_ARRAY) ?: return null
    return runCatching { formats.binaryFormat.decodeFromByteArray(serializer, encoded) }
        .onFailure { it.printStackTrace() }
        .getOrNull()
}

/**
 * Encodes a list of [components] to this [PersistentDataContainer].
 *
 * @see encode
 */
context(Geary)
fun PersistentDataContainer.encodeComponents(
    components: Collection<GearyComponent>,
    type: GearyEntityType,
) {
    hasComponentsEncoded = true
    //remove all keys present on the PDC so we only end up with the new list of components being encoded
    keys.filter { it.key.isComponentKey() && it != COMPONENTS_KEY }
        .forEach { remove(it) }

    for (value in components)
        encode(value)

    val prefabs = type.filter { it.toRelation()?.kind == componentId<InstanceOf>() }
    if (prefabs.size != 0)
        encodePrefabs(prefabs.map { it.toRelation()!!.target.toGeary().get<PrefabKey>() }.filterNotNull())
}

/**
 * Encodes a list of [PrefabKey]s under the key `geary:prefabs`. When decoding these will be stored in
 * [DecodedEntityData.type].
 */
context(Geary)
fun PersistentDataContainer.encodePrefabs(
    keys: Collection<PrefabKey>
) {
    hasComponentsEncoded = true

    // I prefer being explicit with the SetSerializer to avoid any confusion, like a class that looks like a persisting
    // component that stores a list of prefabs.
    encode(
        keys.toSet(),
        SetSerializer(PrefabKey.serializer()),
        PREFABS_KEY
    )
}

/** Decodes [PrefabKey]s under the key `geary:prefabs`. */
context(Geary)
fun PersistentDataContainerView.decodePrefabs(): Set<PrefabKey> =
    decode(PREFABS_KEY, SetSerializer(PrefabKey.serializer()))
        ?.map { key ->
            // Migrate namespace if needed
            val migrated = PrefabNamespaceMigrations.migrations.getOrDefault(key.namespace, key.namespace)
            PrefabKey.of(migrated, key.key)
        }
        ?.toSet()
        ?: emptySet()

/**
 * Decodes a set of components from this [PersistentDataContainerView].
 *
 * @see decode
 */
context(Geary)
fun PersistentDataContainerView.decodeComponents(): DecodedEntityData =
    DecodedEntityData(
        // only include keys that start with the component prefix and remove it to get the serial name
        persistingComponents = keys
            .filter { it.key.startsWith(COMPONENT_PREFIX) }
            .mapNotNull { decode(it) }
            .toSet(),
        type = GearyEntityType(decodePrefabs().mapNotNull {
            relationOf<InstanceOf?>(entityOfOrNull(it) ?: return@mapNotNull null).id
        })
    )

/** Verifies a [PersistentDataContainer] has a tag identifying it as containing Geary components. */
var PersistentDataContainer.hasComponentsEncoded: Boolean
    get() = has(COMPONENTS_KEY, PersistentDataType.BYTE) || has(PREFABS_KEY, BYTE_ARRAY)
    set(value) {
        when {
            value -> if (!hasComponentsEncoded) set(COMPONENTS_KEY, PersistentDataType.BYTE, 1)
            else -> remove(COMPONENTS_KEY)
        }
    }

/** Verifies a [PersistentDataContainerView] has a tag identifying it as containing Geary components. */
val PersistentDataContainerView.hasComponentsEncoded
    get() = has(COMPONENTS_KEY, PersistentDataType.BYTE) || has(PREFABS_KEY, BYTE_ARRAY)
