package mapogolons.expirable

import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Expirable<K, T>(
    val key: K,
    val value: T,
    private val ttl: Long,
    private val callback: (Expirable<K, T>) -> Unit) : TimerTask() {

    private val lock: Lock = ReentrantLock()
    private var timer: Timer? = Timer(true)
    private var expired: Boolean = false

    init {
        timer!!.schedule(this, ttl, Long.MAX_VALUE)
    }

    override fun run() {
        if (expired) return
        lock.withLock {
            if (expired) return
            timer!!.cancel()
            timer = null
            expired = true
        }
        callback(this)
    }
}