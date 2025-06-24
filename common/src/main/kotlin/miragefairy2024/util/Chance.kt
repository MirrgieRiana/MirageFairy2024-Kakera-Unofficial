package miragefairy2024.util

import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.util.RandomSource as Random

class Chance<out T : Any>(val weight: Double, val item: T) {
    override fun toString() = "${weight formatAs "%8.4f"}: $item"
}

class CondensedItem<out T : Any>(val count: Double, val item: T) {
    override fun toString() = "$item x ${count formatAs "%.2f"}"
}

infix fun <T : Any> Double.chanceTo(item: T) = Chance(this, item)


val List<Chance<*>>.totalWeight get() = this.sumOf { it.weight }

fun <T : Any> List<Chance<T>>.filled(creator: () -> T): List<Chance<T>> {
    val totalWeight = this.totalWeight
    return if (totalWeight < 0.9999) this + Chance(1.0 - totalWeight, creator()) else this
}


/** @param randomValue 0以上1未満の値 */
fun <T : Any> List<Chance<T>>.weightedRandom(randomValue: Double): T? {
    if (this.isEmpty()) return null

    var w = randomValue * totalWeight // 0 <= w < totalWeight
    this.forEach { item ->
        w -= item.weight
        if (w < 0) return item.item
    }
    return this.last().item
}

fun <T : Any> List<Chance<T>>.weightedRandom(random: Random) = weightedRandom(random.nextDouble())


/** 同一キーのエントリの重みを加算することによってキーをユニークにします。 */
fun <T : Any> List<Chance<T>>.distinct(equals: (T, T) -> Boolean): List<Chance<T>> {
    class Slot(val item: T) {
        override fun hashCode() = 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true // 相手が自分自身なら一致
            if (other == null) return false // 相手が無なら不一致

            // 型チェック
            if (javaClass != other.javaClass) return false
            @Suppress("UNCHECKED_CAST")
            other as Slot

            return equals(item, other.item)
        }
    }

    val map = mutableMapOf<Slot, Double>()
    this.forEach { item ->
        val slot = Slot(item.item)
        map[slot] = (map[slot] ?: 0.0) + item.weight
    }
    return map.entries.map { Chance(it.value, it.key.item) }
}


/** 出現率の合計が最大100%になるように出現率の高いものから出現率を切り詰め、失われた出現率を凝縮数に還元します */
fun <T : Any> Iterable<Chance<T>>.compressWeight(): List<Chance<CondensedItem<T>>> {
    val sortedChanceList = this.sortedByDescending { it.weight } // 確率が大きいものから順に並んでいる
    val condensedChanceList = mutableListOf<Chance<CondensedItem<T>>>() // 出力用リスト

    // エントリーを確率の高い順に左から右に並べる
    // 希少なものはそのままの確率で、確率が溢れた場合、残りの確率をその時点で残っているすべてのエントリーで等分する
    //
    // ↑確率
    // │*
    // │***
    // │#####
    // │##########
    // │###############
    // └─────────────→エントリーindex
    // グラフの下の方から30個まで#塗りつぶすことができる
    // 以下がその30個を選ぶアルゴリズム
    // 確率が大きすぎるエントリは潰され、確率が小さいエントリはそのまま残る
    // 実際には一番上の#は半端なところで削れる（下記における確率の分配）

    var weightOfLastEntry = 0.0 // 現在の全体で受理済みの1個当たりの確率
    var weightOfConsumedEntries = 0.0 // 現在の全体で受理済みの確率
    var currentIndex = sortedChanceList.size - 1
    while (currentIndex >= 0) { // 確率が小さいものから順にイテレートする
        val countOfRemainingEntries = currentIndex + 1 // 現在のエントリーも含む残りのエントリー数
        val currentEntry = sortedChanceList[currentIndex] // 現在のエントリー
        val weightOfCurrentEntry = currentEntry.weight // 現在のエントリーの確率
        val additionalWeightOfCurrentEntry = weightOfCurrentEntry - weightOfLastEntry // 現在のエントリーをそのまま受理することで生じる1個当たりの確率の増分
        val additionalWeightOfAllRemainingEntries = additionalWeightOfCurrentEntry * countOfRemainingEntries // 現在のエントリーをそのまま受理することで生じる全体の確率の増分
        val estimatedWeightOfNextConsumedEntries = weightOfConsumedEntries + additionalWeightOfAllRemainingEntries // 現在のエントリーをそのまま受理する場合の全体の確率
        if (estimatedWeightOfNextConsumedEntries > 1) { // 現在のエントリーをそのまま受理すると確率が溢れる
            // 利用可能な確率を残りのすべてのエントリーで分配

            val usableWeightOfAllRemainingEntries = 1.0 - weightOfConsumedEntries // 利用可能な全体の残りの確率
            val usableWeightPerRemainingEntry = usableWeightOfAllRemainingEntries / countOfRemainingEntries // 残りのエントリーで利用可能な1個当たりの確率
            val actualWeightPerRemainingEntry = weightOfLastEntry + usableWeightPerRemainingEntry // 残りの各エントリーに割り当てられる実際の確率

            (currentIndex downTo 0).forEach { index -> // 残りのエントリーを希少な順に排出
                val entry = sortedChanceList[index]
                condensedChanceList += Chance(actualWeightPerRemainingEntry, CondensedItem(entry.weight / actualWeightPerRemainingEntry, entry.item))
            }

            break
        } else { // 現在のエントリーをそのまま受理出来る
            condensedChanceList += Chance(currentEntry.weight, CondensedItem(1.0, currentEntry.item)) // 排出
            weightOfLastEntry = weightOfCurrentEntry // 現在の全体で受理済みの1個当たりの確率を更新
            weightOfConsumedEntries = estimatedWeightOfNextConsumedEntries // 現在の全体で受理済みの確率を更新
        }
        currentIndex--
    }

    return condensedChanceList
}
