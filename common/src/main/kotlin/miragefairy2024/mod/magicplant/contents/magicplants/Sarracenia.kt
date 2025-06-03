package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

object SarraceniaCard : SimpleMagicPlantCard<SarraceniaBlock>() {

}

class SarraceniaBlock(settings: Properties) : SimpleMagicPlantBlock(SarraceniaCard, settings) {
    companion object {
        val CODEC: MapCodec<SarraceniaBlock> = simpleCodec(::SarraceniaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
