package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.en
import miragefairy2024.util.ja
import miragefairy2024.util.text
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.Util

// api

val traitEffectKeyRegistryKey: RegistryKey<Registry<TraitEffectKey<*>>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "trait_effect_key"))
val traitEffectKeyRegistry: Registry<TraitEffectKey<*>> = FabricRegistryBuilder.createSimple(traitEffectKeyRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

abstract class TraitEffectKey<T : Any> {
    abstract fun getValue(level: Int): T
    abstract fun plus(a: T, b: T): T
    abstract fun getDescription(value: T): Text
    abstract fun getDefaultValue(): T
}


// init

context(ModContext)
fun TraitEffectKey<*>.enJa(enName: String, jaName: String) {
    en { this.getTranslationKey() to enName }
    ja { this.getTranslationKey() to jaName }
}


// util

fun TraitEffectKey<*>.getIdentifier() = traitEffectKeyRegistry.getId(this)!!
fun Identifier.toTraitEffectKey() = traitEffectKeyRegistry.get(this)

fun TraitEffectKey<*>.getTranslationKey(): String = Util.createTranslationKey("mirageFairy2024.traitEffect", this.getIdentifier())
fun TraitEffectKey<*>.getName() = text { translate(this@getName.getTranslationKey()) }
