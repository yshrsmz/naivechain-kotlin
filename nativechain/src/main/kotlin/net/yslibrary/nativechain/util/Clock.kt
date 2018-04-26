package net.yslibrary.nativechain.util

import java.util.concurrent.TimeUnit

interface Clock {
    fun currentTimeMillis(): Long
    fun currentTimeSeconds(): Long

    companion object {
        fun getDefault(): Clock = ClockImpl()
    }
}

class ClockImpl : Clock {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun currentTimeSeconds(): Long = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis())
}