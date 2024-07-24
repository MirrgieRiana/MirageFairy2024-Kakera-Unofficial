package miragefairy2024.util

import net.minecraft.util.Identifier

val Identifier.string get() = this.toString()

fun String.toIdentifier() = Identifier(this)

operator fun String.times(identifier: Identifier) = Identifier(identifier.namespace, this + identifier.path)
operator fun Identifier.times(string: String) = Identifier(this.namespace, this.path + string)
