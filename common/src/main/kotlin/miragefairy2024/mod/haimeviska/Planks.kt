package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.material.MapColor

fun createPlankSettings(sound: Boolean = true) = createBaseWoodSetting(sound = sound).strength(2.0F, 3.0F).mapColor(MapColor.RAW_IRON)

context(ModContext)
fun initPlanksHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.PLANKS }
    card.item.registerItemTagGeneration { ItemTags.PLANKS }

}
