package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.cmp
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey as RegistryKey
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component as Text
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level as World

// api

val traitRegistryKey: RegistryKey<Registry<Trait>> = RegistryKey.createRegistryKey(MirageFairy2024.identifier("trait"))
val traitRegistry: Registry<Trait> = FabricRegistryBuilder.createSimple(traitRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

abstract class Trait(val style: Style, val poem: Text) : Comparable<Trait> {
    abstract val spawnSpecs: List<TraitSpawnSpec>

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

fun Trait.getIdentifier() = traitRegistry.getKey(this)!!
fun Identifier.toTrait() = traitRegistry.get(this)

fun Trait.getTranslationKey(): String = Util.makeDescriptionId("${MirageFairy2024.MOD_ID}.trait", this.getIdentifier())
fun Trait.getName() = run { text { translate(this@run.getTranslationKey()) } }

val Trait.texture get() = "textures/gui/traits/" * this.getIdentifier() * ".png"
