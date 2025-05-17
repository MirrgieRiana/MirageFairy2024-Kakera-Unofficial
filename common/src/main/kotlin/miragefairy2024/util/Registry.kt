package miragefairy2024.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import miragefairy2024.ModContext
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

object RegistryEvents {
    val registrations = mutableListOf<Registration<*, *>>()
}

class Registration<T : Any, U : T>(val registry: Registry<T>, val identifier: ResourceLocation, val creator: suspend () -> U) : () -> U {

    private val value = CompletableDeferred<U>()
    private val holder = CompletableDeferred<Holder<T>>()

    fun complete(value: U, holder: Holder<T>) {
        this.value.complete(value)
        this.holder.complete(holder)
    }

    suspend fun await() = value.await()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke() = value.getCompleted()

    suspend fun awaitHolder() = holder.await()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHolder() = holder.getCompleted()

}

context(ModContext)
fun Registration<*, *>.register() {
    RegistryEvents.registrations += this
}

val <T> Registry<T>.sortedEntrySet: List<Map.Entry<ResourceKey<T>, T>> get() = this.entrySet().sortedBy { it.key.location() }

operator fun <T> HolderLookup.Provider.get(registry: ResourceKey<Registry<T>>, key: ResourceKey<T>): Holder.Reference<T> = this.lookupOrThrow(registry).getOrThrow(key)
