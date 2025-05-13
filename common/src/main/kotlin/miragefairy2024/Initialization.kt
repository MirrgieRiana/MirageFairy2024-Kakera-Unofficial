package miragefairy2024

class ModContext

class InitializationEventRegistry<T> {
    private val list = mutableListOf<T>()
    private var closed = false

    context(ModContext)
    operator fun invoke(listener: T) {
        require(!closed)
        list += listener
    }

    fun fire(processor: (T) -> Unit) {
        closed = true
        list.forEach {
            processor(it)
        }
        list.clear()
    }
}

object Modules {
    private val lock = Any()
    private var initialized = false
    context(ModContext)
    fun init() {
        synchronized(lock) {
            if (initialized) return
            initialized = true
            initModules()
        }
    }
}
