package au.prayer.app.ui.navigation

import androidx.compose.runtime.mutableStateListOf

/**
 * A lightweight, reactive Last-In, First-Out (LIFO) back stack for navigation.
 */
class LifoBackStack<T>(initial: T) {
    private val _items = mutableStateListOf<T>(initial)

    val items: List<T>
        get() = _items.toList()

    val current: T
        get() = _items.last()

    val size: Int
        get() = _items.size

    val canPop: Boolean
        get() = _items.size > 1

    fun push(entry: T) {
        _items.add(entry)
    }

    fun pop(): Boolean {
        if (_items.size > 1) {
            _items.removeAt(_items.lastIndex)
            return true
        }
        return false
    }

    fun popToRoot() {
        while (_items.size > 1) {
            _items.removeAt(_items.lastIndex)
        }
    }

    fun replace(entry: T) {
        if (_items.isNotEmpty()) {
            _items[_items.lastIndex] = entry
        } else {
            _items.add(entry)
        }
    }

    fun clearAndSet(entry: T) {
        _items.clear()
        _items.add(entry)
    }
}
