package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.clientProxy
import miragefairy2024.util.Channel
import miragefairy2024.util.ITEMS_CODEC
import miragefairy2024.util.ITEMS_STREAM_CODEC
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.dummyUnitStreamCodec
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.invoke
import miragefairy2024.util.quickMove
import miragefairy2024.util.register
import miragefairy2024.util.registerServerPacketReceiver
import miragefairy2024.util.set
import miragefairy2024.util.size
import miragefairy2024.util.text
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.Container
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.SimpleContainer as SimpleInventory
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler

private val SOUL_STREAM_TRANSLATION = Translation({ "container.${MirageFairy2024.MOD_ID}.soul_stream" }, "Soul Stream", "ソウルストリーム")
val OPEN_SOUL_STREAM_KEY_TRANSLATION = Translation({ "key.${MirageFairy2024.MOD_ID}.open_soul_stream" }, "Open Soul Stream", "ソウルストリームを開く")

val SOUL_STREAM_CONTAINABLE_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, MirageFairy2024.identifier("soul_stream_containable"))

context(ModContext)
fun initSoulStream() {

    // プレイヤー追加データ
    SOUL_STREAM_ATTACHMENT_TYPE.register()

    // ソウルストリームを開く要求パケット
    ModEvents.onInitialize {
        OpenSoulStreamChannel.registerServerPacketReceiver { player, _ ->
            player.openMenu(object : ExtendedScreenHandlerFactory<Unit> {
                override fun createMenu(syncId: Int, playerInventory: Inventory, player: PlayerEntity): ScreenHandler {
                    return SoulStreamScreenHandler(syncId, playerInventory, player.soulStream.getOrCreate())
                }

                override fun getDisplayName() = text { SOUL_STREAM_TRANSLATION() }

                override fun getScreenOpeningData(player: ServerPlayer) = Unit
            })
        }
    }

    // GUI
    soulStreamScreenHandlerType.register()

    // 翻訳
    SOUL_STREAM_TRANSLATION.enJa()
    OPEN_SOUL_STREAM_KEY_TRANSLATION.enJa()

}


// 拡張プレイヤーデータ

val SOUL_STREAM_ATTACHMENT_TYPE: AttachmentType<SoulStream> = AttachmentRegistry.create(MirageFairy2024.identifier("soul_stream")) {
    it.persistent(SoulStream.CODEC)
    it.initializer(::SoulStream)
    it.syncWith(SoulStream.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.soulStream get() = this[SOUL_STREAM_ATTACHMENT_TYPE]

class SoulStream() : SimpleInventory(SLOT_COUNT) {
    companion object {
        const val SLOT_COUNT = 9 * 31
        const val PASSIVE_SKILL_SLOT_COUNT = 9

        val CODEC: Codec<SoulStream> = ITEMS_CODEC.xmap(::SoulStream, SoulStream::items)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SoulStream> = ITEMS_STREAM_CODEC.map(::SoulStream, SoulStream::items)
    }

    constructor(items: List<ItemStack>) : this() {
        items.forEachIndexed { index, itemStack ->
            if (index < size) this[index] = itemStack
        }
    }
}


// ソウルストリームを開く要求パケット

object OpenSoulStreamChannel : Channel<Unit>(MirageFairy2024.identifier("open_soul_stream")) {
    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: Unit) = Unit
    override fun readFromBuf(buf: RegistryFriendlyByteBuf) = Unit
}


// GUI

val soulStreamScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("soul_stream")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, _ ->
        SoulStreamScreenHandler(syncId, playerInventory, clientProxy!!.getClientPlayer()!!.soulStream.getOrCreate())
    }, dummyUnitStreamCodec())
}

class SoulStreamScreenHandler(syncId: Int, val playerInventory: Inventory, val soulStream: Container) : ScreenHandler(soulStreamScreenHandlerType(), syncId) {
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
                override fun mayPlace(stack: ItemStack) = stack.`is`(SOUL_STREAM_CONTAINABLE_TAG)
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
