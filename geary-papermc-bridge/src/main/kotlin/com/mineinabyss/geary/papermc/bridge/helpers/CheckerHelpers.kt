package com.mineinabyss.geary.papermc.bridge.helpers

internal infix fun <T> T?.nullOrEquals(other: T?): Boolean =
    this == null || this == other

internal inline infix fun <T> T?.nullOr(check: (T) -> Boolean): Boolean =
    this == null || check(this)
