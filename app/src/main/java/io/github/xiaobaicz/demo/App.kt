package io.github.xiaobaicz.demo

import android.app.Application
import com.tencent.mmkv.MMKV

class App : Application() {

    companion object {
        lateinit var app: App
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        app = this
    }

}