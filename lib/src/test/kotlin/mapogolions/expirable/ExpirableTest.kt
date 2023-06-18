package mapogolions.expirable

import mapogolions.expirable.internal.Item
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExpirableTest {
    @Test fun shouldCallCallback_whenItemExpires() {
        val latch = CountDownLatch(1)
        Expirable("foo", Item(), 50) {
            assertIs<Item>(it.value)
            latch.countDown()
        }
        latch.await()
        assertEquals(latch.count, 0)
    }
}
