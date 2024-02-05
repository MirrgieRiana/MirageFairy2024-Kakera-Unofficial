package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.WISP_TAG
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.util.Channel
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Translation
import miragefairy2024.util.compound
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.registerServerPacketReceiver
import miragefairy2024.util.size
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventories
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

private val SOUL_STREAM_TRANSLATION = Translation({ "container.miragefairy2024.soul_stream" }, "Soul Stream", "ソウルストリーム")
val OPEN_SOUL_STREAM_KEY_TRANSLATION = Translation({ "key.miragefairy2024.open_soul_stream" }, "Open Soul Stream", "ソウルストリームを開く")

fun initSoulStream() {

    // 拡張プレイヤーデータ
    SoulStreamExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, Identifier(MirageFairy2024.modId, "soul_stream"))

    // ソウルストリームを開く要求パケット
    OpenSoulStreamChannel.registerServerPacketReceiver { player, _ ->
        player.openHandledScreen(object : ExtendedScreenHandlerFactory {
            override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return SoulStreamScreenHandler(syncId, playerInventory, player.soulStream)
            }

            override fun getDisplayName() = SOUL_STREAM_TRANSLATION()

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) = Unit
        })
    }

    // GUI
    soulStreamScreenHandlerType.register(Registries.SCREEN_HANDLER, Identifier(MirageFairy2024.modId, "soul_stream"))

    // 翻訳
    SOUL_STREAM_TRANSLATION.enJa()
    OPEN_SOUL_STREAM_KEY_TRANSLATION.enJa()

}


// 拡張プレイヤーデータ

object SoulStreamExtraPlayerDataCategory : ExtraPlayerDataCategory<SoulStream> {
    override fun create() = SoulStream()
    override fun castOrThrow(value: Any) = value as SoulStream
    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<SoulStream> {
        override fun fromNbt(player: PlayerEntity, nbt: NbtCompound): SoulStream {
            val data = SoulStream()
            Inventories.readNbt(nbt.wrapper["Inventory"].compound.get() ?: NbtCompound(), data.stacks)
            return data
        }

        override fun toNbt(player: PlayerEntity, data: SoulStream): NbtCompound {
            val nbt = NbtCompound()
            nbt.wrapper["Inventory"].compound.set(NbtCompound().also {
                Inventories.writeNbt(it, data.stacks)
            })
            return nbt
        }
    }
}

val PlayerEntity.soulStream get() = this.extraPlayerDataContainer.getOrInit(SoulStreamExtraPlayerDataCategory)

class SoulStream : SimpleInventory(SLOT_COUNT) {
    companion object {
        val SLOT_COUNT = 9 * 18
    }
}


// ソウルストリームを開く要求パケット

object OpenSoulStreamChannel : Channel<Unit>(Identifier(MirageFairy2024.modId, "open_soul_stream")) {
    override fun writeToBuf(buf: PacketByteBuf, player: PlayerEntity, packet: Unit) = Unit
    override fun readFromBuf(buf: PacketByteBuf, player: PlayerEntity) = Unit
}


// GUI

val soulStreamScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
    SoulStreamScreenHandler(syncId, playerInventory, SimpleInventory(SoulStream.SLOT_COUNT))
}

class SoulStreamScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val soulStream: Inventory) : ScreenHandler(soulStreamScreenHandlerType, syncId) {
    init {
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(playerInventory, c, 0, 0))
        }
        repeat(soulStream.size) { i ->
            addSlot(object : Slot(soulStream, i, 0, 0) {
                override fun canInsert(stack: ItemStack) = stack.isOf(FairyCard.item) || stack.isIn(WISP_TAG)
            })
        }
    }

    override fun canUse(player: PlayerEntity) = true
    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {

        if (slot < 0 || slot >= slots.size) return EMPTY_ITEM_STACK
        if (!slots[slot].hasStack()) return EMPTY_ITEM_STACK // そこに何も無い場合は何もしない

        val newItemStack = slots[slot].stack
        val originalItemStack = newItemStack.copy()

        if (slot < 9 * 4) {
            if (!insertItem(newItemStack, 9 * 4, 9 * 4 + SoulStream.SLOT_COUNT, false)) return EMPTY_ITEM_STACK
        } else {
            if (!insertItem(newItemStack, 0, 9 * 4, false)) return EMPTY_ITEM_STACK
        }

        // 終了処理
        if (newItemStack.isEmpty) {
            slots[slot].stack = EMPTY_ITEM_STACK
        } else {
            slots[slot].markDirty()
        }

        return originalItemStack
    }
}
