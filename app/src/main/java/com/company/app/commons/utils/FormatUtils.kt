package com.company.app.commons.utils

import java.util.*

fun Double.toDistanceString(): String {
    return if (this < 1000) {
        String.format(Locale.getDefault(), "%.0fm", this)
    } else {
        String.format(Locale.getDefault(), "%.1fkm", this / 1000)
    }
}
