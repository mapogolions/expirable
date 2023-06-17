package mapogolions.expirable

import mapogolions.expirable.internal.ExpirableHooks
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ExpirableCollection<K : Any, T>(
    private val defaultCleanupInterval: Long = 10_000,
) {
    private val expirables: ConcurrentHashMap<K, Lazy<Expirable<K, T>>> = ConcurrentHashMap()
    private val queue: ConcurrentLinkedQueue<Expired<T>> = ConcurrentLinkedQueue()
    private var timer: Timer? = null
    private val cleanupLock: Lock = ReentrantLock()

    private var hooks: ExpirableHooks<K, T>? = null

    internal constructor(
        defaultCleanupInterval: Long,
        hooks: ExpirableHooks<K, T>
    ) : this(defaultCleanupInterval) {
        this.hooks = hooks
    }

    fun getOrPut(key: K, factory: (K) -> T, ttl: Long): T {
        val expirable = expirables.getOrPut(key) {
            lazy {
                Expirable(key, factory(key), ttl) { callback(it) }
            }
        }.value
        return expirable.value
    }

    val size: Int
        get() = expirables.size

    val expiredItemsCount: Int
        get() = queue.size

    private fun callback(expirable: Expirable<K, T>) {
        expirables.remove(expirable.key)
        queue.add(Expired(expirable.value))
        hooks?.onExpire(expirable)
        initCleanupTimer(defaultCleanupInterval)
    }

    private fun initCleanupTimer(delay: Long) {
        if (timer != null) return
        synchronized(this) {
            if (timer == null) {
                timer = Timer(true)
                timer!!.schedule(object : TimerTask() {
                    override fun run() = cleanup()
                }, delay, Long.MAX_VALUE)
            }
        }
    }

    private fun discardCleanupTimer() {
        timer!!.cancel()
        timer = null
    }

    private fun cleanup() {
        hooks?.beforeCleanup(this)
        // give a chance to other threads as soon as possible
        discardCleanupTimer()
        if (!cleanupLock.tryLock()) {
            return
        }
        try {
            var index = 0
            val count = queue.size
            while (index < count) {
                val expired = queue.elementAt(index++)
                hooks?.onDequeue(expired)
                if (expired.alive) continue
                queue.remove(expired)
            }
        } finally {
            cleanupLock.unlock()
        }
        hooks?.afterCleanup(this)
        if (!queue.isEmpty()) {
            initCleanupTimer(defaultCleanupInterval)
        }
    }
}