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
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.nbt.ListTag as NbtList
import net.minecraft.network.FriendlyByteBuf as PacketByteBuf
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext

val traitListScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
    TraitListScreenHandler(syncId, playerInventory, ScreenHandlerContext.NULL, TraitListScreenHandler.read(buf))
}

val traitListScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("trait_list").toTranslationKey()}" }, "Traits", "特性")

context(ModContext)
fun initTraitListScreenHandler() {
    traitListScreenHandlerType.register(Registries.MENU, MirageFairy2024.identifier("trait_list"))
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
