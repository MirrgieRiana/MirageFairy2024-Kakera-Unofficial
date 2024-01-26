package miragefairy2024.util

import mirrg.kotlin.hydrogen.castOrNull
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLong
import net.minecraft.nbt.NbtShort
import net.minecraft.nbt.NbtString
import kotlin.reflect.KProperty


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
val <N : NbtElement> N.wrapper: NbtWrapper<N>
    get() {
        val nbt = this
        return object : NbtWrapper<N> {
            override fun getOrNull() = nbt
            override fun getOrCreate() = nbt
        }
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

val NbtProperty<NbtElement?, NbtElement?>.byte get() = NbtProperty<Byte?, Byte?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.byteValue() }, { this.set(it?.let { a -> NbtByte.of(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.short get() = NbtProperty<Short?, Short?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.shortValue() }, { this.set(it?.let { a -> NbtShort.of(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.int get() = NbtProperty<Int?, Int?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.intValue() }, { this.set(it?.let { a -> NbtInt.of(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.long get() = NbtProperty<Long?, Long?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.longValue() }, { this.set(it?.let { a -> NbtLong.of(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.float get() = NbtProperty<Float?, Float?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.floatValue() }, { this.set(it?.let { a -> NbtFloat.of(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.double get() = NbtProperty<Double?, Double?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.doubleValue() }, { this.set(it?.let { a -> NbtDouble.of(a) }) })
val NbtProperty<NbtElement?, NbtElement?>.number get() = NbtProperty<Number?, Number?>({ this.get()?.castOrNull<AbstractNbtNumber>()?.numberValue() }, { this.set(it?.let { a -> NbtDouble.of(a.toDouble()) }) })
val NbtProperty<NbtElement?, NbtElement?>.string get() = NbtProperty<String?, String?>({ this.get()?.asString() }, { this.set(it?.let { a -> NbtString.of(a) }) })

val NbtProperty<NbtElement?, NbtElement?>.map
    get() = NbtProperty<Map<String, NbtElement>?, Map<String, NbtElement>?>({
        val nbt = this.get()?.castOrNull<NbtCompound>() ?: return@NbtProperty null
        nbt.keys.associate { key -> key!! to nbt[key]!! }
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
