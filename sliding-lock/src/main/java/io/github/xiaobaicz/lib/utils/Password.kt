package io.github.xiaobaicz.lib.utils

import io.github.xiaobaicz.lib.widgets.SlidingLockView

object Password {

    fun toString(nodes: List<SlidingLockView.Node>): String {
        val sb = StringBuilder()
        for ((index, node) in nodes.withIndex()) {
            if (index != 0)
                sb.append(",")
            sb.append("${node.id}")
        }
        return sb.toString()
    }

}