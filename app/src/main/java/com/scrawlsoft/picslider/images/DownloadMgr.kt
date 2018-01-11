package com.scrawlsoft.picslider.images

import android.app.DownloadManager
import android.net.Uri

/* Wrapper around Android DownloadManager to allow testing and provide some convenience funcs */
// TODO: needs testing.
interface DownloadMgr {
    fun enqueue(req: DownloadManager.Request)

    // TODO: pass Uri here instead of String
    private fun upscaleTumblrUri(uriString: String): String {
        val uri = Uri.parse(uriString)
        if (uri.host.contains("tumblr")) {
            // TODO: should I escape this '.'?
            val re = Regex("""_\d?00.""")
            val replaced = re.replace(uri.path, "_1280.")

            val newUri = uri.buildUpon().path(replaced).build()
            return newUri.toString()
        }
        return uriString
    }

    // TODO: pass Uri here instead of String
    fun downloadUri(uriString: String) {
        val uri = upscaleTumblrUri(uriString)
        val req = DownloadManager.Request(Uri.parse(uri))
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        enqueue(req)
    }

}