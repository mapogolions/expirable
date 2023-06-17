package mapogolions.expirable

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpiredTest {
    @Test fun shouldBeAbleToDetectEndOfLifetime_whenThereIsNoReference() {
        val expired = expiredFactory(Item())
        gc(100) { expired.alive }
        assertFalse(expired.alive)
    }

    @Test fun shouldBeAbleToDetectThatObjectIsAlive_whenThereIsAtLeastOneReachableReference() {
        val obj = Item()
        val expired = expiredFactory(obj)
        gc(100) { expired.alive }
        assertTrue(expired.alive)
    }
}

fun gc(hints: Int, predicate: () -> Boolean) {
    var count = 0
    while (predicate() && count++ < hints) {
        System.gc()
    }
}

fun <T> expiredFactory(obj: T): Expired<T> = Expired(obj)
internal data class Item(val name: String = "")
