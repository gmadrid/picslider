package com.scrawlsoft.picslider

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    fun runAsync(func: () -> Unit) {
        Thread(Runnable { func() }).start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val foo = feedlyService.categories()
        runAsync {
            val r = foo.execute()
            val cats = r.body()
            if (cats != null) {
                val bar = feedlyService.entriesForStream(cats[0].id).execute()
                println(bar.body())
            }
        }
    }
}
