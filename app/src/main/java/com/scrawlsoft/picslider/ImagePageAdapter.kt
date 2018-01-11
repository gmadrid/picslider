package com.scrawlsoft.picslider

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.scrawlsoft.picslider.images.ImageDisplayAndCache
import com.squareup.picasso.Callback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

class ImagePageAdapter(private val context: Context,
                       private val service: ImageService,
                       private val thing: ImageDisplayAndCache)
    : PagerAdapter() {

    inner class ViewHolder(val layout: View,
                           private val entryId: EntryId,
                           var loaded: Boolean = false) : Callback {
        override fun onSuccess() {
            loaded = true
        }

        override fun onError() {
            // TODO do something here.
        }

        fun markViewed(service: ImageService) {
            service.markAsRead(listOf(entryId))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy {
                        // TODO: do something here.
                    }
        }
    }

    private var primaryItem: ViewHolder? = null

    var entries: List<ImageService.Entry> = emptyList()
        set(value) {
            field = value; notifyDataSetChanged()
        }

    override fun getCount(): Int {
        return entries.size
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.main_page, container, false)
        val view = layout.findViewById<ImageView>(R.id.pager_image)

        val entry = entries[position]
        thing.displayIntoView(entry.uri, view)
        container.addView(layout)
        return ViewHolder(layout, entry.id)
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView((view as ViewHolder).layout)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        primaryItem = (obj as ViewHolder).also {
            if (it.loaded) {
                it.markViewed(service)
            }
        }
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean = (obj as ViewHolder).layout == view

    override fun notifyDataSetChanged() {
        primaryItem = null
        super.notifyDataSetChanged()
    }
}