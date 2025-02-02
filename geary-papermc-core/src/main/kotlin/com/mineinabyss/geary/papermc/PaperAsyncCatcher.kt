package com.mineinabyss.geary.papermc

import ca.spottedleaf.moonrise.common.util.TickThread
import com.mineinabyss.geary.helpers.async.AsyncCatcher

class PaperAsyncCatcher: AsyncCatcher {
    override fun isAsync(): Boolean {
        return !TickThread.isTickThread()
    }

    override fun throwException(message: String) {
        throw IllegalStateException("Thread " + Thread.currentThread().name + " failed main thread check: " + message)
    }
}
