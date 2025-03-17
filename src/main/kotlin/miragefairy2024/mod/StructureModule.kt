package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.register
import net.minecraft.registry.Registries
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.structure.pool.StructurePool
import net.minecraft.structure.pool.StructurePoolBasedGenerator
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.Codecs
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.HeightContext
import net.minecraft.world.gen.StructureTerrainAdaptation
import net.minecraft.world.gen.heightprovider.HeightProvider
import net.minecraft.world.gen.structure.Structure
import net.minecraft.world.gen.structure.StructureType
import java.util.Optional

context(ModContext)
fun initStructureModule() {
    initUnlimitedJigsaw()
}


context(ModContext)
fun initUnlimitedJigsaw() {
    UnlimitedJigsawCard.let { card ->
        card.structureType.register(Registries.STRUCTURE_TYPE, card.identifier)
    }
}

object UnlimitedJigsawCard {
    val identifier = MirageFairy2024.identifier("unlimited_jigsaw")
    val structureType: StructureType<UnlimitedJigsawStructure> = StructureType { UnlimitedJigsawStructure.CODEC }
}

class UnlimitedJigsawStructure(
    config: Config,
    private val startPool: RegistryEntry<StructurePool>,
    private val startJigsawName: Optional<Identifier> = Optional.empty(),
    private val size: Int,
    private val startHeight: HeightProvider,
    private val useExpansionHack: Boolean,
    private val projectStartToHeightmap: Optional<Heightmap.Type> = Optional.empty(),
    private val maxDistanceFromCenter: Int = 80,
) : Structure(config) {
    companion object {
        val CODEC: Codec<UnlimitedJigsawStructure> = Codecs.validate(RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                configCodecBuilder(instance),
                StructurePool.REGISTRY_CODEC.fieldOf("start_pool").forGetter { it.startPool },
                Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter { it.startJigsawName },
                Codec.intRange(0, 256).fieldOf("size").forGetter { it.size },
                HeightProvider.CODEC.fieldOf("start_height").forGetter { it.startHeight },
                Codec.BOOL.fieldOf("use_expansion_hack").forGetter { it.useExpansionHack },
                Heightmap.Type.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter { it.projectStartToHeightmap },
                Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter { it.maxDistanceFromCenter },
            ).apply(instance, ::UnlimitedJigsawStructure)
        }, UnlimitedJigsawStructure::validate).codec()

        private fun validate(structure: UnlimitedJigsawStructure): DataResult<UnlimitedJigsawStructure> {
            val var10000 = when (structure.terrainAdaptation) {
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

    override fun getStructurePosition(context: Context): Optional<StructurePosition> {
        val chunkPos = context.chunkPos()
        val i = startHeight.get(context.random(), HeightContext(context.chunkGenerator(), context.world()))
        val blockPos = BlockPos(chunkPos.startX, i, chunkPos.startZ)
        return StructurePoolBasedGenerator.generate(context, startPool, startJigsawName, size, blockPos, useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter)
    }

    override fun getType(): StructureType<*> = UnlimitedJigsawCard.structureType
}
