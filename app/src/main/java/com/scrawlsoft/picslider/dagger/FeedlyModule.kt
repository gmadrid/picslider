package com.scrawlsoft.picslider.dagger

import android.content.res.Resources
import android.net.Uri
import com.scrawlsoft.picslider.BuildConfig
import com.scrawlsoft.picslider.R
import com.scrawlsoft.picslider.base.KeyStore
import com.scrawlsoft.picslider.base.OAuth2Info
import com.scrawlsoft.picslider.feedly.FeedlyApi
import com.scrawlsoft.picslider.feedly.FeedlyKeyStore
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
class FeedlyModule {
    private val defaultBaseUrl =
            if (!BuildConfig.useFeedlyDevToken && BuildConfig.useSandbox) "https://sandbox7.feedly.com/"
            else "http://cloud.feedly.com/"

    @Provides
    @Singleton
    fun providesFeedlyKeyStore(resources: Resources): KeyStore =
            FeedlyKeyStore.getInstance(resources)

    @Provides
    @Singleton
    fun provideFeedlyApi(builder: Retrofit.Builder): FeedlyApi =
            builder.baseUrl(defaultBaseUrl).build().create(FeedlyApi::class.java)

    @Provides
    @Singleton
    fun provideFeedlyOAuth2Info(resources: Resources): OAuth2Info = if (BuildConfig.DEBUG) {
        val clientId = resources.getString(R.string.feedly_sandbox_client)
        val clientSecret = resources.getString(R.string.feedly_sandbox_secret)

        OAuth2Info(Uri.parse("https://sandbox7.feedly.com/v3/auth/auth"),
                clientId,
                clientSecret,
                Uri.parse("http://localhost:8080/"))
    } else {
        throw IllegalStateException("No OAuth2 info for release build yet.")
    }
}
