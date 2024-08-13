package miragefairy2024.mod.magicplant.contents

import miragefairy2024.mod.magicplant.TraitCondition
import miragefairy2024.mod.magicplant.getMagicPlantBlockEntity
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.getCrystalErg
import miragefairy2024.util.getMoisture
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.temperatureCategory
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World

enum class TraitConditionCard(
    val traitCondition: TraitCondition,
) {
    FLOOR_MOISTURE(TraitCondition { world, blockPos -> world.getMoisture(blockPos.down()) }),
    FLOOR_CRYSTAL_ERG(TraitCondition { world, blockPos -> world.getCrystalErg(blockPos.down()) }),
    FLOOR_HARDNESS(TraitCondition { world, blockPos -> getFloorHardness(world, blockPos) }),
    LIGHT(TraitCondition { world, blockPos -> (world.getLightLevel(blockPos) - 8 atLeast 0) / 7.0 }),
    DARKNESS(TraitCondition { world, blockPos -> ((15 - world.getLightLevel(blockPos)) - 8 atLeast 0) / 7.0 }),
    LOW_TEMPERATURE(TraitCondition { world, blockPos -> if (world.getBiome(blockPos).temperatureCategory == TemperatureCategory.LOW) 1.0 else 0.0 }),
    MEDIUM_TEMPERATURE(TraitCondition { world, blockPos -> if (world.getBiome(blockPos).temperatureCategory == TemperatureCategory.MEDIUM) 1.0 else 0.0 }),
    HIGH_TEMPERATURE(TraitCondition { world, blockPos -> if (world.getBiome(blockPos).temperatureCategory == TemperatureCategory.HIGH) 1.0 else 0.0 }),
    LOW_HUMIDITY(TraitCondition { world, blockPos -> if (world.getBiome(blockPos).humidityCategory == HumidityCategory.LOW) 1.0 else 0.0 }),
    MEDIUM_HUMIDITY(TraitCondition { world, blockPos -> if (world.getBiome(blockPos).humidityCategory == HumidityCategory.MEDIUM) 1.0 else 0.0 }),
    HIGH_HUMIDITY(TraitCondition { world, blockPos -> if (world.getBiome(blockPos).humidityCategory == HumidityCategory.HIGH) 1.0 else 0.0 }),
    OUTDOOR(TraitCondition { world, blockPos -> if (blockPos.y >= world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).y) 1.0 else 0.0 }),
    NATURAL(TraitCondition { world, blockPos -> if (world.getMagicPlantBlockEntity(blockPos)?.isNatural() == true) 1.0 else 0.0 }),
}

private fun getFloorHardness(world: World, blockPos: BlockPos): Double {
    val blockState = world.getBlockState(blockPos.down())
    if (!blockState.isIn(BlockTags.PICKAXE_MINEABLE)) return 0.0
    val hardness = blockState.getHardness(world, blockPos.down())
    if (hardness < 0) return 0.0
    return hardness / 2.0 atMost 2.0
}
