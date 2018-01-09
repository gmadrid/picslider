package com.scrawlsoft.picslider

import io.reactivex.Single
import java.net.URL

const val NO_CONTINUATION = ""

interface ImageService {
    data class Category(val id: String, val name: String, val description: String?)
    data class Entry(val id: String, val uri: URL)

    val categories: Single<List<Category>>
    fun getEntryIdsForCategory(categoryId: String, continuation: String?)
            : Single<Pair<String, List<String>>>
    fun getEntriesForIds(entryIds: List<String>): Single<List<Entry>>
}
