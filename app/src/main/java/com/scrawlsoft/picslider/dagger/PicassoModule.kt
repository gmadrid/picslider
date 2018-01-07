package com.scrawlsoft.picslider.dagger

import android.content.Context
import com.scrawlsoft.picslider.images.ImageDisplayAndCache
import com.scrawlsoft.picslider.images.PicassoDisplayAndCache
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PicassoModule {
    @Provides
    @Singleton
    fun providePicasso(context: Context): Picasso =
            Picasso.with(context).apply {
                setIndicatorsEnabled(true)
            }

    @Provides
    @Singleton
    fun provideImageDisplayAndCache(picasso: Picasso): ImageDisplayAndCache =
            PicassoDisplayAndCache(picasso)
}