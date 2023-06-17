package mapogolions.expirable

import mapogolions.expirable.internal.ExpirableHooksImpl
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ExpirableCollectionTest {
    @Test fun lifetimeShouldBeEqualToTtl() {
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
        val latch = CountDownLatch(1)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun afterCleanup(expirables: ExpirableCollection<String, Item>) = latch.countDown()
        }
        val items = ExpirableCollection(defaultCleanupInterval = 500, hooks = hooks)
        val factory: (String) -> Item = { Item(it) }
        val ttl: Long = 2000

        // act
        val a = items.getOrPut("foo", factory, ttl)
        val b = items.getOrPut("foo", factory, ttl)

        // assert
        assertSame(a, b)
    }
}
