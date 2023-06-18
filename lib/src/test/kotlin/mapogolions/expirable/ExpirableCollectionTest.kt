package mapogolions.expirable

import mapogolions.expirable.internal.ExpirableHooksImpl
import mapogolions.expirable.internal.Item
import mapogolions.expirable.internal.gc
import mapogolions.expirable.internal.itemFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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
        items.getOrPut("a", ::itemFactory, ttl = 50)
        latch.await()

        // assert
        assertEquals(items.size, 0)
    }

    @Test fun getOrPut_shouldReturnTheSameObject_whenObjectIsNotExpired() {
        // arrange
        val items = ExpirableCollection<String, Item>(defaultCleanupInterval = 100)

        // act
        val a = items.getOrPut("foo", ::itemFactory, ttl = 2000)
        val b = items.getOrPut("foo", ::itemFactory, ttl = 2000)

        // assert
        assertSame(a, b)
    }

    @Test fun getOrPut_shouldConstructAndReturnNewObject_whenObjectIsExpired() {
        // arrange
        val latch = CountDownLatch(1)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun onExpire(expirable: Expirable<String, Item>) = latch.countDown()
        }
        val items = ExpirableCollection(defaultCleanupInterval = 100, hooks = hooks)

        // act
        val a = items.getOrPut("foo", ::itemFactory, ttl = 20)
        latch.await()
        val b = items.getOrPut("foo", ::itemFactory, ttl = 20)

        // assert
        assertNotSame(a, b)
    }

    @Test fun shouldRemoveObjectFromCleanupQueue_whenThereIsNoReachableReference() {
        // arrange
        val latch = CountDownLatch(2)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun onDequeue(expired: Expired<Item>) {
                gc(hints = 200) { expired.alive }
                latch.countDown()
            }
        }
        val items = ExpirableCollection(defaultCleanupInterval = 500, hooks = hooks)

        // act
        items.getOrPut("foo", ::itemFactory, ttl = 20)
        items.getOrPut("bar", ::itemFactory, ttl = 40)
        latch.await()

        // assert
        assertEquals(items.size, 0)
        assertEquals(items.cleanupQueueSize, 0)
    }

    @Test fun shouldBeAbleToUseObjectAfterExpiration_whenThereIsAtLeastOneReachableReference() {
        // arrange
        val latch = CountDownLatch(1)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun onDequeue(expired: Expired<Item>) {
                gc(hints = 200) { expired.alive }
                latch.countDown()
            }
        }
        val items = ExpirableCollection(defaultCleanupInterval = 500, hooks = hooks)

        // act
        val foo = items.getOrPut("foo", ::itemFactory, ttl = 20)
        latch.await()

        // assert
        assertEquals(items.size, 0)
        assertEquals(items.cleanupQueueSize, 1)
        assertEquals(foo.name, "foo")
    }
}
