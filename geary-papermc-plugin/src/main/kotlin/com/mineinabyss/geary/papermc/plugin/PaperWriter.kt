package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.*
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PaperWriter(private val plugin: Plugin) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        plugin.logger.log(severityToLogLevel(severity), message)
        throwable?.printStackTrace()
    }

    companion object {
        // Spigot passes the java log level into log4j that's harder to configure, we'll just stick to info level
        // and filter on our end
        fun severityToLogLevel(severity: Severity): Level = when (severity) {
            Verbose -> Level.INFO
            Debug -> Level.INFO
            Info -> Level.INFO
            Warn -> Level.WARNING
            Error -> Level.SEVERE
            Assert -> Level.SEVERE
        }
    }
}
