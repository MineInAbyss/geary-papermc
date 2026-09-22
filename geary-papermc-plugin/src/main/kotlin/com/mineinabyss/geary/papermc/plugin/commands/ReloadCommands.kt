package com.mineinabyss.geary.papermc.plugin.commands

import com.mineinabyss.dependencies.DI
import com.mineinabyss.dependencies.module
import com.mineinabyss.geary.papermc.GearyPaperConfig
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

private val reloadableFeatures: List<DI.Module> = listOf(
    PrefabsFeature,
    ResourcepackGeneratorFeature,
    RecipeFeature,
    LocationsFeature,
    SpawningFeature,
)

val ReloadFeature = module("reload") { }.mainCommand {
    reload()
}

context(di: DICommandContext)
private fun IdoCommand.reload() = ("reload" / "rl") {
    permission = "geary.admin.reload"

    executes {
        get<SingleConfig<GearyPaperConfig>>().updateCached()
        val result = di.scope.reload(*reloadableFeatures.toTypedArray())
        val failed = result.results.filterValues { it.isFailure }.keys
        if (failed.isEmpty()) sender.success("Reloaded ${result.results.size} features")
        else sender.error(
            "Reloaded ${result.results.size - failed.size}/${result.results.size} features, failed: ${failed.joinToString { it.name }}\n" +
                    "See console for details"
        )
    }

    executes.args(
        "feature" to Args.string().oneOf { reloadableFeatures.map { it.name } }
    ) { featureName ->
        val feature = reloadableFeatures.find { it.name == featureName } ?: fail("Feature $featureName not found")
        get<SingleConfig<GearyPaperConfig>>().updateCached()
        if (di.scope.reload(feature).isSuccess) sender.success("Reloaded feature $featureName")
        else sender.error("Failed to reload feature $featureName, see console for details")
    }
}
