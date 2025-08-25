package miragefairy2024.mod.magicplant.contents.magicplants

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.EnJa
import miragefairy2024.util.createCuboidShape
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty

abstract class AbstractLuminariaCard<B : SimpleMagicPlantBlock> : SimpleMagicPlantCard<B>() {
    override val classification = EnJa("Order Miragales, family Luminariaceae", "妖花目ルミナリア科")
    override val baseGrowth = super.baseGrowth / 5
    override val baseSeedGeneration = 1.0

    override val ageProperty: IntegerProperty = BlockStateProperties.AGE_3

    override val outlineShapes = listOf(
        createCuboidShape(4.0, 6.0),
        createCuboidShape(5.0, 13.0),
        createCuboidShape(7.0, 16.0),
        createCuboidShape(7.0, 16.0),
    )

    override val family = MirageFairy2024.identifier("luminaria")
}

fun getWeakLuminance(age: Int) = if (age == 3) 11 else 0
fun getLuminance(age: Int) = if (age == 3) 15 else 0
