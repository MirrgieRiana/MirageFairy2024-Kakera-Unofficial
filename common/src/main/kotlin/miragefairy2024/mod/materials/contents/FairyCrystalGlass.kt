package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.string
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.PipeBlock
import net.minecraft.world.level.block.TransparentBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.data.models.model.TextureSlot as TextureKey

class FairyCrystalGlassBlock(properties: Properties) : TransparentBlock(properties) {
    companion object {
        val CODEC: MapCodec<FairyCrystalGlassBlock> = simpleCodec(::FairyCrystalGlassBlock)
    }

    override fun codec() = CODEC

    init {
        registerDefaultState(
            defaultBlockState()
                .setValue(BlockStateProperties.NORTH, false)
                .setValue(BlockStateProperties.EAST, false)
                .setValue(BlockStateProperties.SOUTH, false)
                .setValue(BlockStateProperties.WEST, false)
                .setValue(BlockStateProperties.UP, false)
                .setValue(BlockStateProperties.DOWN, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(
            BlockStateProperties.NORTH,
            BlockStateProperties.EAST,
            BlockStateProperties.SOUTH,
            BlockStateProperties.WEST,
            BlockStateProperties.UP,
            BlockStateProperties.DOWN,
        )
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        return defaultBlockState()
            .setValue(BlockStateProperties.NORTH, ctx.level.getBlockState(ctx.clickedPos.north()).`is`(this))
            .setValue(BlockStateProperties.EAST, ctx.level.getBlockState(ctx.clickedPos.east()).`is`(this))
            .setValue(BlockStateProperties.SOUTH, ctx.level.getBlockState(ctx.clickedPos.south()).`is`(this))
            .setValue(BlockStateProperties.WEST, ctx.level.getBlockState(ctx.clickedPos.west()).`is`(this))
            .setValue(BlockStateProperties.UP, ctx.level.getBlockState(ctx.clickedPos.above()).`is`(this))
            .setValue(BlockStateProperties.DOWN, ctx.level.getBlockState(ctx.clickedPos.below()).`is`(this))
    }

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION[direction]!!, neighborState.`is`(this))
    }
}

val fairyCrystalGlassFrameBlockModel = Model { textureMap ->
    ModelData(
        parent = ResourceLocation.withDefaultNamespace("block/block"),
        textures = ModelTexturesData(
            TextureKey.PARTICLE.id to textureMap.get(TextureKey.TEXTURE).string,
            TextureKey.TEXTURE.id to textureMap.get(TextureKey.TEXTURE).string,
        ),
        elements = ModelElementsData(
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    north = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "north"),
                    south = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "south"),
                    west = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "west"),
                    east = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "east"),
                ),
            ),
        ),
    )
}

val fairyCrystalGlassBlockModel = Model { textureMap ->
    fun createPart(rotation: Int) = ModelElementData(
        from = listOf(0, 0, 0),
        to = listOf(16, 16, 16),
        faces = ModelFacesData(
            north = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "north", rotation = rotation),
            south = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "south", rotation = rotation),
            west = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "west", rotation = rotation),
            east = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "east", rotation = rotation),
            up = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "up", rotation = rotation),
            down = ModelFaceData(texture = TextureKey.TEXTURE.string, cullface = "down", rotation = rotation),
        ),
    )
    ModelData(
        parent = ResourceLocation.withDefaultNamespace("block/block"),
        textures = ModelTexturesData(
            TextureKey.PARTICLE.id to textureMap.get(TextureKey.TEXTURE).string,
            TextureKey.TEXTURE.id to textureMap.get(TextureKey.TEXTURE).string,
        ),
        elements = ModelElementsData(
            createPart(0),
            createPart(90),
            createPart(180),
            createPart(270),
        ),
    )
}
