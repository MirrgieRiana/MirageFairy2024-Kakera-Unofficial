package miragefairy2024.mod.passiveskill.conditions

import miragefairy2024.mod.passiveskill.PassiveSkillCondition
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.tool.ToolMaterialCard
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import net.minecraft.network.chat.Component as Text

class ToolMaterialCardPassiveSkillCondition(private val card: ToolMaterialCard) : PassiveSkillCondition {
    override val text: Text get() = text { card.translation() }
    override fun test(context: PassiveSkillContext, level: Double, mana: Double) = context.player.mainHandItem.`is`(card.tag)
}
