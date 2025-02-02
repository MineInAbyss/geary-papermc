package com.mineinabyss.geary.papermc

import ca.spottedleaf.moonrise.common.util.TickThread
import com.mineinabyss.geary.helpers.async.AsyncCatcher
import net.minecraft.server.MinecraftServer

class PaperAsyncCatcher: AsyncCatcher {
    override fun isAsync(): Boolean {
        return !TickThread.isTickThread()
    }

    override fun throwException(message: String) {
        MinecraftServer.LOGGER.error(
            "Thread " + Thread.currentThread().name + " failed main thread check: " + message,
            Throwable()
        )
        throw IllegalStateException("Asynchronous $message!")
    }
}
