package com.mineinabyss.geary.papermc

import co.touchlab.kermit.Severity
import com.charleskorn.kaml.YamlComment
import com.mineinabyss.geary.modules.geary
import io.papermc.paper.util.TickThread
import kotlinx.serialization.Serializable
import org.bukkit.entity.EntityType
import org.spigotmc.AsyncCatcher

@Serializable
class GearyPaperConfig(
    @YamlComment("Convert bukkit entities to and from geary, for instance to store and persist components on a player.")
    val trackEntities: Boolean = true,
    val items: GearyItemConfig = GearyItemConfig(),
    @YamlComment("Convert blocks to and from geary.")
    val trackBlocks: Boolean = true,
    @YamlComment("If an item has no prefabs encoded, try to find its prefab by matching custom model data.")
    val migrateItemCustomModelDataToPrefab: Boolean = true,
    val catch: Catching = Catching(),
    val mobTypeConversion: MobTypeConversion = MobTypeConversion.IGNORE,
    @YamlComment("List of mob types to remove if they are not entities with Geary prefabs (i.e. vanilla entities)")
    val removeVanillaMobTypes: Set<EntityType> = emptySet(),
    val logLevel: Severity = Severity.Info,
)

@Serializable
class Catching(
    @YamlComment("Whether to throw an error when an entity read operation occurs outside of the server thread.")
    val asyncRead: CatchType = CatchType.IGNORE,
    @YamlComment("Whether to throw an error when an entity write operation occurs outside of the server thread.")
    val asyncWrite: CatchType = CatchType.ERROR,
    @YamlComment("Whether to throw an error when converting bukkit concepts to geary entities outside of the server thread.")
    val asyncEntityConversion: CatchType = CatchType.IGNORE,
    val asyncRecordsAccess: CatchType = CatchType.IGNORE,
    val asyncArchetypeProviderAccess: CatchType = CatchType.IGNORE,
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



enum class MobTypeConversion {
    MIGRATE, REMOVE, IGNORE
}

@Serializable
data class GearyItemConfig(
    val enabled: Boolean = true,
    val migrateByCustomModelData: Boolean = false,
    val autoDiscoverVanillaRecipes: Boolean = false
)
