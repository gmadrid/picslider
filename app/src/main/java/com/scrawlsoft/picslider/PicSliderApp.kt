package com.scrawlsoft.picslider

import android.app.Application
import com.scrawlsoft.picslider.dagger.AppComponent
import com.scrawlsoft.picslider.dagger.AppModule
import com.scrawlsoft.picslider.dagger.DaggerAppComponent

class PicSliderApp : Application() {
    lateinit var appComponent: AppComponent

    private fun initDagger(app: PicSliderApp): AppComponent =
            DaggerAppComponent.builder()
                    .appModule(AppModule(app))
                    .build()

    override fun onCreate() {
        super.onCreate()
        appComponent = initDagger(this)
    }
}
