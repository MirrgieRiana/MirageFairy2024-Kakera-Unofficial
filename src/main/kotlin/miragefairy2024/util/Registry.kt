package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

context(ModContext)
fun <T : Any> T.register(registry: Registry<T>, identifier: ResourceLocation): () -> Holder<T> {
    lateinit var holder: Holder<T>
    ModEvents.onRegistration {
        holder = Registry.registerForHolder(registry, identifier, this@register)
    }
    return { holder }
}

fun <T : Any> T.registerStatic(registry: Registry<T>, identifier: ResourceLocation): Holder<T> {
    return Registry.registerForHolder(registry, identifier, this)
}

val <T> Registry<T>.sortedEntrySet: List<Map.Entry<ResourceKey<T>, T>> get() = this.entrySet().sortedBy { it.key.location() }

operator fun <T> HolderLookup.Provider.get(registry: ResourceKey<Registry<T>>, key: ResourceKey<T>): Holder.Reference<T> = this.lookupOrThrow(registry).getOrThrow(key)
