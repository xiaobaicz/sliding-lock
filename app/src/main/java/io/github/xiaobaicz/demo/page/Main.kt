package io.github.xiaobaicz.demo.page

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.github.xiaobaicz.demo.databinding.PageMainBinding
import io.github.xiaobaicz.demo.store.Local
import io.github.xiaobaicz.demo.utils.ToastX
import io.github.xiaobaicz.demo.utils.toPage
import io.github.xiaobaicz.lib.utils.Password
import io.github.xiaobaicz.lib.widgets.SlidingLockView
import vip.oicp.xiaobaicz.lib.store.store

class Main : FragmentActivity() {

    private val bind by lazy { PageMainBinding.inflate(layoutInflater) }

    private val local = store<Local>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        bind.lock.onSlidingComplete = SlidingLockView.OnSlidingComplete {
            if (it.size < 4) {
                ToastX.show("Minimum 4 points")
                bind.lock.clean()
                return@OnSlidingComplete
            }
            if (local.password != Password.toString(it)) {
                ToastX.show("Incorrect Password")
                bind.lock.clean()
                return@OnSlidingComplete
            }
            ToastX.show("Verification Success")
        }
    }

    override fun onStart() {
        super.onStart()
        if (local.password.isNullOrBlank()) {
            toPage<Config>()
            return
        }
    }

}