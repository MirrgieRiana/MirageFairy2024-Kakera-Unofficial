package miragefairy2024.mod.logistics

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.withHorizontalRotation
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags

object FairyDistributionCenter {
    val name = EnJa("Fairy Distribution Center", "妖精のお届けもの屋")
    val identifier = MirageFairy2024.identifier("fairy_distribution_center")
    val block = FairyDistributionCenterBlock(FabricBlockSettings.create()) // TODO
    val item = BlockItem(block, Item.Settings())
    val poemList = PoemList(3).poem("TODO", "TODO") // TODO
}

context(ModContext)
fun initFairyDistributionCenter() {
    FairyDistributionCenter.let { card ->

        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        card.block.registerCutoutRenderLayer()

        card.block.enJa(card.name)
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }

        card.block.registerDefaultLootTableGeneration()

    }
}

class FairyDistributionCenterBlock(settings: Settings) : FairyLogisticsBlock(settings) { // TODO

}
