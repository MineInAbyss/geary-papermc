package com.mineinabyss.geary.papermc.sqlite

import com.mineinabyss.geary.papermc.sqlite.PersistentStorage.tableNameFor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.dvyy.sqlite.Transaction
import me.dvyy.sqlite.WriteTransaction
import org.bukkit.entity.Player

/**
 * WIP api for persistent data store via sqlite
 */
private object PersistentStorage {
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun tableNameFor(serialDescriptor: SerialDescriptor) =
        "\"component_${serialDescriptor.serialName}\""

    fun tableFor(serializer: KSerializer<*>): String = TODO() /*Table(
        """
        CREATE TABLE IF NOT EXISTS ${tableNameFor(serializer.descriptor)} (
            uuid BLOB NOT NULL PRIMARY KEY,
            data TEXT NOT NULL
        ) STRICT;
        """.trimIndent()
    )*/
}

context(tx: Transaction)
private inline fun <reified T> Player.getStored(): T? {
    val serializer = serializer<T>()
    val serialized = tx.select(
        "SELECT data FROM ${tableNameFor(serializer.descriptor)} WHERE uuid = :uuid",
        uniqueId
    ).firstOrNull { getText(0) }
    return PersistentStorage.json.decodeFromString(serializer, serialized ?: return null)
}

context(tx: WriteTransaction)
private inline fun <reified T> Player.store(data: T) {
    val serializer = serializer<T>()
    val serialized = PersistentStorage.json.encodeToString(serializer, data)
    tx.exec(
        """
        INSERT INTO ${tableNameFor(serializer.descriptor)}
        VALUES (:uuid, :data)
        ON CONFLICT (uuid) DO UPDATE SET data = json(:data)
        """.trimIndent(),
        uniqueId, serialized
    )
}
