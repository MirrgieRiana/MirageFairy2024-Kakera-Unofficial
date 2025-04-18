package miragefairy2024.util

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.core.Holder as RegistryEntry
import net.minecraft.world.level.biome.Biome

enum class TemperatureCategory {
    HIGH,
    MEDIUM,
    LOW,
}

val RegistryEntry<Biome>.temperatureCategory
    get() = when {
        this.`is`(ConventionalBiomeTags.CLIMATE_HOT) -> TemperatureCategory.HIGH
        this.`is`(ConventionalBiomeTags.CLIMATE_COLD) -> TemperatureCategory.LOW
        this.`is`(ConventionalBiomeTags.AQUATIC_ICY) -> TemperatureCategory.LOW
        else -> TemperatureCategory.MEDIUM
    }

enum class HumidityCategory {
    HIGH,
    MEDIUM,
    LOW,
}

val RegistryEntry<Biome>.humidityCategory
    get() = when {
        this.`is`(ConventionalBiomeTags.CLIMATE_WET) -> HumidityCategory.HIGH
        this.`is`(ConventionalBiomeTags.CLIMATE_DRY) -> HumidityCategory.LOW
        else -> HumidityCategory.MEDIUM
    }
