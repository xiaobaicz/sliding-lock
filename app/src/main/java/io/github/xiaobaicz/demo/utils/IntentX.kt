package io.github.xiaobaicz.demo.utils

import android.app.Activity
import android.content.Intent

inline fun <reified T: Activity> Activity.toPage() {
    startActivity(Intent(this, T::class.java))
}