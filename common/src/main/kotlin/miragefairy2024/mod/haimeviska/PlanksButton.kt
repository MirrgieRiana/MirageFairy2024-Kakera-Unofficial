package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags

context(ModContext)
fun initPlanksButtonHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.WOODEN_BUTTONS }
    card.item.registerItemTagGeneration { ItemTags.WOODEN_BUTTONS }

}
