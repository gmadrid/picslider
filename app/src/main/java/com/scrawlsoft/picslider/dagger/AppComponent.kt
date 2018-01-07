package com.scrawlsoft.picslider.dagger

import com.scrawlsoft.picslider.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    DownloaderModule::class,
    FeedlyModule::class,
    PicassoModule::class,
    RetrofitModule::class
])
interface AppComponent {
    fun inject(target: MainActivity)
}
