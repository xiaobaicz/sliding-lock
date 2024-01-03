package io.github.xiaobaicz.lib.utils

object Utils {

    fun shrinkRange(min: Int, max: Int, value: Int) = when {
        value < min -> min
        value > max -> max
        else -> value
    }

}