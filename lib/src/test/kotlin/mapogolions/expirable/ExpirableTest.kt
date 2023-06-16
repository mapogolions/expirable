package mapogolions.expirable

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpirableTest {
    @Test fun shouldCallCallbackOnlyOnce_whenItemIsExpired() {
        val latch = CountDownLatch(4)
        Expirable("foo", Foo(), 50) { latch.countDown() }
        latch.await(1, TimeUnit.SECONDS)
        assertEquals(latch.count, 3)
    }
}
