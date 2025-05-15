package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.APPEARANCE_RATE_BONUS_TRANSLATION
import miragefairy2024.util.Chance
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Translation
import miragefairy2024.util.blue
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.hasSameItemAndComponents
import miragefairy2024.util.invoke
import miragefairy2024.util.mutate
import miragefairy2024.util.obtain
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.text
import miragefairy2024.util.totalWeight
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.Single
import mirrg.kotlin.hydrogen.cmp
import mirrg.kotlin.hydrogen.floorToInt
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import kotlin.math.pow
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.sounds.SoundSource as SoundCategory
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.UseAnim as UseAction

private val identifier = MirageFairy2024.identifier("mirage_flour")
val MIRAGE_FLOUR_DESCRIPTION_USE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.description.use" }, "Use and hold to summon fairies", "使用時、長押しで妖精を連続召喚")
val MIRAGE_FLOUR_DESCRIPTION_SNEAKING_USE_TRANSLATION = Translation({ "item.${identifier.toLanguageKey()}.description.sneaking_use" }, "Use while sneaking to show loot table", "スニーク中に使用時、提供割合を表示")

context(ModContext)
fun initRandomFairySummoning() {
    MIRAGE_FLOUR_DESCRIPTION_USE_TRANSLATION.enJa()
    MIRAGE_FLOUR_DESCRIPTION_SNEAKING_USE_TRANSLATION.enJa()
}

class RandomFairySummoningItem(val appearanceRateBonus: Double, settings: Properties) : Item(settings) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        tooltipComponents += text { (APPEARANCE_RATE_BONUS_TRANSLATION() + ": x"() + (appearanceRateBonus formatAs "%.3f").replace("""\.?0+$""".toRegex(), "")()).blue }
        tooltipComponents += text { MIRAGE_FLOUR_DESCRIPTION_USE_TRANSLATION().yellow }
        tooltipComponents += text { MIRAGE_FLOUR_DESCRIPTION_SNEAKING_USE_TRANSLATION().yellow }
    }

    override fun getUseAnimation(stack: ItemStack) = UseAction.BOW
    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 72000 // 1時間

    override fun use(world: Level, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getItemInHand(hand)
        if (!user.isShiftKeyDown) {

            // 使用開始
            user.startUsingItem(hand)

            return TypedActionResult.consume(itemStack)
        } else {
            if (world.isClientSide) return TypedActionResult.success(itemStack)

            val motifSet: Set<Motif> = getCommonMotifSet(user) + user.fairyDreamContainer.getOrCreate().entries
            val chanceTable = motifSet.toChanceTable(appearanceRateBonus).compressRate().sortedDescending()

            user.openMenu(object : ExtendedScreenHandlerFactory<List<CondensedMotifChance>> {
                override fun createMenu(syncId: Int, playerInventory: Inventory, player: PlayerEntity) = MotifTableScreenHandler(syncId, chanceTable)
                override fun getDisplayName() = itemStack.hoverName
                override fun getScreenOpeningData(player: ServerPlayer) = chanceTable
            })
            return TypedActionResult.consume(itemStack)
        }
    }

    override fun onUseTick(world: Level, user: LivingEntity, stack: ItemStack, remainingUseTicks: Int) {
        if (world.isClientSide) return
        if (user !is ServerPlayerEntity) return

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

        if (stack.isEmpty) user.stopUsingItem()

    }

    private fun craft(player: ServerPlayerEntity, itemStack: ItemStack) {
        val world = player.level()

        // 消費
        if (!player.isCreative) {
            if (itemStack.count != 1) {
                // 最後の1個でない場合

                // 普通に消費
                itemStack.shrink(1)

            } else {
                // 最後の1個の場合

                // リロードが可能ならリロードする
                val isReloaded = run {
                    (0 until player.inventory.size).forEach { index ->
                        val searchingItemStack = player.inventory[index]
                        if (searchingItemStack !== itemStack) { // 同一のアイテムスタックでなく、
                            if (searchingItemStack hasSameItemAndComponents itemStack) { // 両者が同一種類のアイテムスタックならば、
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
                if (!isReloaded) itemStack.shrink(1)

            }
        }

        // モチーフの判定
        val motifSet: Set<Motif> = getCommonMotifSet(player) + player.fairyDreamContainer.getOrCreate().entries

        // 抽選
        val result = getRandomFairy(world.random, motifSet, appearanceRateBonus) ?: return

        // 入手
        player.obtain(result.motif.createFairyItemStack(condensation = result.condensation, count = result.count))

        // 妖精召喚履歴に追加
        player.fairyHistoryContainer.mutate { it[result.motif] += result.condensation * result.count }

        // エフェクト
        world.playSound(null, player.x, player.y, player.z, SoundEvents.DEEPSLATE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F)

    }
}

class RandomFairyResult(val motif: Motif, val condensation: Int, val count: Int)

fun getRandomFairy(random: Random, motifSet: Set<Motif>, appearanceRateBonus: Double): RandomFairyResult? {

    // 提供割合の生成
    val chanceTable: MutableList<Chance<Single<CondensedMotifChance?>>> = motifSet.toChanceTable(appearanceRateBonus).compressRate().map { Chance(it.rate, Single(it)) }.toMutableList()
    val totalWeight = chanceTable.totalWeight
    if (totalWeight < 1.0) chanceTable += Chance(1.0 - totalWeight, Single(null))

    // ガチャ
    val condensedMotif = chanceTable.weightedRandom(random)?.first ?: return null

    // actualCondensation は condensation を超えない最大の3の整数乗
    // condensation = 11.46 の場合、 actualCondensation = 9
    val actualCondensation = getNiceCondensation(condensedMotif.count).second

    // 上の場合、 count ≒ 1.27
    val count = condensedMotif.count / actualCondensation
    val actualCount = random.randomInt(count)

    return RandomFairyResult(
        motif = condensedMotif.motif,
        condensation = actualCondensation,
        count = actualCount,
    )
}

fun getCommonMotifSet(player: PlayerEntity): Set<Motif> {
    val biome = player.level().getBiome(player.blockPosition())
    return COMMON_MOTIF_RECIPES.filter {
        when (it) {
            is AlwaysCommonMotifRecipe -> true
            is BiomeCommonMotifRecipe -> biome.`is`(it.biome)
            is BiomeTagCommonMotifRecipe -> biome.`is`(it.biomeTag)
        }
    }.map { it.motif }.toSet()
}

class MotifChance(val motif: Motif, val rate: Double)

fun Iterable<Motif>.toChanceTable(amplifier: Double = 1.0) = this.map { MotifChance(it, (1.0 / 3.0).pow(it.rare - 1) * amplifier) } // 通常花粉・レア度1で100%になる

class CondensedMotifChance(val showingItemStack: ItemStack, val motif: Motif, val rate: Double, val count: Double) : Comparable<CondensedMotifChance> {
    override fun compareTo(other: CondensedMotifChance): Int {
        (rate cmp other.rate).let { if (it != 0) return it }
        (count cmp other.count).let { if (it != 0) return it }
        (motif.getIdentifier()!! cmp other.motif.getIdentifier()!!).let { if (it != 0) return it }
        return 0
    }
}

/** 出現率の合計が最大100%になるように出現率の高いものから出現率を切り詰め、失われた出現率を凝縮数に還元します */
fun Iterable<MotifChance>.compressRate(): List<CondensedMotifChance> {
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
                condensedMotifChanceList += CondensedMotifChance(entry.motif.createFairyItemStack(), entry.motif, actualRatePerRemainingEntry, entry.rate / actualRatePerRemainingEntry)
            }

            break
        } else { // 現在のエントリーをそのまま受理出来る
            condensedMotifChanceList += CondensedMotifChance(currentEntry.motif.createFairyItemStack(), currentEntry.motif, currentEntry.rate, 1.0)
            rateOfLastEntry = rateOfCurrentEntry
            rateOfConsumedEntries = estimatedRateOfNextConsumedEntries
        }
        currentIndex--
    }

    return condensedMotifChanceList
}

fun getNiceCondensation(value: Double) = getNiceCondensation(value.floorToInt())

/** (power, niceCondensation) */
fun getNiceCondensation(value: Int): Pair<Int, Int> {
    if (value < 1) return Pair(0, 1)

    var power = 0
    var t = 1L
    while (true) {
        val nextT = t * 3L
        if (nextT > Integer.MAX_VALUE) return Pair(power, t.toInt()) // overflow
        if (nextT > value) return Pair(power, t.toInt())
        power++
        t = nextT
    }
}
