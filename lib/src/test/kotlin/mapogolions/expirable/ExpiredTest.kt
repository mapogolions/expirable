package mapogolions.expirable

import kotlin.test.Test
import kotlin.test.assertFalse

class ExpiredTest {
    @Test fun shouldBeAbleToTrackEndOfLifetime() {
        val expired = expiredFactory(Foo())
        // anti pattern
        var count = 100
        while (expired.alive && count > 100) System.gc()

        assertFalse(expired.alive)
    }
}

fun <T> expiredFactory(obj: T): Expired<T> = Expired(obj)
internal class Foo {}
