package mapogolions.expirable.internal

import mapogolions.expirable.Expired

internal fun gc(hints: Int, predicate: () -> Boolean) {
    var count = 0
    while (predicate() && count++ < hints) {
        System.gc()
    }
}

internal fun <T> expiredFactory(obj: T): Expired<T> = Expired(obj)
internal class Item(val name: String = "")

internal fun itemFactory(name: String): Item = Item(name)
