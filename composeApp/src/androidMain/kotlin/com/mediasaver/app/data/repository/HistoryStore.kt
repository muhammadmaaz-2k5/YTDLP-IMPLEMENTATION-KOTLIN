package com.mediasaver.app.data.repository

import com.mediasaver.app.domain.model.DownloadRecord
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persists [DownloadRecord] history as JSON at [historyFilePath].
 *
 * Thread-safety: all mutations are guarded by [mutex].
 * The in-memory [cache] is loaded lazily on first access.
 */
class HistoryStore(historyFilePath: String) {

    private val historyFile: File = File(historyFilePath).also { it.parentFile?.mkdirs() }

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val mutex = Mutex()
    private var cache: List<DownloadRecord>? = null

    /** Returns all persisted records, newest first. */
    suspend fun load(): List<DownloadRecord> = mutex.withLock {
        cache ?: readFromDisk().also { cache = it }
    }

    /** Prepends [record] to the history and persists to disk. */
    suspend fun add(record: DownloadRecord) = mutex.withLock {
        val current = cache ?: readFromDisk()
        val updated = listOf(record) + current
        cache = updated
        writeToDisk(updated)
    }

    /** Clears all history in memory and on disk. */
    suspend fun clear() = mutex.withLock {
        cache = emptyList()
        writeToDisk(emptyList())
    }

    /** Removes a single record by [id] from the history and persists the change. */
    suspend fun remove(id: String) = mutex.withLock {
        val current = cache ?: readFromDisk()
        val updated = current.filterNot { it.id == id }
        cache = updated
        writeToDisk(updated)
    }

    /** Replaces the record with the same id as [record] (e.g. after a rename) and persists the change. */
    suspend fun update(record: DownloadRecord) = mutex.withLock {
        val current = cache ?: readFromDisk()
        val updated = current.map { if (it.id == record.id) record else it }
        cache = updated
        writeToDisk(updated)
    }

    // -- Private helpers -----------------------------------------------------

    private fun readFromDisk(): List<DownloadRecord> = try {
        if (historyFile.exists()) {
            json.decodeFromString<List<DownloadRecord>>(historyFile.readText())
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        // Corrupted file -- start fresh rather than crashing
        emptyList()
    }

    private fun writeToDisk(records: List<DownloadRecord>) {
        try {
            historyFile.writeText(json.encodeToString(records))
        } catch (e: Exception) {
            // Non-fatal -- history persistence is best-effort
        }
    }
}
