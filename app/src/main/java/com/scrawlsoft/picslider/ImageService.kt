package com.scrawlsoft.picslider

import io.reactivex.Single
import java.net.URL

typealias CategoryId = String
typealias EntryId = String

sealed class Continuation(val apiString: String?) {
    companion object {
        fun fromString(str: String?): Continuation {
            return if (str == null) NoContinuationToken else ContinuationToken(str)
        }
    }
}

object NoContinuationToken : Continuation(null)

class ContinuationToken(apiString: String) : Continuation(apiString)

interface ImageService {

    data class Category(val id: CategoryId, val name: String, val description: String?)
    data class Entry(val id: EntryId, val uri: URL)
    data class EntryIdsResponse(val continuation: Continuation, val ids: List<EntryId>)

    val categories: Single<List<Category>>
    fun getEntryIdsForCategory(categoryId: CategoryId, continuation: Continuation)
            : Single<EntryIdsResponse>

    fun getEntriesForIds(entryIds: List<EntryId>): Single<List<Entry>>
}
