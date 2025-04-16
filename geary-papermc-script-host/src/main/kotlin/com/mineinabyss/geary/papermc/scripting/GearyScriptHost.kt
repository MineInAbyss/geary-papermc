package com.mineinabyss.geary.papermc.scripting

import co.touchlab.kermit.Logger
import com.mineinabyss.geary.papermc.GearyPaper
import com.mineinabyss.idofront.config.IdofrontConfig
import java.io.File
import java.security.MessageDigest
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.jvmTarget
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache

private const val COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR = "KOTLIN_SIMPLE_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"
private const val COMPILED_SCRIPTS_CACHE_DIR_PROPERTY = "kotlin.simple.main.kts.compiled.scripts.cache.dir"

class GearyScriptHost(
    val logger: Logger,
) {
    private val host = BasicJvmScriptingHost()

    fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult> {
        logger.d { "Evaluting $scriptFile..." }
        Thread.currentThread().contextClassLoader = GearyPaper::class.java.classLoader
        return host.eval(scriptFile.toScriptSource(), ScriptWithMavenDepsConfiguration, null).also {
            logger.d { "Finished evaluating $scriptFile" }
        }
    }

    fun <T : Any> evalObject(scriptFile: File): Result<T> = runCatching {
        when(val returned = evalFile(scriptFile = scriptFile).valueOrThrow().returnValue) {
            is ResultValue.Value -> returned.value as T
            is ResultValue.Error -> throw returned.error
            else -> error("Script $scriptFile did not return a value")
        }
    }
}

private object ScriptWithMavenDepsConfiguration : ScriptCompilationConfiguration(body = {
    jvm {
        dependenciesFromClassContext(
            GearyPaper::class,
            wholeClasspath = true,
        )
        dependenciesFromClassContext(
            IdofrontConfig::class,
            wholeClasspath = true,
        )
        jvmTarget.put("21")
    }
    ide {
        acceptedLocations(ScriptAcceptedLocation.Project)
    }
    hostConfiguration(ScriptingHostConfiguration {
        jvm {
            val cacheExtSetting = System.getProperty(COMPILED_SCRIPTS_CACHE_DIR_PROPERTY)
                ?: System.getenv(COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR)
            val cacheBaseDir = when {
                cacheExtSetting == null -> System.getProperty("java.io.tmpdir")
                    ?.let(::File)?.takeIf { it.exists() && it.isDirectory }
                    ?.let { File(it, "main.kts.compiled.cache").apply { mkdir() } }

                cacheExtSetting.isBlank() -> null
                else -> File(cacheExtSetting)
            }?.takeIf { it.exists() && it.isDirectory }
            if (cacheBaseDir != null)
                compilationCache(
                    CompiledScriptJarsCache { script, scriptCompilationConfiguration ->
                        File(cacheBaseDir, compiledScriptUniqueName(script, scriptCompilationConfiguration) + ".jar")
                    }
                )
        }
    })
})

@OptIn(ExperimentalStdlibApi::class)
private fun compiledScriptUniqueName(
    script: SourceCode,
    scriptCompilationConfiguration: ScriptCompilationConfiguration,
): String {
    val digestWrapper = MessageDigest.getInstance("MD5")
    digestWrapper.update(script.text.toByteArray())
    scriptCompilationConfiguration.notTransientData.entries
        .sortedBy { it.key.name }
        .forEach {
            digestWrapper.update(it.key.name.toByteArray())
            digestWrapper.update(it.value.toString().toByteArray())
        }
    return digestWrapper.digest().toHexString()
}
