package miragefairy2024.mod.passiveskill.effects

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.passiveskill.PassiveSkillEffect
import miragefairy2024.mod.passiveskill.passiveSkillEffectRegistry
import miragefairy2024.util.boolean
import miragefairy2024.util.double
import miragefairy2024.util.get
import miragefairy2024.util.register
import miragefairy2024.util.wrapper
import net.minecraft.nbt.CompoundTag as NbtCompound

abstract class AbstractPassiveSkillEffect<T>(path: String) : PassiveSkillEffect<T> {
    val identifier = MirageFairy2024.identifier(path)

    context(ModContext)
    open fun init() {
        this.register(passiveSkillEffectRegistry, identifier)
    }

    override val isPreprocessor = false
}

abstract class AbstractDoublePassiveSkillEffect(path: String) : AbstractPassiveSkillEffect<Double>(path) {
    override val unit = 0.0
    override fun castOrThrow(value: Any?) = value as Double
    override fun combine(a: Double, b: Double) = a + b
    override fun fromNbt(nbt: NbtCompound) = nbt.wrapper["value"].double.get()!!
    override fun toNbt(value: Double) = NbtCompound().also { it.wrapper["value"].double.set(value) }
}

abstract class AbstractBooleanPassiveSkillEffect(path: String) : AbstractPassiveSkillEffect<Boolean>(path) {
    override val unit = false
    override fun castOrThrow(value: Any?) = value as Boolean
    override fun combine(a: Boolean, b: Boolean) = a || b
    override fun fromNbt(nbt: NbtCompound) = nbt.wrapper["value"].boolean.get()!!
    override fun toNbt(value: Boolean) = NbtCompound().also { it.wrapper["value"].boolean.set(value) }
}
