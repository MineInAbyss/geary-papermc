package com.mineinabyss.geary.papermc.features.common.conditions.location

sealed interface CheckResult {
    data object Success : CheckResult

    /** The conditions were evaluated and did not match, the normal way for a spawn to be rejected */
    data class Failure(val message: String) : CheckResult

    /** A condition could not be evaluated at all, ex. a malformed matcher, so the config needs fixing */
    data class Error(val message: String) : CheckResult

    /** Whether the check matched, throwing only when a condition was impossible to evaluate */
    fun passedOrThrow() = when (this) {
        is Success -> true
        is Failure -> false
        is Error -> error(message)
    }
}
