package com.scrawlsoft.picslider

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.clicks
import com.scrawlsoft.picslider.base.OAuth2Info
import com.scrawlsoft.picslider.feedly.FeedlyApi
import com.trello.rxlifecycle2.android.lifecycle.kotlin.bindToLifecycle
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var oAuthInfo: OAuth2Info

    @Inject
    lateinit var feedlyApi: FeedlyApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as PicSliderApp).appComponent.inject(this)
        setContentView(R.layout.activity_login)

        login_button.clicks()
                .bindToLifecycle(this)
                .subscribeBy {
                    // We build this URL by hand since we need to explicitly pass it
                    // to an Intent
                    val uri = oAuthInfo.login.buildUpon()
                            .appendQueryParameter("client_id", oAuthInfo.clientId)
                            .appendQueryParameter("redirect_uri", oAuthInfo.redirectUri.toString())
                            .appendQueryParameter("response_type", "code")
                            .appendQueryParameter("scope", "https://cloud.feedly.com/subscriptions")
                            .build()

                    val intent = Intent(ACTION_VIEW, uri)
                    startActivity(intent)
                }
    }

    override fun onResume() {
        super.onResume()

        intent.data?.let {
            if (it.host.contains("localhost")) {
                val code = it.getQueryParameter("code")
                if (code == null) {
                    TODO("Deal with error situation here")
                } else {
                    feedlyApi.getToken(code,
                            oAuthInfo.clientId, oAuthInfo.clientSecret, oAuthInfo.redirectUri.toString())
                            .flatMap { result ->
                                println("AT: ${result.access_token}")
                                feedlyApi.categories("OAuth ${result.access_token}")
                            }
                            .subscribeBy {
                                println("FOO: $it")
                            }
                }
            }
        }

    }
}