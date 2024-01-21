package miragefairy2024.mod.fairy

import miragefairy2024.mod.APPEARANCE_RATE_BONUS_TRANSLATION
import miragefairy2024.mod.MIRAGE_FLOUR_DESCRIPTION_TRANSLATION
import miragefairy2024.util.Chance
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Single
import miragefairy2024.util.blue
import miragefairy2024.util.createItemStack
import miragefairy2024.util.get
import miragefairy2024.util.hasSameItemAndNbt
import miragefairy2024.util.invoke
import miragefairy2024.util.obtain
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.totalWeight
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.cmp
import mirrg.kotlin.hydrogen.floorToInt
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World
import kotlin.math.pow

class RandomFairySummoningItem(val appearanceRateBonus: Double, settings: Settings) : Item(settings) {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip += text { (APPEARANCE_RATE_BONUS_TRANSLATION() + ": x"() + (appearanceRateBonus formatAs "%.3f").replace("""\.?0+$""".toRegex(), "")()).blue }
        tooltip += MIRAGE_FLOUR_DESCRIPTION_TRANSLATION().yellow
    }

    override fun getUseAction(stack: ItemStack) = UseAction.BOW
    override fun getMaxUseTime(stack: ItemStack) = 72000 // 1時間

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        if (!user.isSneaking) {

            // 使用開始
            user.setCurrentHand(hand)

            return TypedActionResult.consume(itemStack)
        } else {
            if (world.isClient) return TypedActionResult.success(itemStack)

            val chanceTable = getCommonChanceTable(user).map { MotifChance(it.motif, it.rate * appearanceRateBonus) }.compressRate().sortedDescending()

            user.openHandledScreen(object : ExtendedScreenHandlerFactory {
                override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity) = MotifTableScreenHandler(syncId, chanceTable)
                override fun getDisplayName() = itemStack.name
                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                    buf.writeInt(chanceTable.size)
                    chanceTable.forEach {
                        buf.writeString(it.motifId.string)
                        buf.writeDouble(it.rate)
                        buf.writeDouble(it.condensation)
                    }
                }
            })
            return TypedActionResult.consume(itemStack)
        }
    }

    override fun usageTick(world: World, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if (world.isClient) return
        if (user !is PlayerEntity) return

        run {
            var t = 72000 - remainingUseTicks

            if (t < 40) {
                return@run
            }
            t -= 40

            if (t < 8 * 4) {
                if (t % 8 == 0) craft(user, stack)
                return@run
            }
            t -= 8 * 4

            if (t < 4 * 8) {
                if (t % 4 == 0) craft(user, stack)
                return@run
            }
            t -= 4 * 8

            if (t < 2 * 16) {
                if (t % 2 == 0) craft(user, stack)
                return@run
            }
            t -= 2 * 16

            craft(user, stack)
        }

        if (stack.isEmpty) user.clearActiveItem()

    }

    private fun craft(player: PlayerEntity, itemStack: ItemStack) {
        val world = player.world

        // 消費
        if (!player.isCreative) {
            if (itemStack.count != 1) {
                // 最後の1個でない場合

                // 普通に消費
                itemStack.decrement(1)

            } else {
                // 最後の1個の場合

                // リロードが可能ならリロードする
                val isReloaded = run {
                    (0 until player.inventory.size).forEach { index ->
                        val searchingItemStack = player.inventory[index]
                        if (searchingItemStack !== itemStack) { // 同一のアイテムスタックでなく、
                            if (searchingItemStack hasSameItemAndNbt itemStack) { // 両者が同一種類のアイテムスタックならば、
                                val count = searchingItemStack.count
                                player.inventory[index] = EMPTY_ITEM_STACK // そのアイテムスタックを消して
                                itemStack.count = count // 手に持っているアイテムスタックに移動する
                                // stack.count == 1なので、このときアイテムが1個消費される
                                return@run true
                            }
                        }
                    }
                    false
                }

                // リロードできなかった場合、最後の1個を減らす
                if (!isReloaded) itemStack.decrement(1)

            }
        }

        // 提供割合の生成
        val chanceTable: MutableList<Chance<Single<CondensedMotifChance?>>> = getCommonChanceTable(player).map { MotifChance(it.motif, it.rate * appearanceRateBonus) }.compressRate().map { Chance(it.rate, Single(it)) }.toMutableList()
        val totalWeight = chanceTable.totalWeight
        if (totalWeight < 1.0) chanceTable += Chance(1.0 - totalWeight, Single(null))

        // ガチャ
        val condensedMotif = chanceTable.weightedRandom(world.random)?.first ?: return

        // actualCondensation は condensation を超えない最大の2の整数乗
        // condensation = 5.86 の場合、 actualCondensation = 4
        val actualCondensation = getActualCondensation(condensedMotif.condensation)

        // lowerRate の確率で凝縮数 actualCondensation の妖精が1体
        // (1 - lowerRate) の確率で凝縮数 actualCondensation の妖精が2体
        // その期待値は、actualCondensation * (2 - lowerRate) = condensation
        // このとき、 lowerRate = 2 - condensation / actualCondensation であればよい
        val lowerRate = 2 - condensedMotif.condensation / actualCondensation

        val resultItemStack = if (world.random.nextDouble() < lowerRate) {
            FairyCard.item.createItemStack(1).also {
                it.setFairyMotifId(condensedMotif.motifId)
                it.setFairyCondensation(actualCondensation)
            }
        } else {
            FairyCard.item.createItemStack(2).also {
                it.setFairyMotifId(condensedMotif.motifId)
                it.setFairyCondensation(actualCondensation)
            }
        }

        // 入手
        player.obtain(resultItemStack)

        // TODO 妖精召喚履歴に追加

        // エフェクト
        world.playSound(null, player.x, player.y, player.z, SoundEvents.BLOCK_DEEPSLATE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F)

    }
}

class MotifChance(val motif: Identifier, val rate: Double)

fun getCommonChanceTable(player: PlayerEntity): List<MotifChance> {
    val biome = player.world.getBiome(player.blockPos)
    return COMMON_MOTIF_RECIPES.filter { it.biome == null || biome.isIn(it.biome) }.map { recipe ->
        MotifChance(recipe.motif.getIdentifier()!!, 0.1.pow(recipe.motif.rare / 2.0))
    }
}

class CondensedMotifChance(val motifId: Identifier, val rate: Double, val condensation: Double) : Comparable<CondensedMotifChance> {
    override fun compareTo(other: CondensedMotifChance): Int {
        (rate cmp other.rate).let { if (it != 0) return it }
        (condensation cmp other.condensation).let { if (it != 0) return it }
        (motifId cmp other.motifId).let { if (it != 0) return it }
        return 0
    }
}

/** 出現率の合計が最大100%になるように出現率の高いものから出現率を切り詰め、失われた出現率を凝縮数に還元します */
fun List<MotifChance>.compressRate(): List<CondensedMotifChance> {
    val sortedMotifChanceList = this.sortedByDescending { it.rate } // 確率が大きいものから順に並んでいる
    val condensedMotifChanceList = mutableListOf<CondensedMotifChance>()

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

    var rateOfLastEntry = 0.0
    var rateOfConsumedEntries = 0.0
    var currentIndex = sortedMotifChanceList.size - 1
    while (currentIndex >= 0) {
        val countOfRemainingEntries = currentIndex + 1
        val currentEntry = sortedMotifChanceList[currentIndex]
        val rateOfCurrentEntry = currentEntry.rate
        val additionalRateOfCurrentEntry = rateOfCurrentEntry - rateOfLastEntry
        val additionalRateOfAllRemainingEntries = additionalRateOfCurrentEntry * countOfRemainingEntries
        val estimatedRateOfNextConsumedEntries = rateOfConsumedEntries + additionalRateOfAllRemainingEntries
        if (estimatedRateOfNextConsumedEntries > 1) { // 現在のエントリーをそのまま受理すると確率が溢れる
            // 利用可能な確率を残りのすべてのエントリーで分配

            val usableRateOfAllRemainingEntries = 1.0 - rateOfConsumedEntries
            val usableRatePerRemainingEntry = usableRateOfAllRemainingEntries / countOfRemainingEntries
            val actualRatePerRemainingEntry = rateOfLastEntry + usableRatePerRemainingEntry

            (currentIndex downTo 0).forEach { index ->
                val entry = sortedMotifChanceList[index]
                condensedMotifChanceList += CondensedMotifChance(entry.motif, actualRatePerRemainingEntry, entry.rate / actualRatePerRemainingEntry)
            }

            break
        } else { // 現在のエントリーをそのまま受理出来る
            condensedMotifChanceList += CondensedMotifChance(currentEntry.motif, currentEntry.rate, 1.0)
            rateOfLastEntry = rateOfCurrentEntry
            rateOfConsumedEntries = estimatedRateOfNextConsumedEntries
        }
        currentIndex--
    }

    return condensedMotifChanceList
}

private fun getActualCondensation(value: Double): Int {
    val i = value.floorToInt()
    if (i < 1) return 1

    var t = 1
    while (true) {
        val nextT = t shl 1
        if (nextT < 0) return t // overflow
        if (i < nextT) return t
        t = nextT
    }
}
