package mapogolons.expirable

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ExpirableCollection<K : Any, T>(
    private val defaultTtl: Long,
    private val defaultCleanupInterval: Long = 10_000
) : TimerTask() {
    private val expirables: ConcurrentHashMap<K, Lazy<Expirable<K, T>>> = ConcurrentHashMap()
    private val queue: ConcurrentLinkedQueue<Expired<T>> = ConcurrentLinkedQueue()
    private var timer: Timer? = null
    private val lock: Lock = ReentrantLock()

    fun getOrPut(key: K, factory: () -> T, ttl: Long = defaultTtl): T {
        val expirable = expirables.getOrPut(key) {
            lazy {
                Expirable(key, factory(), ttl) { callback(it) }
            }
        }.value
        return expirable.value
    }

    private fun callback(item: Expirable<K, T>) {
        expirables.remove(item.key)
        queue.add(Expired(item.value))
        initCleanupTimer()
    }

    private fun initCleanupTimer() {
        lock.withLock {
            if (timer != null) {
                timer = Timer()
                timer!!.schedule(this, defaultCleanupInterval, Long.MAX_VALUE)
            }
        }
    }

    private fun discardCleanupTimer() {
        lock.withLock {
            timer!!.cancel()
            timer = null
        }
    }

    override fun run() {
        var index = 0
        val count = queue.size
        while (index < count) {
            val expired = queue.elementAt(index++)
            if (expired.alive) continue
            queue.remove(expired)
        }
        discardCleanupTimer()
        if (!queue.isEmpty()) {
            initCleanupTimer()
        }
    }
}