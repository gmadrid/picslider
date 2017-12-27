package com.scrawlsoft.picslider.feedly

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

class FeedlyFetcher(private val userId: String, private val token: String) {

    data class FeedlyCategory(val id: String, val label: String, val description: String?)

    private val service: FeedlyApi

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        service = retrofit.create(FeedlyApi::class.java)
    }

    fun fetchCategories(): List<FeedlyCategory>? {
        val resp = service.categories().execute()
        return resp.body()
    }
}

private val BASE_URL = "https://cloud.feedly.com/"

// TODO: make the authentication an OKIO interceptor

private data class FeedlySubscriptionResponse(
        val id: String,
        val title: String,
        val categories: List<FeedlyFetcher.FeedlyCategory>
)

private data class FeedlyEntriesResponse(
        val ids: List<String>,
        val continuation: String?
)

private data class FeedlyEntryResponse(
        val id: String,
        val title: String
)

private const val AUTH_HEADER = "Authorization: OAuth Ay_DjsKr3hvAlo8Iki1JLIim01bIgQAFmV_Mhc25FXX2anbywwMe03dv1tF6160_Ouajc-1yHH4GZH5QZ_IRhq-h2aM0gAucae3K-mPXNXv_YzlNDTalg52DiOgnO3jvBtFlhvM1ix52GZ-n2X4qPGiqbC0RuwuiMx7wbaD338rdIWfPrAP8jXD30BUG6pALcnkw-86UlLnutPvq2YO4DBrZTm3DlnrjbdFV7qqaFdxOSeCxwmhzJrzoGZ7Xdg:feedlydev"

private interface FeedlyApi {
    @Headers(AUTH_HEADER)
    @GET("/v3/subscriptions")
    fun subscriptions(): Call<List<FeedlySubscriptionResponse>>

    @Headers(AUTH_HEADER)
    @GET("/v3/categories")
    fun categories(): Call<List<FeedlyFetcher.FeedlyCategory>>

    @Headers(AUTH_HEADER)
    @GET("/v3/streams/ids")
    fun entriesForStream(@Query("streamId") streamId: String): Call<FeedlyEntriesResponse>
}

