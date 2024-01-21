package miragefairy2024.mod.fairy

import miragefairy2024.mod.APPEARANCE_RATE_BONUS_TRANSLATION
import miragefairy2024.mod.MIRAGE_FLOUR_DESCRIPTION_TRANSLATION
import miragefairy2024.util.blue
import miragefairy2024.util.invoke
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.cmp
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import kotlin.math.pow

class RandomFairySummoningItem(val appearanceRateBonus: Double, settings: Settings) : Item(settings) {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip += text { (APPEARANCE_RATE_BONUS_TRANSLATION() + ": x"() + (appearanceRateBonus formatAs "%.3f").replace("""\.?0+$""".toRegex(), "")()).blue }
        tooltip += MIRAGE_FLOUR_DESCRIPTION_TRANSLATION().yellow
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        if (!user.isSneaking) {
            return TypedActionResult.pass(itemStack)
        } else {
            if (world.isClient) return TypedActionResult.success(itemStack)

            val chanceTable = getCommonChanceTable(user).compressRate().sortedDescending()

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
