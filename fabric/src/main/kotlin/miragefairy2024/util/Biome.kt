package miragefairy2024.util

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.world.level.biome.Biome
import net.minecraft.core.Holder as RegistryEntry

enum class TemperatureCategory {
    HIGH,
    MEDIUM,
    LOW,
}

val RegistryEntry<Biome>.temperatureCategory
    get() = when {
        this.`is`(ConventionalBiomeTags.IS_HOT) -> TemperatureCategory.HIGH
        this.`is`(ConventionalBiomeTags.IS_COLD) -> TemperatureCategory.LOW
        this.`is`(ConventionalBiomeTags.IS_AQUATIC_ICY) -> TemperatureCategory.LOW
        else -> TemperatureCategory.MEDIUM
    }

enum class HumidityCategory {
    HIGH,
    MEDIUM,
    LOW,
}

val RegistryEntry<Biome>.humidityCategory
    get() = when {
        this.`is`(ConventionalBiomeTags.IS_WET) -> HumidityCategory.HIGH
        this.`is`(ConventionalBiomeTags.IS_DRY) -> HumidityCategory.LOW
        else -> HumidityCategory.MEDIUM
    }
