package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier

// api

val traitEffectKeyRegistryKey: RegistryKey<Registry<TraitEffectKey<*>>> = RegistryKey.ofRegistry(MirageFairy2024.identifier("trait_effect_key"))
val traitEffectKeyRegistry: Registry<TraitEffectKey<*>> = FabricRegistryBuilder.createSimple(traitEffectKeyRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

abstract class TraitEffectKey<T : Any> {
    abstract val emoji: Text
    abstract val name: Text
    abstract val sortValue: Double
    abstract val color: Int
    abstract fun getValue(level: Double): T
    abstract fun renderValue(value: T): Text
    abstract fun plus(a: T, b: T): T
    abstract fun getDefaultValue(): T
}


// util

fun TraitEffectKey<*>.getIdentifier() = traitEffectKeyRegistry.getId(this)!!
fun Identifier.toTraitEffectKey() = traitEffectKeyRegistry.get(this)
val TraitEffectKey<*>.style: Style get() = Style.EMPTY.withColor(this.color)
