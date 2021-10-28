package cc.xiaobaicz.slidinglock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cc.xiaobaicz.slidinglock.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {

    private val bind by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
    }

}