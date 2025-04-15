package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder as RegistryEntry
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth as MathHelper
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration as FeatureConfig
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext as FeatureContext

class FairyRingFeatureConfig(val tries: Int, val minRadius: Float, val maxRadius: Float, val ySpread: Int, val feature: RegistryEntry<PlacedFeature>) : FeatureConfig {
    companion object {
        val CODEC: Codec<FairyRingFeatureConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("tries").forGetter(FairyRingFeatureConfig::tries),
                Codec.FLOAT.fieldOf("min_radius").forGetter(FairyRingFeatureConfig::minRadius),
                Codec.FLOAT.fieldOf("max_radius").forGetter(FairyRingFeatureConfig::maxRadius),
                Codec.INT.fieldOf("y_spread").forGetter(FairyRingFeatureConfig::ySpread),
                PlacedFeature.REGISTRY_CODEC.fieldOf("feature").forGetter(FairyRingFeatureConfig::feature),
            ).apply(instance, ::FairyRingFeatureConfig)
        }
    }

    init {
        require(tries >= 0)
        require(minRadius >= 0F)
        require(maxRadius >= 0F)
        require(maxRadius >= minRadius)
        require(ySpread >= 0)
    }
}

class FairyRingFeature(codec: Codec<FairyRingFeatureConfig>) : Feature<FairyRingFeatureConfig>(codec) {
    override fun generate(context: FeatureContext<FairyRingFeatureConfig>): Boolean {
        val config = context.config
        val random = context.random
        val originBlockPos = context.origin
        val world = context.level()

        var count = 0
        val minRadius = config.minRadius
        val radiusRange = config.maxRadius - minRadius
        val y1 = config.ySpread + 1
        val mutableBlockPos = BlockPos.Mutable()
        for (l in 0 until config.tries) {
            val r = random.nextFloat() * radiusRange + minRadius
            val theta = random.nextFloat() * MathHelper.TWO_PI
            val x = MathHelper.floor(MathHelper.cos(theta) * r)
            val y = random.nextInt(y1) - random.nextInt(y1)
            val z = MathHelper.floor(MathHelper.sin(theta) * r)

            mutableBlockPos.set(originBlockPos, x, y, z)
            if (config.feature.value().generateUnregistered(world, context.generator, random, mutableBlockPos)) {
                count++
            }
        }

        return count > 0
    }
}
