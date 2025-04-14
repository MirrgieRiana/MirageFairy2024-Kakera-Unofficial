package miragefairy2024.util

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey as RegistryKey
import net.minecraft.resources.ResourceLocation as Identifier

val Identifier.string get() = this.toString()

fun String.toIdentifier() = Identifier(this)

operator fun String.times(identifier: Identifier) = Identifier(identifier.namespace, this + identifier.path)
operator fun Identifier.times(string: String) = Identifier(this.namespace, this.path + string)

infix fun <T> RegistryKey<out Registry<T>>.with(value: Identifier): RegistryKey<T> = RegistryKey.create(this, value)
