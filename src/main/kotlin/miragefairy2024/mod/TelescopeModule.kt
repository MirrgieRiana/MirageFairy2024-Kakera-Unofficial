package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.util.BlockStateVariant
import miragefairy2024.util.BlockStateVariantRotation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.long
import miragefairy2024.util.on
import miragefairy2024.util.propertiesOf
import miragefairy2024.util.register
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.MapColor
import net.minecraft.block.ShapeContext
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import java.time.Instant

object TelescopeCard {
    val identifier = MirageFairy2024.identifier("telescope")
    val block = TelescopeBlock(FabricBlockSettings.create().mapColor(MapColor.ORANGE).sounds(BlockSoundGroup.COPPER).strength(0.5F).nonOpaque())
    val item = BlockItem(block, Item.Settings())
}

context(ModContext)
fun initTelescopeModule() {

    TelescopeMissionExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, MirageFairy2024.identifier("telescope_mission"))

    TelescopeCard.let { card ->

        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration {
            val normal = BlockStateVariant(model = "block/" * card.block.getIdentifier())
            listOf(
                propertiesOf(HorizontalFacingBlock.FACING with Direction.NORTH) to normal.with(y = BlockStateVariantRotation.R0),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.EAST) to normal.with(y = BlockStateVariantRotation.R90),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.SOUTH) to normal.with(y = BlockStateVariantRotation.R180),
                propertiesOf(HorizontalFacingBlock.FACING with Direction.WEST) to normal.with(y = BlockStateVariantRotation.R270),
            )
        }
        card.block.registerCutoutRenderLayer()

        card.block.enJa("Minia's Telescope", "ミーニャの望遠鏡")
        val poemList = PoemList(2)
            .poem("Tell me more about the human world!", "きみは妖精には見えないものが見えるんだね。")
            .description("Use once a day to obtain Fairy Jewels", "1日1回使用時にフェアリージュエルを獲得")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        card.block.registerDefaultLootTableGeneration()

    }

    registerShapedRecipeGeneration(TelescopeCard.item) {
        pattern("IIG")
        pattern(" S ")
        pattern("S S")
        input('S', Items.STICK)
        input('I', Items.COPPER_INGOT)
        input('G', MaterialCard.FAIRY_CRYSTAL.item)
    } on MaterialCard.FAIRY_CRYSTAL.item

}

class TelescopeBlock(settings: Settings) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        private val FACING_TO_SHAPE: Map<Direction, VoxelShape> = mapOf(
            Direction.NORTH to createCuboidShape(4.0, 0.0, 1.0, 12.0, 16.0, 15.0),
            Direction.SOUTH to createCuboidShape(4.0, 0.0, 1.0, 12.0, 16.0, 15.0),
            Direction.WEST to createCuboidShape(1.0, 0.0, 4.0, 15.0, 16.0, 12.0),
            Direction.EAST to createCuboidShape(1.0, 0.0, 4.0, 15.0, 16.0, 12.0),
        )
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState = defaultState.with(FACING, ctx.horizontalPlayerFacing)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canPathfindThrough(state: BlockState, world: BlockView, pos: BlockPos, type: NavigationType?) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext) = FACING_TO_SHAPE[state.get(FACING)]

    // TODO 使用時効果

    // TODO パーティクル

}

val PlayerEntity.telescopeMission get() = this.extraPlayerDataContainer.getOrInit(TelescopeMissionExtraPlayerDataCategory)

object TelescopeMissionExtraPlayerDataCategory : ExtraPlayerDataCategory<TelescopeMission> {
    override fun create() = TelescopeMission()
    override fun castOrThrow(value: Any) = value as TelescopeMission
    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<TelescopeMission> {
        override fun fromNbt(nbt: NbtCompound): TelescopeMission {
            val data = TelescopeMission()
            data.lastUsedTime = nbt.wrapper["LastUsedTime"].long.get()
            return data
        }

        override fun toNbt(data: TelescopeMission): NbtCompound {
            val nbt = NbtCompound()
            nbt.wrapper["LastUsedTime"].long.set(data.lastUsedTime)
            return nbt
        }
    }
}

class TelescopeMission {
    var lastUsedTime: Long? = null
}

var TelescopeMission.lastUsedInstant
    get() = lastUsedTime?.let { Instant.ofEpochMilli(it) }
    set(value) {
        lastUsedTime = value?.toEpochMilli()
    }
