package mapogolions.expirable

import mapogolions.expirable.internal.ExpirableHooksImpl
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class ExpirableCollectionTest {
    @Test fun shouldManageObjectLifetimeThroughTtl() {
        // arrange
        val latch = CountDownLatch(1)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun afterCleanup(expirables: ExpirableCollection<String, Item>) = latch.countDown()
        }
        val items = ExpirableCollection(defaultCleanupInterval = 500, hooks = hooks)

        // act
        items.getOrPut("a", { Item(it) }, ttl = 50)
        latch.await()

        // assert
        assertEquals(items.size, 0)
    }

    @Test fun getOrPut_shouldReturnTheSameObject_whenObjectIsNotExpired() {
        // arrange
        val items = ExpirableCollection<String, Item>(defaultCleanupInterval = 100)
        val factory: (String) -> Item = { Item(it) }

        // act
        val a = items.getOrPut("foo", factory, ttl = 2000)
        val b = items.getOrPut("foo", factory, ttl = 2000)

        // assert
        assertSame(a, b)
    }

    @Test fun getOrPut_shouldReturnDifferentObject_whenObjectIsExpired() {
        // arrange
        val latch = CountDownLatch(1)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun onExpire(expirable: Expirable<String, Item>) = latch.countDown()
        }
        val items = ExpirableCollection(defaultCleanupInterval = 100, hooks = hooks)
        val factory: (String) -> Item = { Item(it) }

        // act
        val a = items.getOrPut("foo", factory, ttl = 20)
        latch.await()
        val b = items.getOrPut("foo", factory, ttl = 20)

        // assert
        assertNotSame(a, b)
    }

    @Test fun shouldCleanupQueueOfExpiredObjects() {
        // arrange
        val latch = CountDownLatch(2)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun onDequeue(expired: Expired<Item>) {
                gc(hints = 200) { expired.alive }
                latch.countDown()
            }
        }
        val items = ExpirableCollection(defaultCleanupInterval = 100, hooks = hooks)

        // act
        items.getOrPut("foo", { Item(it) }, ttl = 20)
        items.getOrPut("bar", { Item(it) }, ttl = 40)
        latch.await()

        // assert
        assertEquals(items.expiredItemsCount, 0)
    }
}
