package miragefairy2024.util

import mirrg.kotlin.hydrogen.castOrNull
import kotlin.reflect.KProperty
import net.minecraft.nbt.ByteTag as NbtByte
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.nbt.DoubleTag as NbtDouble
import net.minecraft.nbt.FloatTag as NbtFloat
import net.minecraft.nbt.IntTag as NbtInt
import net.minecraft.nbt.ListTag as NbtList
import net.minecraft.nbt.LongTag as NbtLong
import net.minecraft.nbt.NumericTag as AbstractNbtNumber
import net.minecraft.nbt.ShortTag as NbtShort
import net.minecraft.nbt.StringTag as NbtString
import net.minecraft.nbt.Tag as NbtElement


// NbtWrapper

/**
 * [NbtElement]のインスタンスを提供するインターフェースです。
 * 参照先の[NbtElement]は実体が未生成である可能性があります。
 * [NbtWrapper]を使って参照先のNbtElementを生成することができます。
 */
interface NbtWrapper<out N : NbtElement> {
    fun getOrNull(): N?
    fun getOrCreate(): N
}

/**
 * 常に生成済みの[NbtElement]を返す[NbtWrapper]を生成します。
 */
val <N : NbtElement> N.wrapper get() = InstanceNbtWrapper(this)

class InstanceNbtWrapper<N : NbtElement>(private val element: N) : NbtWrapper<N>, NbtProperty<N, Nothing> {
    override fun getOrNull() = element
    override fun getOrCreate() = element
    override fun get() = element
    override fun set(value: Nothing) = value
}

/**
 * [NbtCompound]の子要素にアクセスする[NbtProperty]を生成します。
 * この[NbtProperty]はnullをセット可能であり、その場合はそのキーを削除します。
 */
operator fun NbtWrapper<NbtCompound>.get(key: String): NbtProperty<NbtElement?, NbtElement?> {
    val parent = this
    return object : NbtProperty<NbtElement?, NbtElement?> {
        override fun get() = parent.getOrNull()?.get(key)
        override fun set(value: NbtElement?) {
            if (value != null) {
                parent.getOrCreate().put(key, value)
            } else {
                parent.getOrCreate().remove(key)
            }
        }
    }
}

/**
 * [NbtList]の子要素にアクセスする[NbtProperty]を生成します。
 */
operator fun NbtWrapper<NbtList>.get(index: Int): NbtProperty<NbtElement?, NbtElement> {
    val parent = this
    return object : NbtProperty<NbtElement?, NbtElement> {
        override fun get() = parent.getOrNull()?.getOrNull(index)
        override fun set(value: NbtElement) {
            parent.getOrCreate()[index] = value
        }
    }
}


// NbtProperty

/**
 * 親NBT要素の子に対する取得および設定のアクセスを提供するインターフェースです。
 * [NbtProperty]は常に親NBT要素の存在を仮定し、設定アクセスは親NBT要素への改変を引き起こします。
 */
interface NbtProperty<out G, in S> {
    fun get(): G
    fun set(value: S)
}

operator fun <G> NbtProperty<G, *>.getValue(thisRef: Any?, property: KProperty<*>) = this.get()
operator fun <S> NbtProperty<*, S>.setValue(thisRef: Any?, property: KProperty<*>, value: S) = this.set(value)

inline fun <G, S> NbtProperty(crossinline getter: () -> G, crossinline setter: (S) -> Unit) = object : NbtProperty<G, S> {
    override fun get() = getter()
    override fun set(value: S) = setter(value)
}

/**
 * 親[NbtProperty]を[NbtCompound]と仮定し、その子要素への[NbtProperty]を返します。
 * 親NBT要素が[NbtCompound]でなかった場合、取得アクセスではnullが返却され、設定アクセスでは親NBT要素に新しい[NbtCompound]を割り当てます。
 */
operator fun NbtProperty<NbtElement?, NbtElement>.get(key: String): NbtProperty<NbtElement?, NbtElement?> {
    val parent = this
    val nbtWrapper = object : NbtWrapper<NbtCompound> {
        override fun getOrNull() = parent.get() as? NbtCompound
        override fun getOrCreate() = getOrNull() ?: NbtCompound().also { parent.set(it) }
    }
    return nbtWrapper[key]
}

/**
 * 親[NbtProperty]を[NbtList]と仮定し、その子要素への[NbtProperty]を返します。
 * 親NBT要素が[NbtList]でなかった場合、取得アクセスではnullが返却され、設定アクセスでは親NBT要素に新しい[NbtList]を割り当てます。
 */
operator fun NbtProperty<NbtElement?, NbtElement>.get(index: Int): NbtProperty<NbtElement?, NbtElement> {
    val parent = this
    val nbtWrapper = object : NbtWrapper<NbtList> {
        override fun getOrNull() = parent.get() as? NbtList
        override fun getOrCreate() = getOrNull() ?: NbtList().also { parent.set(it) }
    }
    return nbtWrapper[index]
}


// utilities

val NbtProperty<NbtElement?, NbtElement?>.list get() = NbtProperty<NbtList?, NbtList?>({ this.get()?.castOrNull() }, { this.set(it) })
val NbtProperty<NbtElement?, NbtElement?>.compound get() = NbtProperty<NbtCompound?, NbtCompound?>({ this.get()?.castOrNull() }, { this.set(it) })

val NbtProperty<NbtElement?, NbtElement?>.byte get() = NbtProperty<Byte?, Byte?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asByte }, { this.set(it?.let { a -> NbtByte.valueOf(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.short get() = NbtProperty<Short?, Short?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asShort }, { this.set(it?.let { a -> NbtShort.valueOf(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.int get() = NbtProperty<Int?, Int?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asInt }, { this.set(it?.let { a -> NbtInt.valueOf(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.long get() = NbtProperty<Long?, Long?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asLong }, { this.set(it?.let { a -> NbtLong.valueOf(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.float get() = NbtProperty<Float?, Float?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asFloat }, { this.set(it?.let { a -> NbtFloat.valueOf(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.double get() = NbtProperty<Double?, Double?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asDouble }, { this.set(it?.let { a -> NbtDouble.valueOf(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.number get() = NbtProperty<Number?, Number?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asNumber }, { this.set(it?.let { a -> NbtDouble.valueOf(a.toDouble()) }) })
val NbtProperty<NbtElement?, NbtElement?>.string get() = NbtProperty<String?, String?>({ this.get()?.castOrNull<NbtString>()?.asString }, { this.set(it?.toNbtString()) })
val NbtProperty<NbtElement?, NbtElement?>.boolean get() = NbtProperty<Boolean?, Boolean?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.asByte?.let { it != 0.toByte() } }, { this.set(it?.let { a -> NbtByte.valueOf(a) }) })

@get:JvmName("listGetter")
val NbtProperty<NbtElement, Nothing>.list get() = NbtProperty<NbtList?, Nothing>({ this.get().castOrNull() }, {})

@get:JvmName("compoundGetter")
val NbtProperty<NbtElement, Nothing>.compound get() = NbtProperty<NbtCompound?, Nothing>({ this.get().castOrNull() }, {})

@get:JvmName("byteGetter")
val NbtProperty<NbtElement, Nothing>.byte get() = NbtProperty<Byte?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asByte }, {})

@get:JvmName("shortGetter")
val NbtProperty<NbtElement, Nothing>.short get() = NbtProperty<Short?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asShort }, {})

@get:JvmName("intGetter")
val NbtProperty<NbtElement, Nothing>.int get() = NbtProperty<Int?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asInt }, {})

@get:JvmName("longGetter")
val NbtProperty<NbtElement, Nothing>.long get() = NbtProperty<Long?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asLong }, {})

@get:JvmName("floatGetter")
val NbtProperty<NbtElement, Nothing>.float get() = NbtProperty<Float?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asFloat }, {})

@get:JvmName("doubleGetter")
val NbtProperty<NbtElement, Nothing>.double get() = NbtProperty<Double?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asDouble }, {})

@get:JvmName("numberGetter")
val NbtProperty<NbtElement, Nothing>.number get() = NbtProperty<Number?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asNumber }, {})

@get:JvmName("stringGetter")
val NbtProperty<NbtElement, Nothing>.string get() = NbtProperty<String?, Nothing>({ this.get().castOrNull<NbtString>()?.asString }, {})

@get:JvmName("booleanGetter")
val NbtProperty<NbtElement, Nothing>.boolean get() = NbtProperty<Boolean?, Nothing>({ this.get().castOrNull<AbstractNbtNumber>()?.asByte?.let { it != 0.toByte() } }, {})

val NbtProperty<NbtElement?, NbtElement?>.map
    get() = NbtProperty<Map<String, NbtElement>?, Map<String, NbtElement>?>({
        val nbt = this.get()?.castOrNull<NbtCompound>() ?: return@NbtProperty null
        nbt.allKeys.associate { key -> key!! to nbt[key]!! }
    }, {
        this.set(it?.let { a ->
            NbtCompound().also { nbt ->
                a.forEach { entry ->
                    nbt.put(entry.key, entry.value)
                }
            }
        })
    })

fun <T> NbtProperty<T?, T?>.orDefault(getter: () -> T) = object : NbtProperty<T, T> {
    override fun get() = this@orDefault.get() ?: getter()
    override fun set(value: T) = this@orDefault.set(value)
}
