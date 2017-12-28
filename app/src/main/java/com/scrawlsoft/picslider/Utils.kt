package com.scrawlsoft.picslider

import com.github.kittinunf.result.Result
import com.squareup.picasso.Picasso

fun <T : Any> nonNullResult(value: T?, e: Exception = Exception()): Result<T, Exception> {
    return if (value == null) {
        Result.Failure(e)
    } else {
        Result.Success(value)
    }
}

fun picasso(): Picasso {
    return Picasso.with(null)
}

