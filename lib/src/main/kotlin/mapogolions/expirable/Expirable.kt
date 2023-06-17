package mapogolions.expirable

import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Expirable<K, T>(
    val key: K,
    val value: T,
    private val ttl: Long,
    private val callback: (Expirable<K, T>) -> Unit) {

    private var timer: Timer? = Timer(true)

    init {
        val self = this
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                timer!!.cancel()
                timer = null
                callback(self)
            }
        }, ttl, Long.MAX_VALUE)
    }
}