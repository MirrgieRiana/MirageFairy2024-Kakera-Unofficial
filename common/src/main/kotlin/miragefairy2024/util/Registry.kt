package miragefairy2024.util

import miragefairy2024.ModContext
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

object RegistryEvents {
    val registrations = mutableListOf<Registration<*, *>>()
}

class Registration<T : Any, U : T>(val registry: Registry<T>, val identifier: ResourceLocation, val creator: () -> U) : () -> T {
    lateinit var value: U
    lateinit var holder: Holder<T>
    override fun invoke(): U = value
}

context(ModContext)
fun Registration<*, *>.register() {
    RegistryEvents.registrations += this
}

context(ModContext)
fun <T : Any> Registry<T>.register(identifier: ResourceLocation, creator: () -> T) {
    RegistryEvents.registrations += Registration(this, identifier, creator)
}

val <T> Registry<T>.sortedEntrySet: List<Map.Entry<ResourceKey<T>, T>> get() = this.entrySet().sortedBy { it.key.location() }

operator fun <T> HolderLookup.Provider.get(registry: ResourceKey<Registry<T>>, key: ResourceKey<T>): Holder.Reference<T> = this.lookupOrThrow(registry).getOrThrow(key)
