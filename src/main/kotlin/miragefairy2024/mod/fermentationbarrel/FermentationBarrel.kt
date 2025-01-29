package miragefairy2024.mod.fermentationbarrel

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.HorizontalFacingMachineBlock
import miragefairy2024.lib.MachineBlockEntity
import miragefairy2024.lib.MachineCard
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.fairybuilding.FairyBuildingCard.Companion.PropertyConfiguration
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.compound
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.int
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.normal
import miragefairy2024.util.on
import miragefairy2024.util.readFromNbt
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.reset
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.times
import miragefairy2024.util.toInventoryDelegate
import miragefairy2024.util.withHorizontalRotation
import miragefairy2024.util.wrapper
import miragefairy2024.util.writeToNbt
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.MapColor
import net.minecraft.block.enums.Instrument
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.tag.BlockTags
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import kotlin.jvm.optionals.getOrNull

object FermentationBarrelCard : MachineCard<FermentationBarrelBlock, FermentationBarrelBlockEntity, FermentationBarrelScreenHandler>() {
    override fun createIdentifier() = MirageFairy2024.identifier("fermentation_barrel")
    override fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create().instrument(Instrument.BASS).sounds(BlockSoundGroup.WOOD).strength(3.0F).mapColor(MapColor.TERRACOTTA_ORANGE)
    override fun createBlock() = FermentationBarrelBlock(this)
    override fun createBlockEntityAccessor() = BlockEntityAccessor(::FermentationBarrelBlockEntity)
    override fun createScreenHandler(arguments: MachineScreenHandler.Arguments) = FermentationBarrelScreenHandler(this, arguments)
    override val guiWidth = 176
    override val guiHeight = 152

    class SlotConfiguration(
        override val x: Int,
        override val y: Int,
        private val insertDirections: Set<Direction> = setOf(),
        private val extractDirections: Set<Direction> = setOf(),
    ) : MachineBlockEntity.InventorySlotConfiguration, MachineScreenHandler.GuiSlotConfiguration {
        override fun isValid(itemStack: ItemStack) = true
        override fun getTooltip() = null
        override fun canInsert(direction: Direction) = direction in insertDirections
        override fun canExtract(direction: Direction) = direction in extractDirections
        override val isObservable = false
        override val dropItem = true
    }

    val INPUT_SLOTS = listOf(
        SlotConfiguration(48, 17, setOf(Direction.UP), setOf()),
        SlotConfiguration(48, 39, setOf(Direction.NORTH), setOf()),
        SlotConfiguration(68, 39, setOf(Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN), setOf()),
    )
    val OUTPUT_SLOTS = listOf(
        SlotConfiguration(108, 28, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
        SlotConfiguration(128, 28, setOf(), setOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN)),
    )
    val SLOTS = INPUT_SLOTS + OUTPUT_SLOTS

    val PROGRESS_PROPERTY = PropertyConfiguration<FermentationBarrelBlockEntity>({ progress }, { progress = it })
    val PROGRESS_MAX_PROPERTY = PropertyConfiguration<FermentationBarrelBlockEntity>({ progressMax }, { progressMax = it })

    context(ModContext)
    override fun init() {
        super.init()

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        block.registerVariantsBlockStateGeneration { normal("block/" * block.getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }

        block.enJa(EnJa("Fermentation Barrel", "発酵樽"))
        val poemList = PoemList(2).poem(EnJa("The scent of Haimeviska feel nostalgic", "懐かしき故郷の香り。"))
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        block.registerBlockTagGeneration { BlockTags.AXE_MINEABLE }

        block.registerDefaultLootTableGeneration()


        inventorySlotConfigurations += SLOTS
        guiSlotConfigurations += SLOTS
        propertyConfigurations += listOf(PROGRESS_PROPERTY, PROGRESS_MAX_PROPERTY)


        registerShapedRecipeGeneration(item) {
            pattern("ILI")
            pattern("LRL")
            pattern("ILI")
            input('L', HaimeviskaBlockCard.LOG.item)
            input('R', MaterialCard.HAIMEVISKA_ROSIN.item)
            input('I', Items.IRON_NUGGET)
        } on MaterialCard.HAIMEVISKA_ROSIN.item
    }
}

class FermentationBarrelBlock(card: FermentationBarrelCard) : HorizontalFacingMachineBlock(card)

class FermentationBarrelBlockEntity(private val card: FermentationBarrelCard, pos: BlockPos, state: BlockState) : MachineBlockEntity<FermentationBarrelBlockEntity>(card, pos, state) {
    override fun getThis() = this

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        progress = nbt.wrapper["Progress"].int.get() ?: 0
        progressMax = nbt.wrapper["ProgressMax"].int.get() ?: 0
        craftingInventory.reset()
        nbt.wrapper["CraftingInventory"].compound.get()?.let { craftingInventory.readFromNbt(it) }
        waitingInventory.reset()
        nbt.wrapper["WaitingInventory"].compound.set(waitingInventory.writeToNbt())
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["Progress"].int.set(progress)
        nbt.wrapper["ProgressMax"].int.set(progressMax)
        nbt.wrapper["CraftingInventory"].compound.set(craftingInventory.writeToNbt())
        nbt.wrapper["WaitingInventory"].compound.set(waitingInventory.writeToNbt())
    }

    override fun markDirty() {
        super.markDirty()
        shouldUpdateRecipe = true
        shouldUpdateWaiting = true
    }

    override fun getActualSide(side: Direction) = HorizontalFacingMachineBlock.getActualSide(cachedState, side)

    private val craftingInventory = mutableListOf<ItemStack>()
    private val waitingInventory = mutableListOf<ItemStack>()

    override fun clear() {
        super.clear()
        craftingInventory.clear()
        waitingInventory.clear()
    }

    override fun dropItems() {
        super.dropItems()
        craftingInventory.forEach {
            ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it)
        }
    }

    private var shouldUpdateRecipe = true
    private var shouldUpdateWaiting = true
    var progressMax = 0
    var progress = 0

    override fun serverTick(world: World, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        if (progressMax == 0 && shouldUpdateRecipe) run {
            shouldUpdateRecipe = false

            val inventory = SimpleInventory(FermentationBarrelCard.INPUT_SLOTS.size)
            FermentationBarrelCard.INPUT_SLOTS.forEachIndexed { index, slot ->
                inventory[index] = getStack(card.inventorySlotIndexTable[slot]!!)
            }

            val recipe = world.recipeManager.getFirstMatch(FermentationBarrelRecipe.TYPE, inventory, world).getOrNull() ?: return@run

            val remainder = recipe.getRemainder(inventory)
            (0 until inventory.size).forEach { index ->
                craftingInventory += inventory[index].split(recipe.inputs[index].second)
            }
            waitingInventory += recipe.output.copy()
            waitingInventory += remainder
            progressMax = recipe.duration
            markDirty()
        }

        if (progressMax > 0) {

            if (progress < progressMax) {
                progress++
                markDirty()
            }

            if (progress >= progressMax) {
                if (shouldUpdateWaiting) {
                    shouldUpdateWaiting = false

                    val result = mergeInventory(
                        waitingInventory.toInventoryDelegate(),
                        this.toInventoryDelegate(),
                        destIndices = FermentationBarrelCard.OUTPUT_SLOTS.map { card.inventorySlotIndexTable[it]!! },
                    )
                    if (result.movementTimes > 0) markDirty()
                    if (result.completed) {
                        progress = 0
                        progressMax = 0
                        craftingInventory.clear()
                        markDirty()
                    }

                }
            }

        }

    }
}

class FermentationBarrelScreenHandler(card: FermentationBarrelCard, arguments: Arguments) : MachineScreenHandler(card, arguments) {
    var progress by Property(FermentationBarrelCard.PROGRESS_PROPERTY)
    var progressMax by Property(FermentationBarrelCard.PROGRESS_MAX_PROPERTY)
}
