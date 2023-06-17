package mapogolions.expirable.internal

import mapogolions.expirable.Expirable
import mapogolions.expirable.ExpirableCollection
import mapogolions.expirable.Expired

internal interface ExpirableHooks<K : Any, T> {
    fun beforeCleanup(expirables: ExpirableCollection<K, T>)
    fun afterCleanup(expirables: ExpirableCollection<K, T>)
    fun onExpire(expirable: Expirable<K, T>)
    fun onDequeue(expired: Expired<T>)
}

