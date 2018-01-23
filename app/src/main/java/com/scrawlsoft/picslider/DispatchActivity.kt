package com.scrawlsoft.picslider

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.scrawlsoft.picslider.base.KeyStore
import javax.inject.Inject

class DispatchActivity : AppCompatActivity() {

    @Inject
    lateinit var feedlyKeyStore: KeyStore

    // Check for login to Feedly, and if present continue to MainActivity, otherwise,
    // go off to LoginActivity and take care of things there.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as PicSliderApp).appComponent.inject(this)

        val nextClass = if (feedlyKeyStore.token?.isNotEmpty() ?: false) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, nextClass))
    }
}

