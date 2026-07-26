package com.mineinabyss.geary.papermc.spawning.locations

import co.touchlab.kermit.Logger
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.mineinabyss.dependencies.addCloseable
import com.mineinabyss.dependencies.get
import com.mineinabyss.dependencies.module
import com.mineinabyss.dependencies.new
import com.mineinabyss.dependencies.single
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsConfig
import com.mineinabyss.geary.papermc.spawning.config.SpawnLocationsUnified
import com.mineinabyss.geary.serialization.SerializableComponents
import com.mineinabyss.idofront.config.config
import com.mineinabyss.idofront.features.listeners
import com.mineinabyss.idofront.features.plugin
import com.mineinabyss.idofront.plugin.Services

import org.bukkit.Bukkit
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div

val LocationsFeature = module("locations") {
    val yaml = Yaml(
        serializersModule = gearyPaper.worldManager.global.getAddon(SerializableComponents).formats.module,
        configuration = YamlConfiguration(strictMode = false),
    )
    val locationConfigReader = config<SpawnLocationsConfig> {
        format = yaml
    }.multiEntry((plugin.dataPath / "locations").createParentDirectories())

    single {
        val entries = locationConfigReader.read()
        entries.filter { it.entry.Locations.isEmpty() }.forEach {
            get<Logger>().w { "Locations entry '${it.key}' (${it.path.fileName}) has no Locations map, check the file format" }
        }
        SpawnLocationsUnified(entries).also {
            get<Logger>().i { "Loaded ${it.unified.size} region(s): ${it.unified.keys.joinToString()}" }
        }
    }
    single<RegionService> { new(::SpawnLocationRegionService) }

    val service = get<RegionService>()
    Services.register<RegionService>(plugin, service)
    addCloseable { Bukkit.getServer().servicesManager.unregister(RegionService::class.java, service) }

    listeners(new(::RegionTracker))
}
