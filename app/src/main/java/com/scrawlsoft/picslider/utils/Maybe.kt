package com.scrawlsoft.picslider.utils

sealed class Maybe<out T : Any> {
    fun unwrap(): T? = when (this) {
        is Some -> value
        is None -> null
    }

}

data class Some<out T : Any>(val value: T) : Maybe<T>()
object None : Maybe<Nothing>()

fun <T : Any> T?.maybe(): Maybe<T> = if (this == null) None else Some(this)
