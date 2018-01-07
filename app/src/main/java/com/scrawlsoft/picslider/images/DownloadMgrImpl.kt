package com.scrawlsoft.picslider.images

import android.app.DownloadManager

class DownloadMgrImpl(private val downloadManager: DownloadManager) : DownloadMgr {
    override fun enqueue(req: DownloadManager.Request) {
        downloadManager.enqueue(req)
    }
}