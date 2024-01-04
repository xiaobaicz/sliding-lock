# Sliding Lock

### Synopsis
Provides a simple sliding password function

### Use
~~~ gradle
dependencyResolutionManagement {
  repositories {
    // ...
    maven { url 'https://jitpack.io' }
  }
}
~~~

~~~ gradle
dependencies {
  // ...
  implementation 'com.github.xiaobaicz:sliding-lock:1.0.1'
}
~~~

### Example
~~~ xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <io.github.xiaobaicz.lib.widgets.SlidingLockView
        android:id="@+id/lock"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:radius="16dp"
        app:lock_color="#000"
        app:line_color="#000"
        app:line_width="16dp"
        app:row="3"/>

</FrameLayout>
~~~

~~~ kotlin
class Main : FragmentActivity() {

    private val bind by lazy { PageMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        // Slide completion listener
        bind.lock.onSlidingComplete = SlidingLockView.OnSlidingComplete {
            println(it)
        }
    }

}
~~~

<img width="320" alt="image" src="https://github.com/xiaobaicz/sliding-lock/assets/18556036/c0465704-8b5a-4023-ac10-3b0b4dbbc544">

<img width="320" alt="image" src="https://github.com/xiaobaicz/sliding-lock/assets/18556036/58a7fbda-c821-495e-a413-5bd98300137d">

