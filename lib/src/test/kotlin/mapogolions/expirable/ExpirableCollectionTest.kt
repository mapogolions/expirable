package mapogolions.expirable

import mapogolions.expirable.internal.ExpirableHooksImpl
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpirableCollectionTest {
    @Test fun shouldUseDefaultTimeToLive() {
        // arrange
        val latch = CountDownLatch(1)
        val hooks = object : ExpirableHooksImpl<String, Item>() {
            override fun afterCleanup(expirables: ExpirableCollection<String, Item>) = latch.countDown()
        }
        val expirables = ExpirableCollection(defaultTtl = 50, defaultCleanupInterval = 500, hooks = hooks)

        // act
        discard(expirables.getOrPut("a", { Item(it) }))
        latch.await()

        // assert
        assertEquals(expirables.size, 0)
    }
}

fun <T> discard(arg: T) = Unit
