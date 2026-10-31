package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.papermc.GearyPaperConfig
import com.mineinabyss.geary.papermc.creativetabs.CreativeTabsFeature
import com.mineinabyss.geary.papermc.features.items.recipes.RecipeFeature
import com.mineinabyss.geary.papermc.features.prefabs.PrefabsFeature
import com.mineinabyss.geary.papermc.features.resourcepacks.ResourcepackGeneratorFeature
import com.mineinabyss.geary.papermc.spawning.SpawningFeature
import com.mineinabyss.geary.papermc.spawning.locations.LocationsFeature
import com.mineinabyss.idofront.commands.brigadier.Args
import com.mineinabyss.idofront.commands.brigadier.IdoCommand
import com.mineinabyss.idofront.commands.brigadier.oneOf
import com.mineinabyss.idofront.config.SingleConfig
import com.mineinabyss.idofront.features.DICommandContext
import com.mineinabyss.idofront.features.get
import com.mineinabyss.idofront.features.mainCommand
import com.mineinabyss.idofront.messaging.error
import com.mineinabyss.idofront.messaging.success
import com.mineinabyss.idofront.messaging.warn

private val reloadableFeatures: List<DI.Module> = listOf(
    PrefabsFeature,
    ResourcepackGeneratorFeature,
    RecipeFeature,
    LocationsFeature,
    SpawningFeature,
    CreativeTabsFeature,
)

val ReloadFeature = module("reload") { }.mainCommand {
    reload()
}

context(di: DICommandContext)
private fun IdoCommand.reload() = ("reload" / "rl") {
    permission = "geary.admin.reload"

    executes {
        get<SingleConfig<GearyPaperConfig>>().updateCached()
        val results = di.scope.reload(*reloadableFeatures.toTypedArray()).results
        val (skipped, failed) = results.filterValues { it.isFailure }.entries.partition { (_, result) -> result.isSkipped }
        val reloaded = results.values.count { it.isSuccess }
        val skippedNote = skipped.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = ", skipped: ") { (feature, result) -> "${feature.name} (${result.exceptionOrNull()?.message})" }
            .orEmpty()
        if (failed.isEmpty()) sender.success("Reloaded $reloaded features$skippedNote")
        else sender.error(
            "Reloaded $reloaded features$skippedNote, failed: ${failed.joinToString { it.key.name }}\n" +
                    "See console for details"
        )
    }

    executes.args(
        "feature" to Args.string().oneOf { reloadableFeatures.map { it.name } }
    ) { featureName ->
        val feature = reloadableFeatures.find { it.name == featureName } ?: fail("Feature $featureName not found")
        get<SingleConfig<GearyPaperConfig>>().updateCached()
        val result = di.scope.reload(feature).results[feature]
        when {
            result == null || result.isSuccess -> sender.success("Reloaded feature $featureName")
            result.isSkipped -> sender.warn("Skipped feature $featureName: ${result.exceptionOrNull()?.message}")
            else -> sender.error("Failed to reload feature $featureName, see console for details")
        }
    }
}

// require() in a feature throws this for a disabled config option or a missing plugin, which is not a broken reload
private val Result<*>.isSkipped get() = exceptionOrNull() is IllegalArgumentException
