package mapogolions.expirable

import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Expirable<K, T>(
    val key: K,
    val value: T,
    private val ttl: Long,
    private val callback: (Expirable<K, T>) -> Unit) : AutoCloseable {

    private var timer: Timer? = Timer(true)
    private var disposed: Boolean = false

    init {
        val self = this
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                close()
                callback(self)
            }
        }, ttl, Long.MAX_VALUE)
    }

    override fun close() {
        if (disposed) return
        synchronized(this) {
            if (disposed) return
            timer!!.cancel()
            timer = null
            disposed = true
        }
    }
}