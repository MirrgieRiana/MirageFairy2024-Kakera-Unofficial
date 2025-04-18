package miragefairy2024.util

import net.minecraft.util.RandomSource as Random
import kotlin.math.floor

/** 期待値がdになるように整数の乱数を生成します。 */
fun Random.randomInt(d: Double): Int {
    val i = floor(d).toInt()
    val mod = d - i
    return if (this.nextDouble() < mod) i + 1 else i
}

fun Random.randomBoolean(maxRate: Int, rate: Int): Boolean {
    if (rate >= maxRate) return true
    if (rate <= 0) return false
    return this.nextInt(maxRate) < rate
}

val Int.bitCount: Int
    get() {
        var b = 0
        var a = this
        while (a != 0) {
            if (a and 0x1 != 0) b++
            a = a ushr 1
        }
        return b
    }
