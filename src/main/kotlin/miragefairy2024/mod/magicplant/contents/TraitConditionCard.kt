package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.Emoji
import miragefairy2024.mod.invoke
import miragefairy2024.mod.magicplant.TraitCondition
import miragefairy2024.mod.magicplant.getMagicPlantBlockEntity
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.getCrystalErg
import miragefairy2024.util.getMoisture
import miragefairy2024.util.humidityCategory
import miragefairy2024.util.invoke
import miragefairy2024.util.temperatureCategory
import mirrg.kotlin.hydrogen.atLeast
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World

enum class TraitConditionCard(
    path: String,
    emoji: Emoji,
    enName: String,
    jaName: String,
    function: (world: World, blockPos: BlockPos) -> Double,
) {
    FLOOR_MOISTURE("floor_moisture", Emoji.FLOOR_MOISTURE, "Floor Moisture", "湿った地面", { world, blockPos -> world.getMoisture(blockPos.down()) }),
    FLOOR_CRYSTAL_ERG("floor_crystal_erg", Emoji.FLOOR_CRYSTAL_ERG, "Floor Crystal Erg", "鉱物質の地面", { world, blockPos -> world.getCrystalErg(blockPos.down()) }),
    FLOOR_HARDNESS("floor_hardness", Emoji.FLOOR_HARDNESS, "Floor Hardness", "硬い地面", { world, blockPos -> getFloorHardness(world, blockPos) }),
    LIGHT("light", Emoji.LIGHT, "Light", "光", { world, blockPos -> (world.getLightLevel(blockPos) - 8 atLeast 0) / 7.0 }),
    DARKNESS("darkness", Emoji.DARKNESS, "Darkness", "闇", { world, blockPos -> ((15 - world.getLightLevel(blockPos)) - 8 atLeast 0) / 7.0 }),
    LOW_TEMPERATURE("low_temperature", Emoji.LOW_TEMPERATURE, "Low Temperature", "低い気温", { world, blockPos -> if (world.getBiome(blockPos).temperatureCategory == TemperatureCategory.LOW) 1.0 else 0.0 }),
    MEDIUM_TEMPERATURE("medium_temperature", Emoji.MEDIUM_TEMPERATURE, "Medium Temperature", "普通の気温", { world, blockPos -> if (world.getBiome(blockPos).temperatureCategory == TemperatureCategory.MEDIUM) 1.0 else 0.0 }),
    HIGH_TEMPERATURE("high_temperature", Emoji.HIGH_TEMPERATURE, "High Temperature", "高い気温", { world, blockPos -> if (world.getBiome(blockPos).temperatureCategory == TemperatureCategory.HIGH) 1.0 else 0.0 }),
    LOW_HUMIDITY("low_humidity", Emoji.LOW_HUMIDITY, "Low Humidity", "低い湿度", { world, blockPos -> if (world.getBiome(blockPos).humidityCategory == HumidityCategory.LOW) 1.0 else 0.0 }),
    MEDIUM_HUMIDITY("medium_humidity", Emoji.MEDIUM_HUMIDITY, "Medium Humidity", "普通の湿度", { world, blockPos -> if (world.getBiome(blockPos).humidityCategory == HumidityCategory.MEDIUM) 1.0 else 0.0 }),
    HIGH_HUMIDITY("high_humidity", Emoji.HIGH_HUMIDITY, "High Humidity", "高い湿度", { world, blockPos -> if (world.getBiome(blockPos).humidityCategory == HumidityCategory.HIGH) 1.0 else 0.0 }),
    OUTDOOR("outdoor", Emoji.OUTDOOR, "Outdoor", "屋外", { world, blockPos -> if (blockPos.y >= world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, blockPos).y) 1.0 else 0.0 }),
    NATURAL("natural", Emoji.NATURAL, "Natural", "天然", { world, blockPos -> if (world.getMagicPlantBlockEntity(blockPos)?.isNatural() == true) 1.0 else 0.0 }),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val translation = Translation({ identifier.toTranslationKey("${MirageFairy2024.modId}.trait_condition") }, enName, jaName)
    val traitCondition = object : TraitCondition {
        override val emoji = emoji()
        override val name = translation()
        override fun getFactor(world: World, blockPos: BlockPos) = function(world, blockPos)
    }
}

private fun getFloorHardness(world: World, blockPos: BlockPos): Double {
    val blockState = world.getBlockState(blockPos.down())
    if (!blockState.isIn(BlockTags.PICKAXE_MINEABLE)) return 0.0
    val hardness = blockState.getHardness(world, blockPos.down())
    if (hardness < 0) return 0.0
    return hardness / 2.0 atMost 2.0
}

context(ModContext)
fun initTraitConditionCard() {
    TraitConditionCard.entries.forEach {
        it.translation.enJa()
    }
}
