package io.github.xiaobaicz.demo.page

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.github.xiaobaicz.demo.databinding.PageMainBinding
import io.github.xiaobaicz.lib.widgets.SlidingLockView

class Main : FragmentActivity() {

    private val bind by lazy { PageMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        bind.lock.onSlidingComplete = SlidingLockView.OnSlidingComplete {
            println(it)
        }
    }

}