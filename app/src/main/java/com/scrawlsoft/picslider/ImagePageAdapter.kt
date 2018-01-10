package com.scrawlsoft.picslider

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.scrawlsoft.picslider.images.ImageDisplayAndCache

class ImagePageAdapter(private val context: Context,
                       private val thing: ImageDisplayAndCache)
    : PagerAdapter() {
    data class ViewHolder(val layout: View)

    var entries: List<ImageService.Entry> = emptyList()
        set(value) {
            println("SETTING: ${value.size}")
            field = value; notifyDataSetChanged()
        }

    override fun getCount(): Int {
        println("NUMITEMS: ${entries.size}")
        return entries.size
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        println("INSTAN")
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.main_page, container, false)
        val view = layout.findViewById<ImageView>(R.id.pager_image)
        thing.displayIntoView(entries[position].uri, view)
        println("DISPLAYED INTO VIEW: $position")
        container.addView(layout)
        val vh = ViewHolder(layout)
        println("INSTANVH: $vh")
        return vh
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        println("DESTROY: $view")
        container.removeView((view as ViewHolder).layout)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        println("SETTING ITEM: $obj")
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
}