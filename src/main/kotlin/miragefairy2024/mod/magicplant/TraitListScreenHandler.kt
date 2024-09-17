package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.list
import miragefairy2024.util.register
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext

val traitListScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
    TraitListScreenHandler(syncId, playerInventory, ScreenHandlerContext.EMPTY, TraitListScreenHandler.read(buf))
}

val traitListScreenTranslation = Translation({ "gui.${MirageFairy2024.MOD_ID}.trait_list" }, "Traits", "特性")

context(ModContext)
fun initTraitListScreenHandler() {
    traitListScreenHandlerType.register(Registries.SCREEN_HANDLER, MirageFairy2024.identifier("trait_list"))
    traitListScreenTranslation.enJa()
}

class TraitListScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val context: ScreenHandlerContext, val traitStacks: TraitStacks) : ScreenHandler(traitListScreenHandlerType, syncId) {
    companion object {
        fun write(buf: PacketByteBuf, traitStacks: TraitStacks) {
            val nbt = NbtCompound()
            nbt.wrapper["TraitStacks"].list.set(traitStacks.toNbt())
            buf.writeNbt(nbt)
        }

        fun read(buf: PacketByteBuf): TraitStacks {
            val nbt = buf.readNbt() ?: NbtCompound()
            return nbt.wrapper["TraitStacks"].list.get().or { NbtList() }.toTraitStacks()
        }
    }

    override fun canUse(player: PlayerEntity) = true
    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack = slots[slot].stack
}
