package com.statsup

interface Listener<T> {
    fun update(subject: T)
}