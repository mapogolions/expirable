package mapogolions.expirable

import mapogolions.expirable.internal.Item
import mapogolions.expirable.internal.expiredFactory
import mapogolions.expirable.internal.gc
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
