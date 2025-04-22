package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

context(ModContext)
fun <T> T.register(registry: Registry<T>, identifier: ResourceLocation) {
    ModEvents.onRegistration {
        Registry.register(registry, identifier, this@register)
    }
}

val <T> Registry<T>.sortedEntrySet: List<Map.Entry<ResourceKey<T>, T>> get() = this.entrySet().sortedBy { it.key.location() }

operator fun <T> HolderLookup.Provider.get(registry: ResourceKey<Registry<T>>, key: ResourceKey<T>): Holder.Reference<T> = this.lookupOrThrow(registry).getOrThrow(key)
