package com.scrawlsoft.picslider.images

import android.app.DownloadManager
import android.net.Uri

/* Wrapper around Android DownloadManager to allow testing and provide some convenience funcs */
// TODO: needs testing.
interface DownloadMgr {
    fun enqueue(req: DownloadManager.Request): Long

    private fun upscaleTumblrUri(uri: Uri): Uri {
        if (uri.host.contains("tumblr")) {
            // TODO: should I escape this '.'?
            val re = Regex("""_\d?00.""")
            val replaced = re.replace(uri.path, "_1280.")


            return uri.buildUpon().path(replaced).build()
        }
        return uri
    }

    fun downloadUri(uriIn: Uri): Pair<Long, Uri> {
        val uri = upscaleTumblrUri(uriIn)
        val req = DownloadManager.Request(uri)
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val result = enqueue(req)
        return result to uri
    }
}