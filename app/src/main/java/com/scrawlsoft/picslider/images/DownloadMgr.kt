package com.scrawlsoft.picslider.images

import android.app.DownloadManager

interface DownloadMgr {
    fun enqueue(req: DownloadManager.Request)
}