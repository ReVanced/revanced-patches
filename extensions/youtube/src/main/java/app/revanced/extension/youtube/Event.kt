package app.revanced.extension.youtube

import app.revanced.extension.shared.Logger
import java.util.Collections

/**
 * generic event provider class
 */
class Event<T> {
    private val eventListeners = Collections.synchronizedSet(mutableSetOf<(T) -> Unit>())

    operator fun plusAssign(observer: (T) -> Unit) {
        addObserver(observer)
    }

    fun addObserver(observer: (T) -> Unit) {
        Logger.printDebug { "Adding observer: $observer" }
        eventListeners.add(observer)
    }

    operator fun minusAssign(observer: (T) -> Unit) {
        removeObserver(observer)
    }

    fun removeObserver(observer: (T) -> Unit) {
        eventListeners.remove(observer)
    }

    operator fun invoke(value: T) {
        for (observer in eventListeners) {
            observer.invoke(value)
        }
    }
}
