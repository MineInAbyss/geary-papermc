package com.mineinabyss.geary.papermc

import ca.spottedleaf.moonrise.common.util.TickThread
import co.touchlab.kermit.Severity
import com.charleskorn.kaml.YamlComment
import com.mineinabyss.geary.modules.Geary
import kotlinx.serialization.Serializable
import org.bukkit.entity.EntityType
import org.spigotmc.AsyncCatcher

@Serializable
class GearyPaperConfig(
    val entities: EntityTrackingConfig = EntityTrackingConfig(),
    val items: ItemTrackingConfig = ItemTrackingConfig(),
    @YamlComment("Convert blocks to and from geary.")
    val trackBlocks: Boolean = true,
    @YamlComment("Whether to enable Minecraft-specific features for Geary like event bridging. Requires item and entity tracking to be enabled.")
    val minecraftFeatures: Boolean = true,
    @YamlComment("Whether to enable Geary actions (ex. used for item configs.)")
    val actions: Boolean = true,
    @YamlComment("Whether to register recipes defined in Geary prefabs")
    val recipes: Boolean = true,
    @YamlComment("Whether to load prefab files located in the Geary plugin folder.")
    val prefabLoading: Boolean = true,
    val catch: Catching = Catching(),
    val mobTypeConversion: MobTypeConversion = MobTypeConversion.IGNORE,
    @YamlComment("List of mob types to remove if they are not entities with Geary prefabs (i.e. vanilla entities)")
    val removeVanillaMobTypes: Set<EntityType> = emptySet(),
    val logLevel: Severity = Severity.Info,
    val resourcePack: ResourcePack = ResourcePack(),
    @YamlComment("Whether to enable Geary's spawning system")
    val spawning: Boolean = true,
)

@Serializable
class Catching(
    //TODO reimplement other catchers
//    @YamlComment("Whether to throw an error when an entity read operation occurs outside of the server thread.")
//    val asyncRead: CatchType = CatchType.IGNORE,
    @YamlComment("Whether to throw an error when an entity write operation occurs outside of the server thread.")
    val asyncWrite: CatchType = CatchType.ERROR,
    @YamlComment("Whether to throw an error when converting bukkit concepts to geary entities outside of the server thread.")
    val asyncEntityConversion: CatchType = CatchType.IGNORE,
//    val asyncRecordsAccess: CatchType = CatchType.IGNORE,
//    val asyncArchetypeProviderAccess: CatchType = CatchType.IGNORE,
) {
    companion object {
        fun asyncCheck(type: CatchType, message: String) {
            when (type) {
                CatchType.ERROR -> AsyncCatcher.catchOp(message)
                CatchType.WARN -> if (!TickThread.isTickThread()) {
                    Geary.w(message)
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
data class ItemTrackingConfig(
    val enabled: Boolean = true,
    @YamlComment("If an item has no prefabs encoded, try to find its prefab by matching custom model data.")
    val migrateByCustomModelData: Boolean = false,
    val autoDiscoverVanillaRecipes: Boolean = false,
)

@Serializable
data class EntityTrackingConfig(
    @YamlComment("Whether players should be tracked as entities in Geary")
    val trackPlayers: Boolean = true,
    @YamlComment("Whether all other Minecraft entities should be tracked as entities in Geary")
    val trackOtherEntities: Boolean = true,
)

@Serializable
data class ResourcePack(
    val generate: Boolean = true,
    @YamlComment("The path to generate the pack to from `plugins/Geary`", "Adding .zip to path will export as a zip instead of directory")
    val outputPath: String = "resourcepack.zip",
    @YamlComment("Points to a resourcepack in zip or directory format to merge into Geary's pack")
    val includedPackPath: String = "",
)
