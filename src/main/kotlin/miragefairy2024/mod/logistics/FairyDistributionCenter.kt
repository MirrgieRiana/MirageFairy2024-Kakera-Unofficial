package miragefairy2024.mod.logistics

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.poem
import miragefairy2024.util.EnJa
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.item.BlockItem
import net.minecraft.item.Item

object FairyDistributionCenter {
    val name = EnJa("Fairy Distribution Center", "妖精のお届けもの屋")
    val identifier = MirageFairy2024.identifier("fairy_distribution_center")
    val block = FairyDistributionCenterBlock(FabricBlockSettings.create()) // TODO
    val item = BlockItem(block, Item.Settings())
    val poemList = PoemList(3).poem("TODO", "TODO") // TODO
}

context(ModContext)
fun initFairyDistributionCenter() {

}

class FairyDistributionCenterBlock(settings: Settings) : FairyLogisticsBlock(settings) { // TODO

}
