package mapogolions.expirable.internal

import mapogolions.expirable.Expired

internal fun bruteForceGc(predicate: () -> Boolean) {
    while (predicate()) {
        System.gc()
    }
}

internal fun gc(hints: Long, predicate: () -> Boolean) {
    var count: Long = 0
    while (predicate() && count++ < hints) {
        System.gc()
    }
}

internal fun <T> expiredFactory(obj: T): Expired<T> = Expired(obj)
internal class Item(val name: String = "")

internal fun itemFactory(name: String): Item = Item(name)
