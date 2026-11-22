package com.scrollthegita.core.common

import javax.inject.Inject

fun interface Clock {
    fun now(): Long
}

class AppClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
}
