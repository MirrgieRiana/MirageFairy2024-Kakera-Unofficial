package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.MachineScreenHandler
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.fairybuilding.FairyBuildingCard.Companion.PropertyConfiguration
import miragefairy2024.util.EnJa
import miragefairy2024.util.get
import miragefairy2024.util.int
import miragefairy2024.util.mergeInventory
import miragefairy2024.util.on
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.toInventoryDelegate
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object AuraReflectorFurnaceCard : SimpleMachineCard<AuraReflectorFurnaceBlock, AuraReflectorFurnaceBlockEntity, AuraReflectorFurnaceScreenHandler, AuraReflectorFurnaceRecipe>() {
    override fun createIdentifier() = MirageFairy2024.identifier("aura_reflector_furnace")
    override fun createBlockSettings(): FabricBlockSettings = FabricBlockSettings.create() // TODO
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

    val FUEL_PROPERTY = PropertyConfiguration<AuraReflectorFurnaceBlockEntity>({ fuel }, { fuel = it })
    val FUEL_MAX_PROPERTY = PropertyConfiguration<AuraReflectorFurnaceBlockEntity>({ fuelMax }, { fuelMax = it })
    override val properties = super.properties + FUEL_PROPERTY + FUEL_MAX_PROPERTY

    override val recipeType = AuraReflectorFurnaceRecipeCard.type

    context(ModContext)
    override fun init() {
        super.init()

        block.registerBlockTagGeneration { BlockTags.PICKAXE_MINEABLE }

        registerShapedRecipeGeneration(item) {
            pattern("XXX")
            pattern("XFX")
            pattern("XXX")
            input('F', Items.FURNACE)
            input('X', MaterialCard.XARPITE.item)
        } on MaterialCard.XARPITE.item
    }
}

class AuraReflectorFurnaceBlock(card: AuraReflectorFurnaceCard) : SimpleMachineBlock(card)

class AuraReflectorFurnaceBlockEntity(private val card: AuraReflectorFurnaceCard, pos: BlockPos, state: BlockState) : SimpleMachineBlockEntity<AuraReflectorFurnaceBlockEntity>(card, pos, state) {
    override fun getThis() = this

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        fuelMax = nbt.wrapper["FuelMax"].int.get() ?: 0
        fuel = nbt.wrapper["Fuel"].int.get() ?: 0
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.wrapper["FuelMax"].int.set(fuelMax)
        nbt.wrapper["Fuel"].int.set(fuel)
    }

    override fun markDirty() {
        super.markDirty()
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
            markDirty()
        }
    }

    override fun serverTick(world: World, pos: BlockPos, state: BlockState) {
        super.serverTick(world, pos, state)

        // クラフトが開始されていなければ、開始を試みる
        if (progressMax == 0) run {
            val onCrafted = mutableListOf<() -> Unit>()

            onCrafted += checkRecipe(world) ?: return@run
            if (fuel == 0) onCrafted += checkFuelInsert() ?: return@run

            onCrafted.forEach {
                it()
            }
        }

        // クラフトが開始されていれば、クラフトの進行を試みる
        if (progressMax > 0) {

            // クラフトが完了していなければ、プログレスの進行を試みる
            if (progress < progressMax) run success@{
                run fail@{

                    if (fuel == 0) {
                        val callback = checkFuelInsert() ?: return@fail
                        callback()
                    }

                    if (fuel == 0) return@fail

                    progress++
                    markDirty()
                    return@success
                }

                progress = 0
                markDirty()
            }

            // プログレスが完了していれば、クラフトの完了を試みる
            if (progress >= progressMax) {
                if (shouldUpdateWaiting) {
                    shouldUpdateWaiting = false

                    val result = mergeInventory(
                        waitingInventory.toInventoryDelegate(),
                        this.toInventoryDelegate(),
                        destIndices = card.outputSlots.map { card.inventorySlotIndexTable[it]!! },
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

        if (fuel > 0) fuel--

    }
}

class AuraReflectorFurnaceScreenHandler(card: AuraReflectorFurnaceCard, arguments: Arguments) : SimpleMachineScreenHandler(card, arguments) {
    var fuel by Property(AuraReflectorFurnaceCard.FUEL_PROPERTY)
    var fuelMax by Property(AuraReflectorFurnaceCard.FUEL_MAX_PROPERTY)
}
