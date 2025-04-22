package miragefairy2024.mod.structure

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.register
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureType
import java.util.Optional
import net.minecraft.core.Holder as RegistryEntry
import net.minecraft.util.ExtraCodecs as Codecs
import net.minecraft.world.level.levelgen.WorldGenerationContext as HeightContext
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment as StructureTerrainAdaptation
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement as StructurePoolBasedGenerator
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool as StructurePool

context(ModContext)
fun initUnlimitedJigsaw() {
    UnlimitedJigsawCard.let { card ->
        card.structureType.register(BuiltInRegistries.STRUCTURE_TYPE, card.identifier)
    }
}

object UnlimitedJigsawCard {
    val identifier = MirageFairy2024.identifier("unlimited_jigsaw")
    val structureType: StructureType<UnlimitedJigsawStructure> = StructureType { UnlimitedJigsawStructure.CODEC }
}

class UnlimitedJigsawStructure(
    config: StructureSettings,
    private val startPool: RegistryEntry<StructurePool>,
    private val startJigsawName: Optional<ResourceLocation> = Optional.empty(),
    private val size: Int,
    private val startHeight: HeightProvider,
    private val useExpansionHack: Boolean,
    private val projectStartToHeightmap: Optional<Heightmap.Types> = Optional.empty(),
    private val maxDistanceFromCenter: Int = 80,
) : Structure(config) {
    companion object {
        val CODEC: Codec<UnlimitedJigsawStructure> = Codecs.validate(RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                settingsCodec(instance),
                StructurePool.CODEC.fieldOf("start_pool").forGetter { it.startPool },
                ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter { it.startJigsawName },
                Codec.intRange(0, 256).fieldOf("size").forGetter { it.size },
                HeightProvider.CODEC.fieldOf("start_height").forGetter { it.startHeight },
                Codec.BOOL.fieldOf("use_expansion_hack").forGetter { it.useExpansionHack },
                Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter { it.projectStartToHeightmap },
                Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter { it.maxDistanceFromCenter },
            ).apply(instance, ::UnlimitedJigsawStructure)
        }, UnlimitedJigsawStructure::validate).codec()

        private fun validate(structure: UnlimitedJigsawStructure): DataResult<UnlimitedJigsawStructure> {
            val var10000 = when (structure.terrainAdaptation()) {
                StructureTerrainAdaptation.NONE -> 0
                StructureTerrainAdaptation.BURY, StructureTerrainAdaptation.BEARD_THIN, StructureTerrainAdaptation.BEARD_BOX -> 12
                else -> throw IncompatibleClassChangeError()
            }
            return if (structure.maxDistanceFromCenter + var10000 > 128) {
                DataResult.error { "Structure size including terrain adaptation must not exceed 128" }
            } else {
                DataResult.success(structure)
            }
        }
    }

    override fun findGenerationPoint(context: GenerationContext): Optional<GenerationStub> {
        val chunkPos = context.chunkPos()
        val i = startHeight.sample(context.random(), HeightContext(context.chunkGenerator(), context.heightAccessor()))
        val blockPos = BlockPos(chunkPos.minBlockX, i, chunkPos.minBlockZ)
        return StructurePoolBasedGenerator.addPieces(context, startPool, startJigsawName, size, blockPos, useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter)
    }

    override fun type(): StructureType<*> = UnlimitedJigsawCard.structureType
}
