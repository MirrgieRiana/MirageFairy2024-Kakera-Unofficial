package miragefairy2024.mod.passiveskill

import miragefairy2024.mod.fairy.Motif
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.world.entity.player.Player as PlayerEntity

interface PassiveSkillProvider {
    fun getPassiveSkill(itemStack: ItemStack): PassiveSkill?
}

class PassiveSkill(val providerId: ResourceLocation, val motif: Motif?, val rare: Double, val count: Double, val specifications: List<PassiveSkillSpecification<*>>)

class PassiveSkillSpecification<T>(val conditions: List<PassiveSkillCondition>, val effect: PassiveSkillEffect<T>, val valueProvider: (mana: Double) -> T)

interface PassiveSkillCondition {
    val text: Component
    fun test(context: PassiveSkillContext, level: Double, mana: Double): Boolean
}

interface PassiveSkillEffect<T> {
    val isPreprocessor: Boolean
    fun getText(value: T): Component
    val unit: T
    fun castOrThrow(value: Any?): T
    fun combine(a: T, b: T): T
    fun fromNbt(nbt: NbtCompound): T = unit
    fun toNbt(value: T): NbtCompound = NbtCompound()
    fun update(context: PassiveSkillContext, oldValue: T, newValue: T)
}

class PassiveSkillContext(val world: Level, val blockPos: BlockPos, val player: PlayerEntity)
