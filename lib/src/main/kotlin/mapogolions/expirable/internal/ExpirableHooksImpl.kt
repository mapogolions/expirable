package mapogolions.expirable.internal

import mapogolions.expirable.Expirable
import mapogolions.expirable.ExpirableCollection
import mapogolions.expirable.Expired

internal open class ExpirableHooksImpl<K : Any, T> : ExpirableHooks<K, T> {
    open override fun beforeCleanup(expirables: ExpirableCollection<K, T>) = Unit
    open override fun afterCleanup(expirables: ExpirableCollection<K, T>) = Unit
    open override fun onExpire(expirable: Expirable<K, T>) = Unit
    open override fun onDequeue(expired: Expired<T>) = Unit
}