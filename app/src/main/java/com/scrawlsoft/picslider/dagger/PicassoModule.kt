package com.scrawlsoft.picslider.dagger

import android.content.Context
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PicassoModule {
    @Provides
    @Singleton
    fun providePicasso(context: Context): Picasso {
        return Picasso.with(context)
    }
}