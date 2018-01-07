package com.scrawlsoft.picslider.images

import com.squareup.picasso.Callback

class ClosureCallback(private val successClosure: () -> Unit,
                      private val errorClosure: () -> Unit = {}) : Callback {
    override fun onSuccess() = successClosure()

    override fun onError() = errorClosure()
}