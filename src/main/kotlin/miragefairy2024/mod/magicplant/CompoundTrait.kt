package miragefairy2024.mod.magicplant

import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class CompoundTrait(sortKey: String, private val factor: TraitFactor, private val traitEffectKeyCard: TraitEffectKeyCard) : Trait(traitEffectKeyCard.color, sortKey) {
    override fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects? {
        val factor = factor.getFactor(world, blockPos)
        return if (factor != 0.0) {
            val traitEffects = MutableTraitEffects()
            traitEffects[traitEffectKeyCard.traitEffectKey] = traitEffectKeyCard.traitEffectKey.getValue(level) * factor
            traitEffects
        } else {
            null
        }
    }
}

fun interface TraitFactor {
    fun getFactor(world: World, blockPos: BlockPos): Double
}

fun interface TraitCondition : TraitFactor {
    override fun getFactor(world: World, blockPos: BlockPos) = if (isValid(world, blockPos)) 1.0 else 0.0
    fun isValid(world: World, blockPos: BlockPos): Boolean
}
