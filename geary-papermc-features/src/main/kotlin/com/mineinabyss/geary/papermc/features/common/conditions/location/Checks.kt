package com.mineinabyss.geary.papermc.features.common.conditions.location

class Checks() {
    var failed = StringBuilder()

    inline fun check(name: String, run: () -> Boolean) {
        val result = runCatching { run() }
            .onFailure { exception ->
                with(failed) {
                    appendLine("$name: ${exception.message}")
                }

            }
            .getOrNull() ?: return
        if (!result) failed.appendLine("$name: Condition failed")
    }

    inline fun <T> checkOptional(name: String, argument: T?, run: (T) -> Boolean) {
        if (argument == null) return
        check(name) { run(argument) }
    }

    val result get() = if(failed.isEmpty()) CheckResult.Success else CheckResult.Failure(failed.toString())
}

inline fun checks(check: Checks.() -> Unit): CheckResult {
    return Checks().apply { check() }.result
}
