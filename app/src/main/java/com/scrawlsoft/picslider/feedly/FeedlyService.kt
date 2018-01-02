package com.scrawlsoft.picslider.feedly

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

private val defaultBaseUrl = "https://cloud.feedly.com/"

/**
 * Convenience wrapper around FeedlyApi.
 * Returned types are raw API data objects.
 * All streams returned will be subscribed on Schedulers.io().
 */
class FeedlyService() {
    @Suppress("unused")
    private val user = DEV_USER
    private val token = DEV_TOKEN
    private val authHeader = "OAuth $token"

    private val api: FeedlyApi = Retrofit.Builder()
            .baseUrl(defaultBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(FeedlyApi::class.java)

    fun getCategories(): Observable<List<FeedlyApiCategory>> {
        return api.categories(authHeader)
                .subscribeOn(Schedulers.io())
    }

    fun getEntryIdsForCategory(streamId: String): Observable<FeedlyApiEntryIdsResponse> {
        return api.entryIdsForStream(authHeader, streamId)
                .subscribeOn(Schedulers.io())
    }

    fun getEntriesForIds(entryIds: List<String>): Observable<List<FeedlyApiEntry>> {
        return api.entriesForIds(entryIds)
                .subscribeOn(Schedulers.io())
    }
}

