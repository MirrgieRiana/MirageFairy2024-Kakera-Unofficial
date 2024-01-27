package miragefairy2024.mod.fairy

import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.opposite
import miragefairy2024.util.registerDebugItem
import miragefairy2024.util.sendToClient
import miragefairy2024.util.text
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity

val GAIN_FAIRY_DREAM_TRANSLATION = Translation({ "gui.miragefairy2024.fairy_dream.gain" }, "Dreamed of a new fairy!", "新たな妖精の夢を見た！")

fun initFairyDream() {

    GAIN_FAIRY_DREAM_TRANSLATION.enJa()

    registerDebugItem("debug_clear_fairy_dream", Items.STRING, 0x0000DD) { world, player, _, _ ->
        if (world.isClient) return@registerDebugItem
        player.fairyDreamContainer.clear()
        player.sendMessage(text { "Cleared fairy dream"() }, true)
    }

    registerDebugItem("debug_gain_fairy_dream", Items.STRING, 0x0000FF) { world, player, hand, _ ->
        if (world.isClient) return@registerDebugItem
        if (player !is ServerPlayerEntity) return@registerDebugItem

        val fairyItemStack = player.getStackInHand(hand.opposite)
        if (!fairyItemStack.isOf(FairyCard.item)) return@registerDebugItem
        val motif = fairyItemStack.getFairyMotif() ?: return@registerDebugItem

        if (!player.isSneaking) {
            player.sendMessage(text { "${player.fairyDreamContainer[motif]}"() })
        } else {
            player.fairyDreamContainer[motif] = true
            GainFairyDreamChannel.sendToClient(player, motif)
        }
    }

}
