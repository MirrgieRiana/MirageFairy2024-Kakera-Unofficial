package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.register
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier

val fairyQuestCardScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
    FairyQuestCardScreenHandler(syncId, playerInventory, fairyQuestRecipeRegistry.get(Identifier(buf.readString()))!!, ScreenHandlerContext.EMPTY)
}

fun initFairyQuestCardScreenHandler() {
    fairyQuestCardScreenHandlerType.register(Registries.SCREEN_HANDLER, Identifier(MirageFairy2024.modId, "fairy_quest_card"))
}

class FairyQuestCardScreenHandler(syncId: Int, val playerInventory: PlayerInventory, val recipe: FairyQuestRecipe, val context: ScreenHandlerContext) : ScreenHandler(fairyQuestCardScreenHandlerType, syncId) {
    init {
        repeat(3) { r ->
            repeat(9) { c ->
                addSlot(Slot(playerInventory, 9 + 9 * r + c, 0, 0))
            }
        }
        repeat(9) { c ->
            addSlot(Slot(playerInventory, c, 0, 0))
        }
    }

    override fun canUse(player: PlayerEntity): Boolean {
        return true
    }

    override fun quickMove(player: PlayerEntity, slot: Int): ItemStack {
        // TODO
        return EMPTY_ITEM_STACK
    }
}
