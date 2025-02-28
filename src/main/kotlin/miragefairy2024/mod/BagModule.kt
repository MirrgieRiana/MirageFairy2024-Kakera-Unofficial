package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MagicPlantSeedItem
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.EnJa
import miragefairy2024.util.FilteringSlot
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.hasSameItemAndNbtAndCount
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.itemStacks
import miragefairy2024.util.mergeTo
import miragefairy2024.util.on
import miragefairy2024.util.plus
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.set
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.castOrNull
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
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
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

enum class BagCard(
    path: String,
    val itemName: EnJa,
    val tier: Int,
    val poem: EnJa,
    val inventoryWidth: Int,
    val inventoryHeight: Int,
    private val filter: (ItemStack) -> Boolean,
) {
    PLANT_BAG(
        "plant_bag", EnJa("Plant Bag", "植物カバン"),
        1, EnJa("Basket wall composed of uneven stems", "人間が手掛ける、初級レベルの藁細工。"),
        5, 3,
        { it.item.castOrNull<BlockItem>()?.block?.registryEntry?.isIn(BlockTags.SWORD_EFFICIENT) == true },
    ),
    SEED_BAG(
        "seed_bag", EnJa("Seed Bag", "種子カバン"),
        3, EnJa("Maintains the freshness of plants", "両手に、花。"),
        17, 6,
        { it.item is MagicPlantSeedItem },
    ),
    ;

    companion object {
        val screenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
            val slotIndex = buf.readInt()
            createBagScreenHandler(syncId, playerInventory, slotIndex)
        }

        val DESCRIPTION1_TRANSLATION = Translation({ MirageFairy2024.identifier("bag").toTranslationKey("item", "description1") }, "Display GUI when used", "使用時、GUIを表示")
        val DESCRIPTION2_TRANSLATION = Translation({ MirageFairy2024.identifier("bag").toTranslationKey("item", "description2") }, "Store to inventory when right-clicked", "インベントリ上で右クリックで収納")
    }

    val identifier = MirageFairy2024.identifier(path)
    val item = BagItem(this, Item.Settings().maxCount(1))
    val inventorySize = inventoryWidth * inventoryHeight
    fun isValid(itemStack: ItemStack) = filter(itemStack)
}


context(ModContext)
fun initBagModule() {
    BagCard.entries.forEach { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        card.item.registerGeneratedModelGeneration()
        card.item.enJa(card.itemName)
        val poemList = PoemList(card.tier)
            .poem(card.poem)
            .translation(PoemType.DESCRIPTION, BagCard.DESCRIPTION1_TRANSLATION)
            .translation(PoemType.DESCRIPTION, BagCard.DESCRIPTION2_TRANSLATION)
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)
    }


    BagCard.screenHandlerType.register(Registries.SCREEN_HANDLER, MirageFairy2024.identifier("bag"))

    BagCard.DESCRIPTION1_TRANSLATION.enJa()
    BagCard.DESCRIPTION2_TRANSLATION.enJa()


    registerShapedRecipeGeneration(BagCard.PLANT_BAG.item) {
        pattern(" S ")
        pattern("L L")
        pattern("LLL")
        input('S', MaterialCard.FAIRY_GLASS_FIBER.item)
        input('L', MaterialCard.MIRAGE_LEAVES.item)
    } on MaterialCard.MIRAGE_LEAVES.item

    registerShapedRecipeGeneration(BagCard.SEED_BAG.item) {
        pattern(" S ")
        pattern("L L")
        pattern("LLL")
        input('S', MaterialCard.FAIRY_GLASS_FIBER.item)
        input('L', MaterialCard.PHANTOM_LEAVES.item)
    } on MaterialCard.PHANTOM_LEAVES.item
}


class BagItem(val card: BagCard, settings: Settings) : Item(settings) {

    override fun getName(stack: ItemStack): Text {
        val bagInventory = stack.getBagInventory() ?: return super.getName(stack)
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return text { super.getName(stack) + (if (count > 0) " ($count / ${card.inventorySize})"() else ""()) }
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)

        val inventory = stack.getBagInventory() ?: return
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
        val bagInventory = stack.getBagInventory() ?: return false
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return count > 0
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        val bagInventory = stack.getBagInventory() ?: return 0
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return (13.0 * count.toDouble() / card.inventorySize.toDouble()).roundToInt()
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        val bagInventory = stack.getBagInventory() ?: return 0
        val count = bagInventory.itemStacks.count { it.isNotEmpty }
        return if (count >= card.inventorySize) 0xFF0000 else 0x00FF00
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
                return createBagScreenHandler(syncId, playerInventory, slotIndex)
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
        val destInventory = stack.getBagInventory() ?: return false

        // マージをシミュレートする
        val result = srcInventory.mergeTo(destInventory)

        if (!result.completed && !slot.canTakePartial(player)) return false // マージが半端である場合、スロットが部分的な回収を受け付けない場合はキャンセル

        // 成功

        if (result.movementTimes > 0) {
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + player.world.getRandom().nextFloat() * 0.4F)
        }

        // シミュレートした結果を適用する
        slot.stack = srcInventory[0]
        stack.setBagInventory(destInventory)

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
        val destInventory = stack.getBagInventory() ?: return false

        // マージをシミュレートする
        val result = srcInventory.mergeTo(destInventory)

        // 成功

        if (result.movementTimes > 0) {
            player.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + player.world.getRandom().nextFloat() * 0.4F)
        }

        // シミュレートした結果を適用する
        cursorStackReference.set(srcInventory[0])
        stack.setBagInventory(destInventory)

        return true
    }

    override fun canBeNested() = false

    override fun onItemEntityDestroyed(entity: ItemEntity) {
        val world = entity.world
        if (world.isClient) return
        val bagInventory = entity.stack.getBagInventory() ?: return
        bagInventory.stacks.forEach { itemStack ->
            world.spawnEntity(ItemEntity(world, entity.x, entity.y, entity.z, itemStack))
        }
    }

}

class BagInventory(private val card: BagCard) : SimpleInventory(card.inventorySize) {
    override fun isValid(slot: Int, stack: ItemStack) = card.isValid(stack) && stack.item.canBeNested()
}

fun ItemStack.getBagInventory(): BagInventory? {
    val item = this.item as? BagItem ?: return null
    val inventory = BagInventory(item.card)
    val nbt = this.nbt
    if (nbt != null) Inventories.readNbt(nbt, inventory.stacks)
    return inventory
}

fun ItemStack.setBagInventory(inventory: BagInventory) {
    val nbt = getOrCreateNbt()
    Inventories.writeNbt(nbt, inventory.stacks, true)
}


fun createBagScreenHandler(syncId: Int, playerInventory: PlayerInventory, slotIndex: Int): BagScreenHandler {
    val itemStackInstance = if (slotIndex == -1) playerInventory.offHand[0] else playerInventory.main[slotIndex]
    var expectedItemStack = itemStackInstance.copy()
    val item = itemStackInstance.item as? BagItem ?: return BagScreenHandler(syncId)
    val bagInventory = itemStackInstance.getBagInventory() ?: return BagScreenHandler(syncId)
    val inventoryDelegate = object : Inventory {
        override fun clear() = bagInventory.clear()
        override fun size() = bagInventory.size()
        override fun isEmpty() = bagInventory.isEmpty()
        override fun getStack(slot: Int) = bagInventory.getStack(slot)
        override fun removeStack(slot: Int, amount: Int) = bagInventory.removeStack(slot, amount)
        override fun removeStack(slot: Int) = bagInventory.removeStack(slot)
        override fun setStack(slot: Int, stack: ItemStack) = bagInventory.setStack(slot, stack)
        override fun getMaxCountPerStack() = bagInventory.maxCountPerStack
        override fun markDirty() {
            bagInventory.markDirty()
            itemStackInstance.setBagInventory(bagInventory)
            expectedItemStack = itemStackInstance.copy()
        }

        override fun canPlayerUse(player: PlayerEntity) = bagInventory.canPlayerUse(player)
        override fun onOpen(player: PlayerEntity) = bagInventory.onOpen(player)
        override fun onClose(player: PlayerEntity) = bagInventory.onClose(player)
        override fun isValid(slot: Int, stack: ItemStack) = bagInventory.isValid(slot, stack)
        override fun canTransferTo(hopperInventory: Inventory, slot: Int, stack: ItemStack) = bagInventory.canTransferTo(hopperInventory, slot, stack)
    }

    return object : BagScreenHandler(syncId) {
        init {
            repeat(3) { r ->
                repeat(9) { c ->
                    addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
                }
            }
            repeat(9) { c ->
                addSlot(Slot(playerInventory, c, 0, 0))
            }
            repeat(item.card.inventorySize) { i ->
                addSlot(FilteringSlot(inventoryDelegate, i, 0, 0))
            }
        }

        override fun canUse(player: PlayerEntity): Boolean {
            val itemStack = if (slotIndex >= 0) playerInventory.main[slotIndex] else playerInventory.offHand[0]
            return itemStack === itemStackInstance && itemStack hasSameItemAndNbtAndCount expectedItemStack
        }

        override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
            val playerIndices = 9 * 4 - 1 downTo 0
            val utilityIndices = 9 * 4 until slots.size
            val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
            return quickMove(slot, destinationIndices)
        }

        override val card = item.card
    }
}

open class BagScreenHandler(syncId: Int) : ScreenHandler(BagCard.screenHandlerType, syncId) {
    override fun canUse(player: PlayerEntity) = false
    override fun quickMove(player: PlayerEntity, slot: Int) = EMPTY_ITEM_STACK
    open val card: BagCard? = null
}
