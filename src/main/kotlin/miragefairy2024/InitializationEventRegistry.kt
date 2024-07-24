package miragefairy2024

class ModContext

class InitializationEventRegistry<T> {
    private val list = mutableListOf<T>()
    private var closed = false

    context(ModContext)
    operator fun invoke(listener: T) {
        require(!closed)
        this.list += listener
    }

    fun fire(processor: (T) -> Unit) {
        closed = true
        this.list.forEach {
            processor(it)
        }
    }
}
