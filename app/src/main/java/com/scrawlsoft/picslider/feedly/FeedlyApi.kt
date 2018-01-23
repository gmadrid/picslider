package com.scrawlsoft.picslider.feedly

import com.scrawlsoft.picslider.base.ClientId
import com.scrawlsoft.picslider.base.ClientSecret
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*
import java.net.URL

data class FeedlyTokenResponse(val id: String,
                               val refresh_token: String,
                               val access_token: String,
                               val token_type: String,
                               val plan: String)

data class FeedlyApiCategory(val id: String, val label: String, val description: String?)
data class FeedlyApiEntryIdsResponse(val ids: List<String>, val continuation: String? = null)

data class FeedlyApiJSONEntry(
        val id: String,
        val url: String?,
        internal val visual: FeedlyEntryVisual?,
        internal val summary: FeedlyEntrySummary?)

data class FeedlyEntryVisual(val url: String, val width: Int, val height: Int, val contentType: String)
data class FeedlyEntrySummary(val content: String)
data class FeedlyApiMarkerRequest(val action: String, val type: String, val entryIds: List<String>)

/**
 * Retrofit-style API description of the Feedly Dev API.
 * The description uses Moshi-style JSON annotations.
 * We also return RxJava 2 Observables.
 *
 * Instantiation with RetrofitBuilder:
 *     Retrofit.Builder()
 *         .baseUrl(defaultBaseUrl)
 *         .addConverterFactory(MoshiConverterFactory.create())
 *         .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
 *         .build()
 *         .create(FeedlyApi::class.java)
 */
interface FeedlyApi {

    @FormUrlEncoded
    @POST("/v3/auth/token")
    fun getToken(@Field("code") code: String,
                 @Field("client_id") clientId: ClientId,
                 @Field("client_secret") clientSecret: ClientSecret,
                 @Field("redirect_uri") redirectUri: String,
                 @Field("state") state: String = "",
                 @Field("grant_type") grantType: String = "authorization_code")
            : Single<FeedlyTokenResponse>

    @GET("/v3/categories")
    fun categories(@Header("Authorization") authHeader: String)
            : Single<List<FeedlyApiCategory>>

    @GET("/v3/streams/ids")
    fun entryIdsForStream(@Header("Authorization") authHeader: String,
                          @Query("streamId") streamId: String,
                          @Query("unreadOnly") unreadOnly: Boolean = true,
                          @Query("count") count: Int = 100,
                          @Query("ranked") ranked: String = "oldest",
                          @Query("continuation") continuation: String? = null)
            : Single<FeedlyApiEntryIdsResponse>

    @POST("/v3/entries/.mget")
    fun entriesForIds(@Body entryIds: List<String>): Single<List<FeedlyApiJSONEntry>>

    @POST("/v3/markers")
    fun mark(@Header("Authorization") authHeader: String,
             @Body body: FeedlyApiMarkerRequest): Completable
}
