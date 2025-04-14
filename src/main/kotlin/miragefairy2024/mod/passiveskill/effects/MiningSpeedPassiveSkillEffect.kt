package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixin.api.BlockBreakingCallback
import miragefairy2024.mod.passiveskill.PassiveSkillContext
import miragefairy2024.mod.passiveskill.passiveSkillResult
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.atMost
import mirrg.kotlin.hydrogen.formatAs

object MiningSpeedPassiveSkillEffect : AbstractDoublePassiveSkillEffect("mining_speed") {
    private val translation = Translation({ "${MirageFairy2024.MOD_ID}.passive_skill_type.${identifier.toLanguageKey()}" }, "Mining Speed: %s", "採掘速度: %s")
    override fun getText(value: Double) = text { translation(value * 100 formatAs "%+.1f%%") }
    override fun update(context: PassiveSkillContext, oldValue: Double, newValue: Double) = Unit

    context(ModContext)
    override fun init() {
        super.init()
        translation.enJa()

        BlockBreakingCallback.EVENT.register { blockState, player, _, _, blockBreakingDelta ->
            if (player.mainHandItem.getMiningSpeedMultiplier(blockState) <= 1F) return@register blockBreakingDelta
            val newBlockBreakingDelta = blockBreakingDelta * (1F + player.passiveSkillResult[MiningSpeedPassiveSkillEffect].toFloat())
            if (blockBreakingDelta < 1F) newBlockBreakingDelta atMost 0.99F else newBlockBreakingDelta
        }
    }
}
