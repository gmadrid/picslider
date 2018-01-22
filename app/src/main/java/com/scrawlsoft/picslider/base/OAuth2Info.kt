package com.scrawlsoft.picslider.base

import android.net.Uri

typealias ClientId = String
typealias ClientSecret = String

data class OAuth2Info(
        val login: Uri,
        val clientId: ClientId,
        val clientSecret: ClientSecret,
        val redirectUri: Uri)

