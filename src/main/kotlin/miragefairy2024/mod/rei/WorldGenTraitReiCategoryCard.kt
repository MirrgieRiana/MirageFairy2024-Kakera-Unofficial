package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.entry.EntryIngredient
import miragefairy2024.mod.magicplant.TraitSpawnRarity
import miragefairy2024.mod.magicplant.TraitStack
import miragefairy2024.mod.magicplant.TraitStacks
import miragefairy2024.mod.magicplant.WorldGenTraitRecipe
import miragefairy2024.mod.magicplant.getIdentifier
import miragefairy2024.mod.magicplant.setTraitStacks
import miragefairy2024.mod.magicplant.toTrait
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.string
import miragefairy2024.util.toBlock
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toIdentifier
import mirrg.kotlin.hydrogen.Single
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object WorldGenTraitReiCategoryCard : ReiCategoryCard<WorldGenTraitReiCategoryCard.Display>("world_gen_trait", "World Gen Trait", "地形生成特性") {
    override val serializer: Single<BasicDisplay.Serializer<Display>> by lazy {
        Single(BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
                WorldGenTraitRecipe(
                    tag.getString("Block").toIdentifier().toBlock(),
                    TraitSpawnRarity.valueOf(tag.getString("Rarity")),
                    tag.getString("Trait").toIdentifier().toTrait()!!,
                    tag.getInt("Level"),
                    object : WorldGenTraitRecipe.Condition {
                        override val description get() = Text.Serializer.fromJson(tag.getString("ConditionDescription"))!!
                        override fun canSpawn(world: World, blockPos: BlockPos) = throw UnsupportedOperationException()
                    },
                )
            )
        }, { display, tag ->
            tag.putString("Block", display.recipe.block.getIdentifier().string)
            tag.putString("Rarity", display.recipe.rarity.name)
            tag.putString("Trait", display.recipe.trait.getIdentifier().string)
            tag.putInt("Level", display.recipe.level)
            tag.putString("ConditionDescription", Text.Serializer.toJson(display.recipe.condition.description))
        }))
    }

    private fun WorldGenTraitRecipe.getOutput(): List<EntryIngredient> {
        val trait = TraitStack(this.trait, this.level)
        val itemStack = this.block.asItem().createItemStack().also { it.setTraitStacks(TraitStacks.of(listOf(trait))) }
        return listOf(itemStack.toEntryStack().toEntryIngredient())
    }

    class Display(val recipe: WorldGenTraitRecipe) : BasicDisplay(
        listOf(),
        recipe.getOutput(),
    ) {
        override fun getCategoryIdentifier() = identifier.first
    }
}
