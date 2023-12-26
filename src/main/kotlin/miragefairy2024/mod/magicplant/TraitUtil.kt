package miragefairy2024.mod.magicplant

import miragefairy2024.util.bitCount
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.register
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.max
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.random.Random

// Trait

fun Trait.register(identifier: Identifier) = this.register(traitRegistry, identifier)

fun Trait.getIdentifier() = traitRegistry.getId(this)!!
fun Identifier.toTrait() = traitRegistry.get(this)

fun Trait.getTranslationKey(): String = Util.createTranslationKey("mirageFairy2024.trait", this.getIdentifier())
fun Trait.getName() = run { text { translate(this@run.getTranslationKey()) } }

fun Trait.enJa(enName: String, jaName: String) {
    en { this.getTranslationKey() to enName }
    ja { this.getTranslationKey() to jaName }
}


// TraitEffects

operator fun MutableTraitEffects.plusAssign(other: MutableTraitEffects) {
    other.keys.forEach { key ->
        fun <T : Any> f(key: TraitEffectKey<T>) {
            this[key] = key.plus(this[key], other[key])
        }
        f(key)
    }
}


// TraitEffect

fun <T : Any> TraitEffect<T>.getDescription() = this.key.getDescription(this.value)


// TraitEffectKey

fun TraitEffectKey<*>.register(identifier: Identifier) = this.register(traitEffectKeyRegistry, identifier)

fun TraitEffectKey<*>.getIdentifier() = traitEffectKeyRegistry.getId(this)!!
fun Identifier.toTraitEffectKey() = traitEffectKeyRegistry.get(this)

fun TraitEffectKey<*>.getTranslationKey(): String = Util.createTranslationKey("mirageFairy2024.traitEffect", this.getIdentifier())
fun TraitEffectKey<*>.getName() = run { text { translate(this@run.getTranslationKey()) } }

fun TraitEffectKey<*>.enJa(enName: String, jaName: String) {
    en { this.getTranslationKey() to enName }
    ja { this.getTranslationKey() to jaName }
}


// TraitStacks

val TraitStacks.bitCount get() = this.traitStackList.sumOf { it.level.bitCount }

const val MAX_TRAIT_COUNT = 15

fun crossTraitStacks(random: Random, a: TraitStacks, b: TraitStacks): TraitStacks {

    class Entry(val trait: Trait, val level: Int, val isDecided: Boolean)

    // 両親から、一旦枠数制限を無視して交配する
    val traits = a.traitStackMap.keys + b.traitStackMap.keys
    val entries = traits.map { trait ->
        val aLevel = a.traitStackMap[trait] ?: 0
        val bLevel = b.traitStackMap[trait] ?: 0
        val bits = (aLevel max bLevel).toString(2).length

        var level = 0
        var isDecided = false
        (0 until bits).forEach { bit ->
            val mask = 1 shl bit
            val aPossession = aLevel and mask != 0
            val bPossession = bLevel and mask != 0
            when {
                aPossession && bPossession -> { // 両親所持ビットは必ず継承しつつ、特性も確定特性にする
                    level = level or mask
                    isDecided = true
                }

                !aPossession && !bPossession -> Unit // 両親不所持ビットは継承しない

                else -> { // 片親所持ビットは50%の確率で継承
                    if (random.nextDouble() < 0.5) level = level or mask
                }
            }
        }

        Entry(trait, level, isDecided)
    }

    // 交配の結果全部のビットが消えた特性はリストから外す
    val entries2 = entries.filter { it.level != 0 }

    // 枠数を超えていて不確定特性を持っている限り、不確定特性をランダムに消していく
    val decidedTraitStackList = entries2.filter { it.isDecided }.map { TraitStack(it.trait, it.level) }
    val undecidedTraitStackList = entries2.filter { !it.isDecided }.map { TraitStack(it.trait, it.level) }.toMutableList()
    while (decidedTraitStackList.size + undecidedTraitStackList.size > MAX_TRAIT_COUNT) {
        if (undecidedTraitStackList.isEmpty()) break
        undecidedTraitStackList.removeAt(random.nextInt(undecidedTraitStackList.size))
    }

    return TraitStacks.of(decidedTraitStackList + undecidedTraitStackList)
}
