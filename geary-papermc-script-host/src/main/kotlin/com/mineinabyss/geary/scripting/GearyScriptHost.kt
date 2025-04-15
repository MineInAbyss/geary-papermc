package com.mineinabyss.geary.scripting

import com.mineinabyss.geary.modules.geary
import com.mineinabyss.geary.papermc.GearyPaper
import com.mineinabyss.geary.papermc.gearyPaper
import com.mineinabyss.idofront.config.IdofrontConfig
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.security.MessageDigest
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.impl.createScriptFromClassLoader
import kotlin.script.experimental.jvm.util.classpathFromClassloader
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

private const val COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR = "KOTLIN_SIMPLE_MAIN_KTS_COMPILED_SCRIPTS_CACHE_DIR"
private const val COMPILED_SCRIPTS_CACHE_DIR_PROPERTY = "kotlin.simple.main.kts.compiled.scripts.cache.dir"

object GearyScriptHost {
    private val host = BasicJvmScriptingHost(/*ScriptingHostConfiguration {
        jvm {
            baseClassLoader.put(IdofrontConfig::class.java.classLoader)
        }
    }*/)

    fun evalFile(scriptFile: File): ResultWithDiagnostics<EvaluationResult> {
        Thread.currentThread().contextClassLoader = GearyPaper::class.java.classLoader
        return host.eval(scriptFile.toScriptSource(), ScriptWithMavenDepsConfiguration, null)
    }

    fun <T : Any> evaluateObject(scriptFile: File): T {
        return (evalFile(scriptFile = scriptFile).valueOrThrow().returnValue as ResultValue.Value).value as T
    }
}

private object ScriptWithMavenDepsConfiguration : ScriptCompilationConfiguration(body = {
//    defaultImports(DependsOn::class, Repository::class)
    jvm {
//        dependenciesFromCurrentContext(wholeClasspath = true)
//        dependenciesFromClassloader(
//            classLoader = gearyPaper.plugin.javaClass.classLoader,
////             "kotlin-stdlib", "kotlin-reflect", "kotlin-scripting-dependencies"
//            wholeClasspath = true
//        )
//        dependenciesFromCurrentContext(wholeClasspath = true)
        dependenciesFromClassContext(
            GearyPaper::class,
            wholeClasspath = true,
        )
        dependenciesFromClassContext(
            IdofrontConfig::class,
            wholeClasspath = true,
        )
    }
    ide {
        acceptedLocations(ScriptAcceptedLocation.Project)
    }
    hostConfiguration(ScriptingHostConfiguration {
//        jvm {
//            baseClassLoader.put(IdofrontConfig::class.java.classLoader)
//        }
//    })
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
private fun compiledScriptUniqueName(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration): String {
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
