package io.github.xiaobaicz.demo.utils

import android.widget.Toast
import io.github.xiaobaicz.demo.App

object ToastX {

    private val toast: Toast by lazy {
        Toast(App.app).apply {
            duration = Toast.LENGTH_SHORT
        }
    }

    fun show(text: String) {
        toast.cancel()
        toast.setText(text)
        toast.show()
    }

}