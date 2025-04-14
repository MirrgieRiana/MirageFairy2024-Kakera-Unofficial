package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairybuilding.FairyBuildingCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.TextureMap
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.int
import miragefairy2024.util.normal
import miragefairy2024.util.on
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.StateDefinition as StateManager
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level as World

object AuraReflectorFurnaceCard : SimpleMachineCard<AuraReflectorFurnaceBlock, AuraReflectorFurnaceBlockEntity, AuraReflectorFurnaceScreenHandler, AuraReflectorFurnaceRecipe>() {
    override fun createIdentifier() = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().mapColor(MapColor.DARK_RED).requiresTool().strength(3.0F).luminance(Blocks.createLightLevelFromLitBlockState(8))
    override fun createBlock() = AuraReflectorFurnaceBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::AuraReflectorFurnaceBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = AuraReflectorFurnaceScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 166

    override val name = EnJa("Aura Reflector Furnace", "オーラ反射炉")
    override val poem = EnJa("Life is essentially inorganic.", "生命と無機物の境界。")
    override val tier = 2

    override val inputSlots = listOf(
        SlotConfiguration(29, 17, setOf(Direction.EAST), setOf()),
        SlotConfiguration(47, 17, setOf(Direction.UP, Direction.NORTH, Direction.DOWN), setOf()),
        SlotConfiguration(65, 17, setOf(Direction.WEST), setOf()),
    )
    val fuelSlot = object : SlotConfiguration(47, 53, setOf(Direction.SOUTH), setOf()) {
        override fun isValid(itemStack: ItemStack) = AuraReflectorFurnaceRecipe.FUELS.contains(itemStack.item)
    }
    override val outputSlots = listOf(
        SlotConfiguration(123, 35, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
    )
    override val slots = inputSlots + fuelSlot + outputSlots

    val FUEL_PROPERTY = FairyBuildingCard.PropertyConfiguration<AuraReflectorFurnaceBlockEntity>({ fuel }, { fuel = it })
    val FUEL_MAX_PROPERTY = FairyBuildingCard.PropertyConfiguration<AuraReflectorFurnaceBlockEntity>({ fuelMax }, { fuelMax = it })
    override val properties = super.properties + FUEL_PROPERTY + FUEL_MAX_PROPERTY

    override val recipeType = AuraReflectorFurnaceRecipeCard.type

    context(ModContext)
    override fun init() {
        super.init()

        registerModelGeneration({ "block/" * identifier * "_lit" }) { Model("block/" * identifier, TextureKey.FRONT) with TextureMap(TextureKey.FRONT to "block/" * identifier * "_front_lit") }

        block.registerBlockTagGeneration { BlockTags.MINEABLE_WITH_PICKAXE }

        registerShapedRecipeGeneration(item) {
            pattern("XXX")
            pattern("XFX")
            pattern("XXX")
            define('F', Items.FURNACE)
            define('X', MaterialCard.XARPITE.item)
        } on MaterialCard.XARPITE.item
    }

    context(ModContext)
    override fun registerBlockStateGeneration() {
        block.registerVariantsBlockStateGeneration {
            normal("block/" * block.getIdentifier())
                .withHorizontalRotation(HorizontalFacingBlock.FACING)
                .with(AuraReflectorFurnaceBlock.LIT) { model, entry -> if (entry.value) model * "_lit" else model }
        }
    }
}

class AuraReflectorFurnaceBlock(card: AuraReflectorFurnaceCard) : SimpleMachineBlock(card) {
    companion object {
        val LIT: BooleanProperty = BlockStateProperties.LIT
    }

    init {
        registerDefaultState(defaultBlockState().setValue(LIT, false))
    }

    override fun createBlockStateDefinition(builder: StateManager.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(LIT)
    }
}

class AuraReflectorFurnaceBlockEntity(private val card: AuraReflectorFurnaceCard, pos: BlockPos, state: BlockState) : SimpleMachineBlockEntity<AuraReflectorFurnaceBlockEntity>(card, pos, state) {
    override fun getThis() = this

    override fun load(nbt: NbtCompound) {
        super.load(nbt)
        fuelMax = nbt.wrapper["FuelMax"].int.get() ?: 0
        fuel = nbt.wrapper["Fuel"].int.get() ?: 0
    }

    override fun saveAdditional(nbt: NbtCompound) {
        super.saveAdditional(nbt)
        nbt.wrapper["FuelMax"].int.set(fuelMax)
        nbt.wrapper["Fuel"].int.set(fuel)
    }

    override fun setChanged() {
        super.setChanged()
        shouldUpdateFuel = true
    }

    var shouldUpdateFuel = true
    var fuelMax = 0
    var fuel = 0

    fun checkFuelInsert(): (() -> Unit)? {
        if (!shouldUpdateFuel) return null
        shouldUpdateFuel = false

        val fuelItemStack = this[card.inventorySlotIndexTable[card.fuelSlot]!!]
        if (!(AuraReflectorFurnaceRecipe.FUELS.contains(fuelItemStack.item) && fuelItemStack.count >= 1)) return null

        return {
            fuelItemStack.decrement(1)
            fuelMax = 20 * 10
            fuel = fuelMax
            setChanged()
        }
    }

    fun setLit(lit: Boolean) {
        val world = world ?: return
        if (cachedState[AuraReflectorFurnaceBlock.LIT] != lit) {
            world.setBlock(pos, cachedState.setValue(AuraReflectorFurnaceBlock.LIT, lit), Block.UPDATE_ALL)
        }
    }

    override fun onRecipeCheck(world: World, pos: BlockPos, state: BlockState, listeners: MutableList<() -> Unit>): Boolean {
        if (!super.onRecipeCheck(world, pos, state, listeners)) return false
        if (fuel == 0) listeners += checkFuelInsert() ?: return false
        return true
    }

    override fun onCraftingTick(world: World, pos: BlockPos, state: BlockState, listeners: MutableList<() -> Unit>): Boolean {
        if (!super.onCraftingTick(world, pos, state, listeners)) return false
        if (fuel == 0) listeners += checkFuelInsert() ?: return false
        return true
    }

    override fun onPostServerTick(world: World, pos: BlockPos, state: BlockState) {
        super.onPostServerTick(world, pos, state)
        val oldFuel = fuel
        if (fuel > 0) fuel--
        setLit(oldFuel > 0)
    }
}

class AuraReflectorFurnaceScreenHandler(card: AuraReflectorFurnaceCard, arguments: Arguments) : SimpleMachineScreenHandler(card, arguments) {
    var fuel by Property(AuraReflectorFurnaceCard.FUEL_PROPERTY)
    var fuelMax by Property(AuraReflectorFurnaceCard.FUEL_MAX_PROPERTY)
}
