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
import net.minecraft.world.Heightmap

object TraitFactors {
    val ALWAYS = TraitFactor { _, _ -> 1.0 }
    val FLOOR_MOISTURE = TraitFactor { world, blockPos -> world.getMoisture(blockPos.down()) }
    val FLOOR_CRYSTAL_ERG = TraitFactor { world, blockPos -> world.getCrystalErg(blockPos.down()) }
    val FLOOR_HARDNESS = TraitFactor { world, blockPos ->
        val blockState = world.getBlockState(blockPos.down())
        if (!blockState.isIn(BlockTags.PICKAXE_MINEABLE)) return@TraitFactor 0.0
        val hardness = blockState.getHardness(world, blockPos.down())
        if (hardness < 0) return@TraitFactor 0.0
        hardness / 2.0 atMost 2.0
    }
    val LIGHT = TraitFactor { world, blockPos -> (world.getLightLevel(blockPos) - 8 atLeast 0) / 7.0 }
    val DARKNESS = TraitFactor { world, blockPos -> ((15 - world.getLightLevel(blockPos)) - 8 atLeast 0) / 7.0 }
    val LOW_TEMPERATURE = TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.LOW }
    val MEDIUM_TEMPERATURE = TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.MEDIUM }
    val HIGH_TEMPERATURE = TraitCondition { world, blockPos -> world.getBiome(blockPos).temperatureCategory == TemperatureCategory.HIGH }
    val LOW_HUMIDITY = TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.LOW }
    val MEDIUM_HUMIDITY = TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.MEDIUM }
    val HIGH_HUMIDITY = TraitCondition { world, blockPos -> world.getBiome(blockPos).humidityCategory == HumidityCategory.HIGH }
    val OUTDOOR = TraitCondition { world, blockPos -> blockPos.y >= world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).y }
}
