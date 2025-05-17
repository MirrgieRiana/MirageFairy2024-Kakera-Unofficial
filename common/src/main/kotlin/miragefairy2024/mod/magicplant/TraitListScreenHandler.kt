package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.list
import miragefairy2024.util.register
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.nbt.ListTag as NbtList
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext

val traitListScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("trait_list")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
        TraitListScreenHandler(syncId, playerInventory, ScreenHandlerContext.NULL, buf)
    }, TraitListScreenHandler.STREAM_CODEC)
}

val traitListScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("trait_list").toLanguageKey()}" }, "Traits", "特性")

context(ModContext)
fun initTraitListScreenHandler() {
    traitListScreenHandlerType.register()
    traitListScreenTranslation.enJa()
}

class TraitListScreenHandler(syncId: Int, val playerInventory: Inventory, val context: ScreenHandlerContext, val traitStacks: TraitStacks) : ScreenHandler(traitListScreenHandlerType(), syncId) {
    companion object {
        val STREAM_CODEC = object : StreamCodec<FriendlyByteBuf, TraitStacks> {
            override fun encode(`object`: FriendlyByteBuf, object2: TraitStacks) {
                val nbt = NbtCompound()
                nbt.wrapper["TraitStacks"].list.set(object2.toNbt())
                `object`.writeNbt(nbt)
            }

            override fun decode(`object`: FriendlyByteBuf): TraitStacks {
                val nbt = `object`.readNbt() ?: NbtCompound()
                return nbt.wrapper["TraitStacks"].list.get().or { NbtList() }.toTraitStacks()
            }
        }
    }

    override fun stillValid(player: PlayerEntity) = true
    override fun quickMoveStack(player: PlayerEntity, slot: Int): ItemStack = slots[slot].item
}
