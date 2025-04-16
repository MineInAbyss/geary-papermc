package com.mineinabyss.geary.papermc.plugin

import com.mineinabyss.geary.papermc.GearyPlugin
import com.mineinabyss.geary.papermc.scripting.GearyScriptHost
import com.mineinabyss.idofront.messaging.ComponentLogger
import com.mineinabyss.idofront.messaging.injectedLogger
import org.koin.core.logger.Logger
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module

object GearyKoinModules {
    fun logging(plugin: GearyPlugin) = module{
        single { plugin.injectedLogger() } binds arrayOf(Logger::class, ComponentLogger::class)
    }
    fun scripting() = module {
        singleOf(::GearyScriptHost)
    }
}
