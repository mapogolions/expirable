package mapogolions.expirable

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExpirableTest {
    @Test fun shouldCallCallbackOnlyOnce_whenItemIsExpired() {
        val latch = CountDownLatch(2)
        Expirable("foo", Foo(), 50) {
            assertIs<Foo>(it)
            latch.countDown()
        }
        latch.await(1, TimeUnit.SECONDS)
        assertEquals(latch.count, 1)
    }
}
