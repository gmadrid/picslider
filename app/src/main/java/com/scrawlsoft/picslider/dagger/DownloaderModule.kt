package com.scrawlsoft.picslider.dagger

import android.app.DownloadManager
import android.content.Context
import com.scrawlsoft.picslider.images.DownloadMgr
import com.scrawlsoft.picslider.images.DownloadMgrImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Singleton
class DownloaderModule {
    @Provides
    fun provideDownloadMgr(context: Context): DownloadMgr {
        return DownloadMgrImpl(context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
    }
}