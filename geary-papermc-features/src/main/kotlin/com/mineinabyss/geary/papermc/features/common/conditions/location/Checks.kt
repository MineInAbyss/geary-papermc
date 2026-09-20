package com.mineinabyss.geary.papermc.features.common.conditions.location

class Checks() {
    var failed = StringBuilder()
    var errored = StringBuilder()

    inline fun check(name: String, run: () -> Boolean) {
        val result = runCatching { run() }
            .onFailure { exception ->
                // Could not be evaluated at all, which is a config problem rather than a mismatch
                if (errored.isNotEmpty()) errored.appendLine()
                errored.append("$name: ${exception.message}")
            }
            .getOrNull() ?: return
        if (!result) {
            if (failed.isNotEmpty()) failed.appendLine()
            failed.append("$name: Condition failed")
        }
    }

    inline fun <T> checkOptional(name: String, argument: T?, run: (T) -> Boolean) {
        if (argument == null) return
        check(name) { run(argument) }
    }

    val result
        get() = when {
            errored.isNotEmpty() -> CheckResult.Error(errored.toString())
            failed.isNotEmpty() -> CheckResult.Failure(failed.toString())
            else -> CheckResult.Success
        }
}

inline fun checks(check: Checks.() -> Unit): CheckResult {
    return Checks().apply { check() }.result
}
