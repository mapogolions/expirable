package mapogolons.expirable

import java.lang.ref.WeakReference

class Expired<T>(item: T) {
    private val weakRef: WeakReference<T> = WeakReference(item)
    val alive get() = weakRef.enqueue()
}


