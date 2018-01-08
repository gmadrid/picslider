package com.scrawlsoft.picslider

import android.net.Uri
import io.reactivex.Observable
import io.reactivex.Single

interface ImageService {
    data class Category(val id: String, val name: String, val description: String?)
    data class Entry(val id: String, val uri: Uri)

    val categories: Single<List<Category>>
    fun getEntryIdsForCategory(categoryId: String): Single<Pair<String?, List<String>>>
    fun getEntriesForIds(entryIds: List<String>): Single<List<Entry>>
}
