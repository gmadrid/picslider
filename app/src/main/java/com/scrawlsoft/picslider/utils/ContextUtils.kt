package com.scrawlsoft.picslider.utils

import android.content.Context
import android.widget.Toast

fun Context.toast(msg: CharSequence, duration: Int) {
    Toast.makeText(this, msg, duration).show()
}

fun Context.shortToast(msg: CharSequence) {
    toast(msg, Toast.LENGTH_SHORT)
}

fun Context.longToast(msg: CharSequence) {
    toast(msg, Toast.LENGTH_LONG)
}