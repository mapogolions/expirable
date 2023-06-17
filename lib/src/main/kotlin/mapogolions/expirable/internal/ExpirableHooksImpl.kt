package mapogolions.expirable.internal

import mapogolions.expirable.Expirable
import mapogolions.expirable.ExpirableCollection
import mapogolions.expirable.Expired

internal open class ExpirableHooksImpl<K : Any, T> : ExpirableHooks<K, T> {
    override fun beforeCleanup(expirables: ExpirableCollection<K, T>) = Unit
    override fun afterCleanup(expirables: ExpirableCollection<K, T>) = Unit
    override fun onExpire(expirable: Expirable<K, T>) = Unit
    override fun onDequeue(expired: Expired<T>) = Unit
}