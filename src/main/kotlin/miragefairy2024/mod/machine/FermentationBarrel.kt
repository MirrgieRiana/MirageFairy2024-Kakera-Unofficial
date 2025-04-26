package miragefairy2024.mod.machine

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument as Instrument

object FermentationBarrelCard : SimpleMachineCard<FermentationBarrelBlock, FermentationBarrelBlockEntity, FermentationBarrelScreenHandler, FermentationBarrelRecipe>() {
    override fun createIdentifier() = MirageFairy2024.identifier("fermentation_barrel")
    override fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).strength(3.0F).mapColor(MapColor.TERRACOTTA_ORANGE)
    override fun createBlock() = FermentationBarrelBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FermentationBarrelBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FermentationBarrelScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 152

    override val name = EnJa("Fermentation Barrel", "醸造樽")
    override val poem = EnJa("The scent of Haimeviska feel nostalgic", "懐かしき故郷の香り。")
    override val tier = 2

    override val inputSlots = listOf(
        SlotConfiguration(42, 17, setOf(Direction.UP), setOf()),
        SlotConfiguration(31, 39, setOf(Direction.NORTH), setOf()),
        SlotConfiguration(53, 39, setOf(Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN), setOf()),
    )
    override val outputSlots = listOf(
        SlotConfiguration(111, 28, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
        SlotConfiguration(129, 28, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
    )
    override val slots = inputSlots + outputSlots

    override val recipeType = FermentationBarrelRecipeCard.type

    context(ModContext)
    override fun init() {
        super.init()

        FermentationBarrelBlock.CODEC.register(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fermentation_barrel"))

        block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_AXE }

        registerShapedRecipeGeneration(item) {
            pattern("ILI")
            pattern("LRL")
            pattern("ILI")
            define('L', HaimeviskaBlockCard.LOG.item)
            define('R', MaterialCard.HAIMEVISKA_ROSIN.item)
            define('I', Items.IRON_NUGGET)
        } on MaterialCard.HAIMEVISKA_ROSIN.item
    }
}

class FermentationBarrelBlock(card: FermentationBarrelCard) : SimpleMachineBlock(card) {
    companion object {
        val CODEC: MapCodec<FermentationBarrelBlock> = simpleCodec { FermentationBarrelBlock(FermentationBarrelCard) }
    }

    override fun codec() = CODEC
}

class FermentationBarrelBlockEntity(card: FermentationBarrelCard, pos: BlockPos, state: BlockState) : SimpleMachineBlockEntity<FermentationBarrelBlockEntity>(card, pos, state) {
    override fun getThis() = this
}

class FermentationBarrelScreenHandler(card: FermentationBarrelCard, arguments: Arguments) : SimpleMachineScreenHandler(card, arguments)
