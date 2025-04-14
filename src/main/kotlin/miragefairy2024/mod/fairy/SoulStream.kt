package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.clientProxy
import miragefairy2024.mod.ExtraPlayerDataCategory
import miragefairy2024.mod.extraPlayerDataCategoryRegistry
import miragefairy2024.mod.extraPlayerDataContainer
import miragefairy2024.util.Channel
import miragefairy2024.util.Translation
import miragefairy2024.util.compound
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.registerServerPacketReceiver
import miragefairy2024.util.size
import miragefairy2024.util.text
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.world.ContainerHelper as Inventories
import net.minecraft.world.Container as Inventory
import net.minecraft.world.SimpleContainer as SimpleInventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.network.FriendlyByteBuf as PacketByteBuf
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.tags.TagKey
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.Slot
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity

private val SOUL_STREAM_TRANSLATION = Translation({ "container.${MirageFairy2024.MOD_ID}.soul_stream" }, "Soul Stream", "ソウルストリーム")
val OPEN_SOUL_STREAM_KEY_TRANSLATION = Translation({ "key.${MirageFairy2024.MOD_ID}.open_soul_stream" }, "Open Soul Stream", "ソウルストリームを開く")

val SOUL_STREAM_CONTAINABLE_TAG: TagKey<Item> = TagKey.create(RegistryKeys.ITEM, MirageFairy2024.identifier("soul_stream_containable"))

context(ModContext)
fun initSoulStream() {

    // 拡張プレイヤーデータ
    SoulStreamExtraPlayerDataCategory.register(extraPlayerDataCategoryRegistry, MirageFairy2024.identifier("soul_stream"))

    // ソウルストリームを開く要求パケット
    ModEvents.onInitialize {
        OpenSoulStreamChannel.registerServerPacketReceiver { player, _ ->
            player.openMenu(object : ExtendedScreenHandlerFactory {
                override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
                    return SoulStreamScreenHandler(syncId, playerInventory, player.soulStream)
                }

                override fun getDisplayName() = text { SOUL_STREAM_TRANSLATION() }

                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) = Unit
            })
        }
    }

    // GUI
    soulStreamScreenHandlerType.register(Registries.MENU, MirageFairy2024.identifier("soul_stream"))

    // 翻訳
    SOUL_STREAM_TRANSLATION.enJa()
    OPEN_SOUL_STREAM_KEY_TRANSLATION.enJa()

}


// 拡張プレイヤーデータ

object SoulStreamExtraPlayerDataCategory : ExtraPlayerDataCategory<SoulStream> {
    override fun create() = SoulStream()
    override fun castOrThrow(value: Any) = value as SoulStream
    override val ioHandler = object : ExtraPlayerDataCategory.IoHandler<SoulStream> {
        override fun fromNbt(nbt: NbtCompound): SoulStream {
            val data = SoulStream()
            Inventories.readNbt(nbt.wrapper["Inventory"].compound.get() ?: NbtCompound(), data.stacks)
            return data
        }

        override fun toNbt(data: SoulStream): NbtCompound {
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
        const val SLOT_COUNT = 9 * 31
        const val PASSIVE_SKILL_SLOT_COUNT = 9
    }
}


// ソウルストリームを開く要求パケット

object OpenSoulStreamChannel : Channel<Unit>(MirageFairy2024.identifier("open_soul_stream")) {
    override fun writeToBuf(buf: PacketByteBuf, packet: Unit) = Unit
    override fun readFromBuf(buf: PacketByteBuf) = Unit
}


// GUI

val soulStreamScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
    SoulStreamScreenHandler(syncId, playerInventory, clientProxy!!.getClientPlayer()!!.soulStream)
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
                override fun canInsert(stack: ItemStack) = stack.`is`(SOUL_STREAM_CONTAINABLE_TAG)
            })
        }
    }

    override fun stillValid(player: PlayerEntity) = true
    override fun quickMoveStack(player: PlayerEntity, slot: Int): ItemStack {
        val playerIndices = 9 * 4 - 1 downTo 0
        val utilityIndices = 9 * 4 + 9 until 9 * 4 + SoulStream.SLOT_COUNT
        val destinationIndices = if (slot in playerIndices) utilityIndices else playerIndices
        return quickMove(slot, destinationIndices)
    }
}
