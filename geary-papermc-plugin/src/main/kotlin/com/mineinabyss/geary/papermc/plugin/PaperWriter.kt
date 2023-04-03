package com.mineinabyss.geary.papermc.plugin

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Severity.*
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PaperWriter(val plugin: Plugin) : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val level: Level = when (severity) {
            Verbose -> Level.FINEST
            Debug -> Level.FINE
            Info -> Level.INFO
            Warn -> Level.WARNING
            Error -> Level.SEVERE
            Assert -> Level.SEVERE
        }
        plugin.logger.log(level, message)
        throwable?.printStackTrace()
    }
}
