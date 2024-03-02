package miragefairy2024.mod.passiveskill

import miragefairy2024.util.join
import miragefairy2024.util.text
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun initPassiveSkillModule() {

}

interface PassiveSkillProvider {
    fun getPassiveSkill(itemStack: ItemStack): PassiveSkill?
}

class PassiveSkill(val itemStackMana: Double, val specifications: List<PassiveSkillSpecification<*>>)

class PassiveSkillSpecification<T>(val conditions: List<PassiveSkillCondition>, val effect: PassiveSkillEffect<T>, val valueProvider: (mana: Double) -> T)

fun <T> PassiveSkillSpecification<T>.getText(mana: Double) = text {
    this@getText.effect.getText(this@getText.valueProvider(mana)) +
        if (this@getText.conditions.isNotEmpty()) " ["() + this@getText.conditions.map { it.text }.join(","()) + "]"() else empty()
}

interface PassiveSkillCondition {
    val text: Text
    fun test(world: World, blockPos: BlockPos, player: PlayerEntity, mana: Double): Boolean
}

interface PassiveSkillEffect<T> {
    val isPreprocessor: Boolean
    fun getText(value: T): Text
    val unit: T
    fun castOrThrow(value: Any?): T
    fun combine(a: T, b: T): T
    fun update(world: World, blockPos: BlockPos, player: PlayerEntity, oldValue: T, newValue: T)
}
