package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.insertItem
import miragefairy2024.util.inventoryAccessor
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.itemStacks
import miragefairy2024.util.mergeTo
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.set
import miragefairy2024.util.text
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.inventory.StackReference
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ClickType
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import kotlin.math.roundToInt

object SeedBagCard {
    val identifier = MirageFairy2024.identifier("seed_bag")
    val item = SeedBagItem(Item.Settings().maxCount(1))
    val screenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
        val slotIndex = buf.readInt()
        SeedBagScreenHandler(syncId, playerInventory, slotIndex)
    }
}


context(ModContext)
fun initSeedBag() {
    SeedBagCard.let { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        card.item.registerGeneratedModelGeneration()
        card.item.enJa("Seed Bag", "種子カバン")
        val poemList = PoemList(1)
            .poem("", "") // TODO
            .description("Can store magic plant seeds", "魔法植物の種子を格納可能")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        card.screenHandlerType.register(Registries.SCREEN_HANDLER, card.identifier)
    }

    registerShapedRecipeGeneration(SeedBagCard.item) {
        pattern(" S ")
        pattern("L L")
        pattern("LLL")
        input('S', MaterialCard.MIRAGE_STEM.item)
        input('L', MaterialCard.MIRAGE_LEAVES.item)
    } on MaterialCard.MIRAGE_LEAVES.item
}


class SeedBagItem(settings: Settings) : Item(settings) {
    companion object {
        const val INVENTORY_WIDTH = 17
        const val INVENTORY_SIZE = INVENTORY_WIDTH * 6
    }

    override fun getName(stack: ItemStack): Text {
        val count = stack.getSeedBagInventory().itemStacks.count { it.isNotEmpty }
        return text { super.getName(stack) + (if (count > 0) " ($count / ${INVENTORY_SIZE})"() else ""()) }
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)

        val inventory = stack.getSeedBagInventory()
        var first = true
        var itemCount = 0
        inventory.itemStacks.forEach { itemStack ->
            if (itemStack.isNotEmpty) {
                itemCount++
                if (itemCount <= 10) {
                    if (first) {
                        first = false
                        tooltip += text { ""() }
                    }
                    tooltip += text { itemStack.name + (if (itemStack.count > 1) " x ${itemStack.count}"() else ""()) }
                }
            }
        }
        if (itemCount > 10) tooltip += text { "... ${itemCount - 10}"() }
    }

    override fun isItemBarVisible(stack: ItemStack): Boolean {
        val count = stack.getSeedBagInventory().itemStacks.count { it.isNotEmpty }
        return count > 0
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        val count = stack.getSeedBagInventory().itemStacks.count { it.isNotEmpty }
        return (13.0 * count.toDouble() / INVENTORY_SIZE.toDouble()).roundToInt()
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        val count = stack.getSeedBagInventory().itemStacks.count { it.isNotEmpty }
        return if (count >= INVENTORY_SIZE) 0xFF0000 else 0x00FF00
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        if (world.isClient) return TypedActionResult.success(itemStack)
        val slotIndex = if (hand == Hand.MAIN_HAND) {
            val selectedSlot = user.inventory.selectedSlot
            if (!PlayerInventory.isValidHotbarIndex(selectedSlot)) return TypedActionResult.fail(itemStack)
            selectedSlot
        } else {
            -1
        }
        user.openHandledScreen(object : ExtendedScreenHandlerFactory {
            override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return SeedBagScreenHandler(syncId, playerInventory, slotIndex)
            }

            override fun getDisplayName() = itemStack.name

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeInt(slotIndex)
            }
        })
        return TypedActionResult.consume(itemStack)
    }

    // カバンを持って種子のスロットを右クリックした場合の処理
    // slot = 種子のスロット（操作後、マージが完了した場合は除去、そうでない場合は部分的除去が行われる）
    // カバンは常にカーソルが保持しているので、アイテムの入出力制限は無い
    override fun onStackClicked(stack: ItemStack, slot: Slot, clickType: ClickType, player: PlayerEntity): Boolean {
        if (clickType != ClickType.RIGHT) return false

        if (!slot.canTakeItems(player)) return false // そもそもスロットからアイテムを回収できない場合はキャンセル

        // シミュレーション用のインベントリを作成
        val srcInventory = SimpleInventory(1)
        srcInventory[0] = slot.stack.copy()
        val destInventory = stack.getSeedBagInventory()

        // マージをシミュレートする
        val result = srcInventory.mergeTo(destInventory)

        if (!result.completed && !slot.canTakePartial(player)) return false // マージが半端である場合、スロットが部分的な回収を受け付けない場合はキャンセル

        // 成功

        if (result.movementTimes > 0) {
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + player.world.getRandom().nextFloat() * 0.4F)
        }

        // シミュレートした結果を適用する
        slot.stack = srcInventory[0]
        stack.setSeedBagInventory(destInventory)

        return true
    }

    // 種子を持ってカバンに突っ込んだ場合の処理
    // slot = カバンのスロット（操作後、変更が行われる）
    // 種子は常にカーソルが保持しているので、アイテムの入出力制限は無い
    override fun onClicked(stack: ItemStack, otherStack: ItemStack, slot: Slot, clickType: ClickType, player: PlayerEntity, cursorStackReference: StackReference): Boolean {
        if (clickType != ClickType.RIGHT) return false

        if (!slot.canTakePartial(player)) return false // そもそもカバンのスロットが変更を受け付けない場合はキャンセル

        // シミュレーション用のインベントリを作成
        val srcInventory = SimpleInventory(1)
        srcInventory[0] = cursorStackReference.get().copy()
        val destInventory = stack.getSeedBagInventory()

        // マージをシミュレートする
        val result = srcInventory.mergeTo(destInventory)

        // 成功

        if (result.movementTimes > 0) {
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + player.world.getRandom().nextFloat() * 0.4F)
        }

        // シミュレートした結果を適用する
        cursorStackReference.set(srcInventory[0])
        stack.setSeedBagInventory(destInventory)

        return true
    }

    override fun canBeNested() = false

    override fun onItemEntityDestroyed(entity: ItemEntity) {
        val world = entity.world
        if (world.isClient) return
        entity.stack.getSeedBagInventory().stacks.forEach { itemStack ->
            world.spawnEntity(ItemEntity(world, entity.x, entity.y, entity.z, itemStack))
        }
    }

}

class SeedBagInventory : SimpleInventory(SeedBagItem.INVENTORY_SIZE) {
    override fun isValid(slot: Int, stack: ItemStack) = stack.item is MagicPlantSeedItem && stack.item.canBeNested()
}

fun ItemStack.getSeedBagInventory(): SeedBagInventory {
    val inventory = SeedBagInventory()
    val nbt = this.nbt
    if (nbt != null) Inventories.readNbt(nbt, inventory.stacks)
    return inventory
}

fun ItemStack.setSeedBagInventory(inventory: SeedBagInventory) {
    val nbt = getOrCreateNbt()
    Inventories.writeNbt(nbt, inventory.stacks, false)
}


class SeedBagScreenHandler(syncId: Int, private val playerInventory: PlayerInventory, private val slotIndex: Int) : ScreenHandler(SeedBagCard.screenHandlerType, syncId) {
    private val itemStackInstance = if (slotIndex >= 0) playerInventory.main[slotIndex] else playerInventory.offHand[0]
    private val seedBagInventory = itemStackInstance.getSeedBagInventory()
    private val inventoryDelegate = object : Inventory {
        override fun clear() = seedBagInventory.clear()
        override fun size() = seedBagInventory.size()
        override fun isEmpty() = seedBagInventory.isEmpty()
        override fun getStack(slot: Int) = seedBagInventory.getStack(slot)
        override fun removeStack(slot: Int, amount: Int) = seedBagInventory.removeStack(slot, amount)
        override fun removeStack(slot: Int) = seedBagInventory.removeStack(slot)
        override fun setStack(slot: Int, stack: ItemStack) = seedBagInventory.setStack(slot, stack)
        override fun getMaxCountPerStack() = seedBagInventory.maxCountPerStack
        override fun markDirty() {
            seedBagInventory.markDirty()
            itemStackInstance.setSeedBagInventory(seedBagInventory)
        }

        override fun canPlayerUse(player: PlayerEntity) = seedBagInventory.canPlayerUse(player)
        override fun onOpen(player: PlayerEntity) = seedBagInventory.onOpen(player)
        override fun onClose(player: PlayerEntity) = seedBagInventory.onClose(player)
        override fun isValid(slot: Int, stack: ItemStack) = seedBagInventory.isValid(slot, stack)
        override fun canTransferTo(hopperInventory: Inventory, slot: Int, stack: ItemStack) = seedBagInventory.canTransferTo(hopperInventory, slot, stack)
    }

    init {
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(playerInventory, c, 0, 0))
        }
        repeat(SeedBagItem.INVENTORY_SIZE) { i ->
            addSlot(object : Slot(inventoryDelegate, i, 0, 0) {
                override fun canInsert(stack: ItemStack) = inventoryDelegate.isValid(i, stack)
            })
        }
    }

    override fun canUse(player: PlayerEntity) = (if (slotIndex >= 0) playerInventory.main[slotIndex] else playerInventory.offHand[0]) === itemStackInstance

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        if (slot < 0 || slot >= slots.size) return EMPTY_ITEM_STACK
        if (!slots[slot].hasStack()) return EMPTY_ITEM_STACK // そこに何も無い場合は何もしない

        val newItemStack = slots[slot].stack
        val originalItemStack = newItemStack.copy()

        if (slot < 9 * 4) { // 上へ
            if (!inventoryAccessor.insertItem(newItemStack, 9 * 4 until slots.size)) return EMPTY_ITEM_STACK
        } else { // 下へ
            if (!inventoryAccessor.insertItem(newItemStack, 9 * 4 - 1 downTo 0)) return EMPTY_ITEM_STACK
        }
        slots[slot].onQuickTransfer(newItemStack, originalItemStack)

        // 終了処理
        if (newItemStack.isEmpty) {
            slots[slot].stack = EMPTY_ITEM_STACK
        } else {
            slots[slot].markDirty()
        }

        return originalItemStack
    }
}
