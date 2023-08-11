package mapogolions.expirable

import mapogolions.expirable.internal.ExpirableHooks
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ExpirableCollection<K : Any, T>(
    defaultCleanupInterval: Long = 10_000,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS
) {
    private val defaultCleanupInterval: Long = timeUnit.toMillis(defaultCleanupInterval)
    private val expirables: ConcurrentHashMap<K, Lazy<Expirable<K, T>>> = ConcurrentHashMap()
    private val queue: ConcurrentLinkedQueue<Expired<T>> = ConcurrentLinkedQueue()
    private var timer: Timer? = null
    private val cleanupLock: Lock = ReentrantLock()

    private var hooks: ExpirableHooks<K, T>? = null

    internal constructor(
        defaultCleanupInterval: Long = 10_000,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
        hooks: ExpirableHooks<K, T>
    ) : this(defaultCleanupInterval, timeUnit) {
        this.hooks = hooks
    }

    fun getOrPut(key: K, factory: (K) -> T, ttl: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): T {
        val expirable = expirables.getOrPut(key) {
            lazy {
                Expirable(key, factory(key), timeUnit.toMillis(ttl)) { callback(it) }
            }
        }.value
        return expirable.value
    }

    val size: Int
        get() = expirables.size

    val cleanupQueueSize: Int
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
            var count = queue.size
            while (count-- > 0) {
                val expired = queue.poll()
                hooks?.onDequeue(expired)
                if (expired.alive) queue.add(expired)
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