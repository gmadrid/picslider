package com.scrawlsoft.picslider

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.scrawlsoft.picslider.images.ImageDisplayAndCache

class ImagePageAdapter(private val context: Context,
                       private val entries: List<ImageService.Entry>,
                       private val thing: ImageDisplayAndCache)
    : PagerAdapter() {
    override fun getCount(): Int = entries.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        println("ENTRIES SIZE: ${entries.size}")

        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.main_page, container, false)
        val view = layout.findViewById<ImageView>(R.id.pager_image)
        thing.displayIntoView(entries[position].uri, view)
        container.addView(layout)
        return layout
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
}