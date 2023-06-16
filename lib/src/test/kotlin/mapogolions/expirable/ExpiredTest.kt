package mapogolions.expirable

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpiredTest {
    @Test fun shouldBeAbleToDetectEndOfLifetime_whenThereIsNoReference() {
        val expired = expiredFactory(Foo())
        // anti-pattern
        var count = 100
        while (expired.alive && count-- > 0) System.gc()
        assertFalse(expired.alive)
    }

    @Test fun shouldBeAbleToDetectThatObjectIsAlive_whenThereIsAtLeastOneReachableReference() {
        val obj = Foo()
        val expired = expiredFactory(obj)
        // anti-pattern
        var count = 100
        while (expired.alive && count-- > 0) System.gc()
        assertTrue(expired.alive)
    }
}

fun <T> expiredFactory(obj: T): Expired<T> = Expired(obj)
internal class Foo {}
