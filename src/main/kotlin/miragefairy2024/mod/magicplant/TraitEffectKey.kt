package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey as RegistryKey
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component as Text
import net.minecraft.resources.ResourceLocation as Identifier

// api

val traitEffectKeyRegistryKey: RegistryKey<Registry<TraitEffectKey<*>>> = RegistryKey.createRegistryKey(MirageFairy2024.identifier("trait_effect_key"))
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

fun TraitEffectKey<*>.getIdentifier() = traitEffectKeyRegistry.getKey(this)!!
fun Identifier.toTraitEffectKey() = traitEffectKeyRegistry.get(this)
val TraitEffectKey<*>.style: Style get() = Style.EMPTY.withColor(this.color)
