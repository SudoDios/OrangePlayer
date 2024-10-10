package me.sudodios.orangeplayer.utils

open class EventLiveData<T>(initialValue: T) {
    private var storedValue: T = initialValue
    private val observers = mutableListOf<(T) -> Unit>()

    open var value: T
        get() = storedValue
        set(value) {
            changeValue(value)
        }

    open fun addObserver(observer: (T) -> Unit) {
        observers.add(observer)
    }

    open fun removeObserver(observer: (T) -> Unit) {
        observers.remove(observer)
    }

    private fun changeValue(value: T) {
        storedValue = value

        observers.forEach { it(storedValue) }
    }
}