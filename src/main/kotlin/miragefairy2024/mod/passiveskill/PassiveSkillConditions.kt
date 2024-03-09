package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.mod.lastFood
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import miragefairy2024.util.orEmpty
import miragefairy2024.util.removeTrailingZeros
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World

fun initPassiveSkillConditions() {
    OutdoorPassiveSkillCondition.init()
    IndoorPassiveSkillCondition.init()
}

abstract class PassiveSkillConditionCard(path: String) : PassiveSkillCondition {
    val identifier = Identifier(MirageFairy2024.modId, path)
    open fun init() = Unit
}


private fun simple(path: String, enName: String, jaName: String, block: (world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double) -> Boolean): PassiveSkillConditionCard {
    return object : PassiveSkillConditionCard(path) {
        override fun test(world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double) = block(world, blockPos, player, mana)
        val translation = Translation({ "miragefairy2024.passive_skill_condition.${identifier.toTranslationKey()}" }, enName, jaName)
        override val text = translation()
        override fun init() {
            translation.enJa()
        }
    }
}

private fun isOutdoor(player: PlayerEntity) = player.eyeBlockPos.y >= player.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, player.eyeBlockPos).y
private fun isIndoor(player: PlayerEntity) = !isOutdoor(player)

val OutdoorPassiveSkillCondition = simple("outdoor", "Outdoor", "屋外") { _, _, player, _ -> isOutdoor(player) }
val IndoorPassiveSkillCondition = simple("indoor", "Indoor", "屋内") { _, _, player, _ -> isIndoor(player) }

class IntComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Int) : PassiveSkillCondition {
    class Term(val emoji: Emoji, val unit: Int = 1, val getValue: (world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double) -> Int)

    private fun format(double: Double) = (double formatAs "%.8f").removeTrailingZeros()
    override val text: Text get() = text { term.emoji() + format(threshold / term.unit.toDouble())() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double): Boolean {
        val value = term.getValue(world, blockPos, player, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}

class DoubleComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Double) : PassiveSkillCondition {
    class Term(val emoji: Emoji, val unit: Double = 1.0, val getValue: (world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double) -> Double)

    private fun format(double: Double) = (double formatAs "%.8f").removeTrailingZeros()
    override val text: Text get() = text { term.emoji() + format(threshold / term.unit)() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double): Boolean {
        val value = term.getValue(world, blockPos, player, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}

val ManaTerm = DoubleComparisonPassiveSkillCondition.Term(Emoji.MANA) { _, _, _, mana -> mana }
val LightLevelTerm = IntComparisonPassiveSkillCondition.Term(Emoji.LIGHT) { _, _, player, _ -> player.world.getLightLevel(player.eyeBlockPos) }
val FoodLevelTerm = IntComparisonPassiveSkillCondition.Term(Emoji.FOOD, 2) { _, _, player, _ -> player.hungerManager.foodLevel }

// TODO タグによる料理素材判定
class FoodPassiveSkillCondition(private val item: Item) : PassiveSkillCondition {
    override val text: Text get() = item.name
    override fun test(world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double) = player.lastFood.itemStack.orEmpty.isOf(item)
}
