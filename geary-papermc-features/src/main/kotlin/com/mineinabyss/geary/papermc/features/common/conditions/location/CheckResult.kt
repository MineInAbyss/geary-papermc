package com.mineinabyss.geary.papermc.features.common.conditions.location

sealed interface CheckResult {
    data object Success : CheckResult
    data class Failure(val message: String) : CheckResult

    fun successOrThrow() = when (this) {
        is Success-> true
        is Failure -> error(message)
    }
}
