package miragefairy2024.mod.passiveskill

import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.text.Text

class ToolMaterialCardPassiveSkillCondition(private val card: ToolMaterialCard) : PassiveSkillCondition {
    override val text: Text get() = text { card.translation() }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.mainHandStack.isIn(card.tag)
}
