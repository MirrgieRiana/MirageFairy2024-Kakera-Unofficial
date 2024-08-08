package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.register
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

val traitListScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, _ ->
    TraitListScreenHandler(syncId, playerInventory, ScreenHandlerContext.EMPTY)
}

context(ModContext)
fun initTraitListScreenHandler() {
    traitListScreenHandlerType.register(Registries.SCREEN_HANDLER, Identifier(MirageFairy2024.modId, "trait_list"))
}

class TraitListScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val context: ScreenHandlerContext) : ScreenHandler(traitListScreenHandlerType, syncId) {
    override fun canUse(player: PlayerEntity) = true
    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack = slots[slot].stack
}
