package com.scrawlsoft.picslider

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.map
import com.scrawlsoft.picslider.feedly.DEV_TOKEN
import com.scrawlsoft.picslider.feedly.DEV_USER
import com.scrawlsoft.picslider.feedly.FeedlyFetcher

class MainActivity : AppCompatActivity() {

    fun runAsync(func: () -> Unit) {
        Thread(Runnable { func() }).start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fetcher = FeedlyFetcher(DEV_USER, DEV_TOKEN)
        runAsync {
            fetcher.fetchCategories()
                    .flatMap {
                        fetcher.fetchEntryIds(it[0].id)
                    }
                    .flatMap {
                        val ids = it.ids.subList(0, 3)
                        fetcher.fetchEntriesForIds(ids)
                    }
                    .map {
                        println("XXXX: $it")
                    }
        }
    }
}
