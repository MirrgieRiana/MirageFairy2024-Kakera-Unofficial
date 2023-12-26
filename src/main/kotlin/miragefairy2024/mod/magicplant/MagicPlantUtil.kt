package miragefairy2024.mod.magicplant

import miragefairy2024.mod.mirageFairy2024ItemGroup
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerGeneratedItemModelGeneration
import miragefairy2024.util.registerItemGroup
import net.minecraft.block.Block
import net.minecraft.registry.Registries

fun MagicPlantCard<*, *>.initMagicPlant() {

    // 登録
    block.register(Registries.BLOCK, blockIdentifier)
    blockEntityType.register(Registries.BLOCK_ENTITY_TYPE, blockIdentifier)
    item.register(Registries.ITEM, itemIdentifier)

    // 分類
    item.registerItemGroup(mirageFairy2024ItemGroup)

    // 見た目
    block.registerCutoutRenderLayer()
    item.registerGeneratedItemModelGeneration()

    // 翻訳
    block.enJa(blockEnName, blockJaName)
    item.enJa(itemEnName, itemJaName)
    item.registerPoem(seedPoemList)
    item.registerPoemGeneration(seedPoemList)

    // 性質
    //block.registerTagGenerate(BlockTags.SMALL_FLOWERS) // これをやるとエンダーマンが勝手に引っこ抜いていく

    // レシピ
    item.registerComposterInput(0.3F) // 種はコンポスターに投入可能

}

class WorldGenTraitRecipeInitScope(val block: Block) {

    fun registerWorldGenTraitRecipe(pattern: String, traitCard: TraitCard, condition: WorldGenTraitRecipe.Condition = WorldGenTraitRecipe.Condition.Always) {
        pattern.forEachIndexed { i, ch ->
            val rarity = when (ch) {
                '.' -> return@forEachIndexed
                'A' -> WorldGenTraitRecipe.Rarity.A
                'C' -> WorldGenTraitRecipe.Rarity.C
                'N' -> WorldGenTraitRecipe.Rarity.N
                'R' -> WorldGenTraitRecipe.Rarity.R
                'S' -> WorldGenTraitRecipe.Rarity.S
                else -> throw IllegalArgumentException()
            }
            val level = 1 shl (pattern.length - 1 - i)
            registerWorldGenTraitRecipe(WorldGenTraitRecipe(block, rarity, traitCard.trait, level, condition))
        }
    }

}
