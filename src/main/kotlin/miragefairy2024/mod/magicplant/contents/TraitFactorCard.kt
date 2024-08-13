package miragefairy2024.mod.magicplant.contents

import miragefairy2024.mod.magicplant.TraitCondition
import miragefairy2024.mod.magicplant.TraitFactor
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

enum class TraitFactorCard(
    val traitFactor: TraitFactor,
) {
    FLOOR_MOISTURE(TraitFactor { world, blockPos -> world.getMoisture(blockPos.down()) }),
    FLOOR_CRYSTAL_ERG(TraitFactor { world, blockPos -> world.getCrystalErg(blockPos.down()) }),
    FLOOR_HARDNESS(TraitFactor { world, blockPos -> getFloorHardness(world, blockPos) }),
    LIGHT(TraitFactor { world, blockPos -> (world.getLightLevel(blockPos) - 8 atLeast 0) / 7.0 }),
    DARKNESS(TraitFactor { world, blockPos -> ((15 - world.getLightLevel(blockPos)) - 8 atLeast 0) / 7.0 }),
    LOW_TEMPERATURE(TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.LOW }),
    MEDIUM_TEMPERATURE(TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.MEDIUM }),
    HIGH_TEMPERATURE(TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.HIGH }),
    LOW_HUMIDITY(TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.LOW }),
    MEDIUM_HUMIDITY(TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.MEDIUM }),
    HIGH_HUMIDITY(TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.HIGH }),
    OUTDOOR(TraitCondition { world, blockPos -> blockPos.y >= world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).y }),
}

private fun getFloorHardness(world: World, blockPos: BlockPos): Double {
    val blockState = world.getBlockState(blockPos.down())
    if (!blockState.isIn(BlockTags.PICKAXE_MINEABLE)) return 0.0
    val hardness = blockState.getHardness(world, blockPos.down())
    if (hardness < 0) return 0.0
    return hardness / 2.0 atMost 2.0
}
