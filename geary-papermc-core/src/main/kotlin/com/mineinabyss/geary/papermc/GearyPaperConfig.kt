package com.mineinabyss.geary.papermc

import co.touchlab.kermit.Severity
import com.charleskorn.kaml.YamlComment
import com.mineinabyss.geary.modules.geary
import io.papermc.paper.util.TickThread
import kotlinx.serialization.Serializable
import org.spigotmc.AsyncCatcher

@Serializable
class GearyPaperConfig(
    @YamlComment("Convert bukkit entities to and from geary, for instance to store and persist components on a player.")
    val trackEntities: Boolean = true,
    @YamlComment("Convert items to and from geary. Depends on entity tracking.")
    val trackItems: Boolean = true,
    @YamlComment("Convert blocks to and from geary.")
    val trackBlocks: Boolean = true,
    @YamlComment("Convert bukkit events to data in Geary (deprecated)")
    val bridgeEvents: Boolean = true,
    @YamlComment("If an item has no prefabs encoded, try to find its prefab by matching custom model data.")
    val migrateItemCustomModelDataToPrefab: Boolean = true,
    val catch: Catching = Catching(),
    val logLevel: Severity = Severity.Warn,
)

@Serializable
class Catching(
    @YamlComment("Whether to throw an error when an entity read operation occurs outside of the server thread.")
    val asyncRead: CatchType = CatchType.WARN,
    @YamlComment("Whether to throw an error when an entity write operation occurs outside of the server thread.")
    val asyncWrite: CatchType = CatchType.ERROR,
    @YamlComment("Whether to throw an error when converting bukkit concepts to geary entities outside of the server thread.")
    val asyncEntityConversion: CatchType = CatchType.WARN,
    val asyncRecordsAccess: CatchType = CatchType.WARN,
    val asyncArchetypeProviderAccess: CatchType = CatchType.WARN,
) {
    companion object{
        fun asyncCheck(type: CatchType, message: String) {
            when (type) {
                CatchType.ERROR -> AsyncCatcher.catchOp(message)
                CatchType.WARN -> if (!TickThread.isTickThread()) {
                    geary.logger.w(message)
                    IllegalStateException("(Ignoring) $message").printStackTrace()
                }
                CatchType.IGNORE -> Unit
            }
        }
    }
}

enum class CatchType {
    ERROR, IGNORE, WARN
}

