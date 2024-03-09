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
import net.minecraft.item.Item
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.Heightmap

fun initPassiveSkillConditions() {
    OutdoorPassiveSkillCondition.init()
    IndoorPassiveSkillCondition.init()
}

abstract class PassiveSkillConditionCard(path: String) : PassiveSkillCondition {
    val identifier = Identifier(MirageFairy2024.modId, path)
    open fun init() = Unit
}


private fun simple(path: String, enName: String, jaName: String, block: (context: PassiveSkillContext, mana: Double) -> Boolean): PassiveSkillConditionCard {
    return object : PassiveSkillConditionCard(path) {
        override fun test(context: PassiveSkillContext, mana: Double) = block(context, mana)
        val translation = Translation({ "miragefairy2024.passive_skill_condition.${identifier.toTranslationKey()}" }, enName, jaName)
        override val text = translation()
        override fun init() {
            translation.enJa()
        }
    }
}

private fun isOutdoor(context: PassiveSkillContext) = context.player.eyeBlockPos.y >= context.player.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, context.player.eyeBlockPos).y
private fun isIndoor(context: PassiveSkillContext) = !isOutdoor(context)

val OutdoorPassiveSkillCondition = simple("outdoor", "Outdoor", "屋外") { context, _ -> isOutdoor(context) }
val IndoorPassiveSkillCondition = simple("indoor", "Indoor", "屋内") { context, _ -> isIndoor(context) }

class IntComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Int) : PassiveSkillCondition {
    companion object {
        val LIGHT_LEVEL_TERM = Term(Emoji.LIGHT) { context, _ -> context.player.world.getLightLevel(context.player.eyeBlockPos) }
        val FOOD_LEVEL_TERM = Term(Emoji.FOOD, 2) { context, _ -> context.player.hungerManager.foodLevel }
    }

    class Term(val emoji: Emoji, val unit: Int = 1, val getValue: (context: PassiveSkillContext, mana: Double) -> Int)

    private fun format(double: Double) = (double formatAs "%.8f").removeTrailingZeros()
    override val text: Text get() = text { term.emoji() + format(threshold / term.unit.toDouble())() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(context: PassiveSkillContext, mana: Double): Boolean {
        val value = term.getValue(context, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}

class DoubleComparisonPassiveSkillCondition(private val term: Term, private val isGreaterOrEquals: Boolean, private val threshold: Double) : PassiveSkillCondition {
    companion object {
        val MANA_TERM = Term(Emoji.MANA) { _, mana -> mana }
    }

    class Term(val emoji: Emoji, val unit: Double = 1.0, val getValue: (context: PassiveSkillContext, mana: Double) -> Double)

    private fun format(double: Double) = (double formatAs "%.8f").removeTrailingZeros()
    override val text: Text get() = text { term.emoji() + format(threshold / term.unit)() + if (isGreaterOrEquals) Emoji.UP() else Emoji.DOWN() }
    override fun test(context: PassiveSkillContext, mana: Double): Boolean {
        val value = term.getValue(context, mana)
        return if (isGreaterOrEquals) value >= threshold else value <= threshold
    }
}

// TODO タグによる料理素材判定
class FoodPassiveSkillCondition(private val item: Item) : PassiveSkillCondition {
    override val text: Text get() = item.name
    override fun test(context: PassiveSkillContext, mana: Double) = context.player.lastFood.itemStack.orEmpty.isOf(item)
}
