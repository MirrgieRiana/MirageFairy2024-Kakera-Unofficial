package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.util.EnJa
import miragefairy2024.util.createCuboidShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.MapColor

object SarraceniaCard : SimpleMagicPlantCard<SarraceniaBlock>() {
    override fun getBlockPath() = "sarracenia"
    override val blockName = EnJa("Sarracenia", "瓶子草サラセニア")
    override fun getItemPath() = "sarracenia_bulb"
    override val itemName = EnJa("Sarracenia Bulb", "サラセニアの球根")
    override val tier = 2
    override val poem = EnJa("TODO", "TODO") // TODO
    override val classification = EnJa("Order Miragales, family Veropedaceae", "妖花目ヴェロペダ科")

    override val ageProperty: IntegerProperty = BlockStateProperties.AGE_3
    override fun createBlock() = SarraceniaBlock(createCommonSettings().breakInstantly().mapColor(MapColor.NETHER).sound(SoundType.CROP))

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(4.0, 7.0),
        createCuboidShape(7.0, 9.0),
        createCuboidShape(7.0, 16.0),
    )


}

class SarraceniaBlock(settings: Properties) : SimpleMagicPlantBlock(SarraceniaCard, settings) {
    companion object {
        val CODEC: MapCodec<SarraceniaBlock> = simpleCodec(::SarraceniaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
