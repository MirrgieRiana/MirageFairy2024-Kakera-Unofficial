package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.cmp
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

// api

val traitRegistryKey: RegistryKey<Registry<Trait>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "trait"))
val traitRegistry: Registry<Trait> = FabricRegistryBuilder.createSimple(traitRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

abstract class Trait(val style: Style, val poem: Text) : Comparable<Trait> {
    abstract val conditions: List<TraitCondition>
    abstract val primaryEffect: TraitEffectKey<*>
    abstract val effectStacks: List<Pair<TraitEffectKey<*>, Double>>

    /** 呼び出された時点でそこにブロックの実体が存在しない場合があります。 */
    abstract fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects?

    override fun compareTo(other: Trait): Int {
        (this.primaryEffect.sortValue cmp other.primaryEffect.sortValue).let { if (it != 0) return it }
        (this.getIdentifier() cmp other.getIdentifier()).let { if (it != 0) return it }
        return 0
    }
}


// init

context(ModContext)
fun Trait.enJa(enName: String, jaName: String) {
    en { this.getTranslationKey() to enName }
    ja { this.getTranslationKey() to jaName }
}


// util

fun Trait.getIdentifier() = traitRegistry.getId(this)!!
fun Identifier.toTrait() = traitRegistry.get(this)

fun Trait.getTranslationKey(): String = Util.createTranslationKey("mirageFairy2024.trait", this.getIdentifier())
fun Trait.getName() = run { text { translate(this@run.getTranslationKey()) } }
