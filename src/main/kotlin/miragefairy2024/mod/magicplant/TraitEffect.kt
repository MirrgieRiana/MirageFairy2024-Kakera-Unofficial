package miragefairy2024.mod.magicplant

class TraitEffect<T : Any>(val key: TraitEffectKey<T>, val value: T)

fun <T : Any> TraitEffect<T>.getDescription() = this.key.getDescription(this.value)
