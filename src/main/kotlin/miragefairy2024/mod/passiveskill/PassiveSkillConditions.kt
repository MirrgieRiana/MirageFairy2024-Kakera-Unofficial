package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.eyeBlockPos
import miragefairy2024.util.invoke
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World

fun initPassiveSkillConditions() {
    PassiveSkillConditionCard.entries.forEach { card ->
        card.translations.forEach {
            it.enJa()
        }
    }
}

private fun isOutdoor(player: PlayerEntity) = player.eyeBlockPos.y >= player.world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, player.eyeBlockPos).y

private fun isIndoor(player: PlayerEntity) = !isOutdoor(player)

abstract class PassiveSkillConditionCard(path: String) : PassiveSkillCondition {
    companion object {
        val entries = mutableListOf<PassiveSkillConditionCard>()
        private operator fun PassiveSkillConditionCard.unaryPlus() = this.also { entries += it }

        val OUTDOOR = +object : SimplePassiveSkillCondition("outdoor", "Outdoor", "屋外") {
            override fun test(world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double) = isOutdoor(player)
        }
        val INDOOR = +object : SimplePassiveSkillCondition("indoor", "Indoor", "屋内") {
            override fun test(world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double) = isIndoor(player)
        }
    }

    val identifier = Identifier(MirageFairy2024.modId, path)
    abstract val translations: List<Translation>
}

private abstract class SimplePassiveSkillCondition(path: String, enName: String, jaName: String) : PassiveSkillConditionCard(path) {
    val translation = Translation({ "miragefairy2024.passive_skill_condition.${identifier.toTranslationKey()}" }, enName, jaName)
    override val text = translation()
    override val translations = listOf(translation)
}
