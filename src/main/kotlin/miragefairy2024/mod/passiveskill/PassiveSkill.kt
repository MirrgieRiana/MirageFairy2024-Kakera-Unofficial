package miragefairy2024.mod.passiveskill

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.Motif
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

context(ModContext)
fun initPassiveSkillModule() {
    initPassiveSkillConditions()
    initPassiveSkillEffects()
    initPassiveSkillExecution()
}


interface PassiveSkillProvider {
    fun getPassiveSkill(itemStack: ItemStack): PassiveSkill?
}


class PassiveSkill(val providerId: Identifier, val motif: Motif?, val rare: Double, val count: Double, val specifications: List<PassiveSkillSpecification<*>>)


class PassiveSkillSpecification<T>(val conditions: List<PassiveSkillCondition>, val effect: PassiveSkillEffect<T>, val valueProvider: (mana: Double) -> T)


class PassiveSkillContext(val world: World, val blockPos: BlockPos, val player: PlayerEntity)


interface PassiveSkillCondition {
    val text: Text
    fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean
}


val passiveSkillEffectRegistryKey: RegistryKey<Registry<PassiveSkillEffect<*>>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "passive_skill_effect"))
val passiveSkillEffectRegistry: Registry<PassiveSkillEffect<*>> = FabricRegistryBuilder.createSimple(passiveSkillEffectRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

interface PassiveSkillEffect<T> {
    val isPreprocessor: Boolean
    fun getText(value: T): Text
    val unit: T
    fun castOrThrow(value: Any?): T
    fun combine(a: T, b: T): T
    fun update(context: PassiveSkillContext, oldValue: T, newValue: T)
}
