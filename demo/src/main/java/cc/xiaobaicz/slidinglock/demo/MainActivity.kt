package cc.xiaobaicz.slidinglock.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.xiaobaicz.slidinglock.demo.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
    }

}