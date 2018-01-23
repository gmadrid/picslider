package com.scrawlsoft.picslider.feedly

import android.content.res.Resources
import com.scrawlsoft.picslider.BuildConfig
import com.scrawlsoft.picslider.R
import com.scrawlsoft.picslider.base.KeyStore

/*
  NOTE TO SELF
  In theory, we need 3 versions of this:
  1) For development: provides the Feedly Development Token
  2) For development: auth against the Feedly Sandbox
  3) For production: auth against Feedly Public Cloud

  We do not have the id/secret for #3 yet, but need #2 to get auth working.
 */
sealed class FeedlyKeyStore : KeyStore() {
    companion object {
        fun getInstance(resources: Resources): FeedlyKeyStore {
            if (BuildConfig.useFeedlyDevToken) {
                return FeedlyDevTokenKeyStoreImpl(resources)
            } else {
                return FeedlyPrivateKeyStore()
            }
        }
    }
}

private class FeedlyDevTokenKeyStoreImpl(resources: Resources) : FeedlyKeyStore() {
    override var token: String? = resources.getString(R.string.feedly_dev_token)
}

private class FeedlyPrivateKeyStore(override var token: String? = null) : FeedlyKeyStore()