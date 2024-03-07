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
