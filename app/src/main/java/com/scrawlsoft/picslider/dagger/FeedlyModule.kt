package com.scrawlsoft.picslider.dagger

import android.content.res.Resources
import com.scrawlsoft.picslider.R
import com.scrawlsoft.picslider.feedly.FeedlyApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton


@Module
class FeedlyModule {
    private val defaultBaseUrl = "https://cloud.feedly.com/"

    @Provides
    @Named("feedlyUserToken")
    fun provideFeedlyUserToken(resources: Resources): String {
        return resources.getString(R.string.feedly_token)
    }

    @Provides
    @Singleton
    fun provideFeedlyApi(builder: Retrofit.Builder): FeedlyApi {
        return builder.baseUrl(defaultBaseUrl).build().create(FeedlyApi::class.java)
    }
}
