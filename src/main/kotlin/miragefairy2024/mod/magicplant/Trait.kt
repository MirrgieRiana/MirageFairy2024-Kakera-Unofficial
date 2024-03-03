package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.toNbtList
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.cmp
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.SortedMap

// TraitRegistry

val traitRegistryKey: RegistryKey<Registry<Trait>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "trait"))
val traitRegistry: Registry<Trait> = FabricRegistryBuilder.createSimple(traitRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()


// TraitEffectKeyRegistry

val traitEffectKeyRegistryKey: RegistryKey<Registry<TraitEffectKey<*>>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "trait_effect_key"))
val traitEffectKeyRegistry: Registry<TraitEffectKey<*>> = FabricRegistryBuilder.createSimple(traitEffectKeyRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()


// Trait

abstract class Trait(val color: Formatting, private val sortKey: String) : Comparable<Trait> {
    /** 呼び出された時点でそこにブロックの実体が存在しない場合があります。 */
    abstract fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects?
    override fun compareTo(other: Trait): Int {
        (this.sortKey cmp other.sortKey).let { if (it != 0) return it }
        (this.getIdentifier() cmp other.getIdentifier()).let { if (it != 0) return it }
        return 0
    }
}


// TraitEffect

class MutableTraitEffects {
    private val map = mutableMapOf<TraitEffectKey<*>, Any>()

    val keys get() = map.keys

    val effects
        get() = map.entries.map { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            fun <T : Any> a(key: TraitEffectKey<T>, value: Any): TraitEffect<*> = TraitEffect(key, value as T)
            a(key, value)
        }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: TraitEffectKey<T>) = map[key].or { return key.getDefaultValue() } as T
    operator fun <T : Any> set(key: TraitEffectKey<T>, value: T?) {
        if (value == null) {
            map.remove(key)
        } else {
            map[key] = value
        }
    }
}

class TraitEffect<T : Any>(val key: TraitEffectKey<T>, val value: T)

abstract class TraitEffectKey<T : Any> {
    abstract fun getValue(level: Int): T
    abstract fun plus(a: T, b: T): T
    abstract fun getDescription(value: T): Text
    abstract fun getDefaultValue(): T
}


// TraitStack

class TraitStack(val trait: Trait, val level: Int) {
    init {
        require(level >= 1)
    }
}

fun NbtCompound.toTraitStack(): TraitStack? {
    val trait = this.wrapper["Trait"].string.get()?.toIdentifier()?.toTrait() ?: return null
    val level = this.wrapper["Level"].int.get() ?: return null
    if (level < 1) return null
    return TraitStack(trait, level)
}

fun TraitStack.toNbt() = NbtCompound().also {
    it.wrapper["Trait"].string.set(this.trait.getIdentifier().string)
    it.wrapper["Level"].int.set(this.level)
}


// TraitStacks

class TraitStacks private constructor(val traitStackMap: SortedMap<Trait, Int>) {
    companion object {
        fun of(traitStackList: Iterable<TraitStack>): TraitStacks {
            // 同じ特性をまとめて、各レベルをビットORする
            val traitStackMap = traitStackList
                .groupBy { it.trait }
                .mapValues {
                    it.value
                        .map { traitStack -> traitStack.level }
                        .reduce { a, b -> a or b }
                }
                .toSortedMap()
            return TraitStacks(traitStackMap)
        }

        fun of(vararg traitStacks: TraitStack) = of(traitStacks.asIterable())

        fun readFromNbt(parent: NbtCompound, key: String = "TraitStacks"): TraitStacks? {
            if (!parent.contains(key, NbtElement.LIST_TYPE.toInt())) return null
            return parent.getList(key, NbtElement.COMPOUND_TYPE.toInt()).toTraitStacks()
        }
    }

    init {
        traitStackMap.forEach { (_, level) ->
            require(level >= 1)
        }
    }

    val traitStackList by lazy { traitStackMap.map { TraitStack(it.key, it.value) } }
}

fun NbtList.toTraitStacks(): TraitStacks {
    val traitStackList = (0..<this.size).mapNotNull {
        this.getCompound(it).toTraitStack()
    }
    return TraitStacks.of(traitStackList)
}

fun TraitStacks.toNbt() = this.traitStackMap.map { TraitStack(it.key, it.value).toNbt() }.toNbtList()
