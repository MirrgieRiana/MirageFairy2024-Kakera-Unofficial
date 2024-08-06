package miragefairy2024.mod.magicplant

// api

class TraitEffect<T : Any>(val key: TraitEffectKey<T>, val value: T)


// util

fun <T : Any> TraitEffect<T>.getDescription() = this.key.getDescription(this.value)
