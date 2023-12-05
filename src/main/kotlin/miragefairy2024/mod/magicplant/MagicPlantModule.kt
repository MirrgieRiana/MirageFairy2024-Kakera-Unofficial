package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.Poem
import miragefairy2024.mod.magicplant.magicplants.initMirageFlower
import miragefairy2024.mod.mirageFairy2024ItemGroup
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerGeneratedItemModelGeneration
import miragefairy2024.util.registerItemGroup
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

val TRAIT_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.trait" }, "Trait", "特性")
val CREATIVE_ONLY_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.creativeOnly" }, "Creative Only", "クリエイティブ専用")
val INVALID_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.invalid" }, "Invalid", "無効")

fun initMagicPlantModule() {

    TraitEffectKeyCard.entries.forEach { card ->
        card.traitEffectKey.register(card.identifier)
        card.traitEffectKey.enJa(card.enName, card.jaName)
    }

    TraitCard.entries.forEach { card ->
        card.trait.register(card.identifier)
        card.trait.enJa(card.enName, card.jaName)
    }

    TRAIT_TRANSLATION.enJa()
    CREATIVE_ONLY_TRANSLATION.enJa()
    INVALID_TRANSLATION.enJa()

    worldGenTraitGenerations += WorldGenTraitGeneration { world, blockPos, block ->
        val resultTraitStackList = mutableListOf<TraitStack>()

        // レシピ判定
        val aTraitStackList = mutableListOf<TraitStack>()
        val nTraitStackList = mutableListOf<TraitStack>()
        val rTraitStackList = mutableListOf<TraitStack>()
        val sTraitStackList = mutableListOf<TraitStack>()
        worldGenTraitRecipeRegistry[block].or { listOf() }.forEach { recipe ->
            if (recipe.condition.canSpawn(world, blockPos)) {
                val traitStackList = when (recipe.rarity) {
                    WorldGenTraitRecipe.Rarity.A -> aTraitStackList
                    WorldGenTraitRecipe.Rarity.N -> nTraitStackList
                    WorldGenTraitRecipe.Rarity.R -> rTraitStackList
                    WorldGenTraitRecipe.Rarity.S -> sTraitStackList
                }
                traitStackList += TraitStack(recipe.trait, recipe.level)
            }
        }

        // 抽選
        val r = world.random.nextDouble()
        if (r < 0.01) { // S
            if (sTraitStackList.isNotEmpty()) {
                resultTraitStackList += sTraitStackList[world.random.nextInt(sTraitStackList.size)]
            }
        } else if (r >= 0.01 && r < 0.1) { // R
            if (rTraitStackList.isNotEmpty()) {
                resultTraitStackList += rTraitStackList[world.random.nextInt(rTraitStackList.size)]
            }
        } else if (r >= 0.1 && r < 0.2) { // N
            if (nTraitStackList.isNotEmpty()) {
                nTraitStackList.removeAt(world.random.nextInt(rTraitStackList.size))
                resultTraitStackList += nTraitStackList
            }
        }
        resultTraitStackList += aTraitStackList // A

        resultTraitStackList
    }

    initMirageFlower()

}

abstract class MagicPlantCard<B : MagicPlantBlock, BE : BlockEntity>(
    blockPath: String,
    val blockEnName: String,
    val blockJaName: String,
    itemPath: String,
    val itemEnName: String,
    val itemJaName: String,
    val seedPoemList: List<Poem>,
    blockCreator: () -> B,
    blockEntityCreator: (BlockPos, BlockState) -> BE,
) {
    companion object {
        fun createCommonSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)
    }

    val blockIdentifier = Identifier(MirageFairy2024.modId, blockPath)
    val itemIdentifier = Identifier(MirageFairy2024.modId, itemPath)
    val block = blockCreator()
    val blockEntityType = BlockEntityType(blockEntityCreator, setOf(block), null)
    val item = MagicPlantSeedItem(block, Item.Settings())

    fun init() {
        block.register(blockIdentifier)
        blockEntityType.register(blockIdentifier)
        item.register(itemIdentifier)

        item.registerItemGroup(mirageFairy2024ItemGroup)

        block.registerCutoutRenderLayer()
        item.registerGeneratedItemModelGeneration()

        block.enJa(blockEnName, blockJaName)
        item.enJa(itemEnName, itemJaName)
        item.registerPoem(seedPoemList)
        item.registerPoemGeneration(seedPoemList)

        item.registerComposterInput(0.3F) // 種はコンポスターに投入可能
    }

}
