package com.example.sharity.data.device

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class MP3IndexerManager(private val indexer: MP3Indexer) {

    private var indexing = AtomicBoolean(false)
    private var error = AtomicReference<Exception?>(null)

    fun startIndex() {
        if (indexing.get()) {
            return
        }

        val thread = Thread(this::index)
        thread.name = "MP3-Indexer"
        thread.start()
        indexing.set(true)
        error.set(null)
    }

    private fun index() {
        try {
            indexer.index()
        } catch (e: Exception) {
            error.set(e)
        } finally {
            indexing.set(false)
        }
    }

    fun isIndexing(): Boolean {
        return indexing.get()
    }

    fun error(): Exception? {
        return error.get()
    }
}