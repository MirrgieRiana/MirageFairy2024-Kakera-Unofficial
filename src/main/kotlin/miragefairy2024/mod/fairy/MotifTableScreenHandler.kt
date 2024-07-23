package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.register
import miragefairy2024.util.toIdentifier
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier

val motifTableScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
    val length = buf.readInt()
    val chanceTable = mutableListOf<CondensedMotifChance>()
    repeat(length) {
        val motifId = buf.readString()
        val rate = buf.readDouble()
        val condensation = buf.readDouble()
        chanceTable += CondensedMotifChance(motifRegistry.get(motifId.toIdentifier())!!, rate, condensation)
    }
    MotifTableScreenHandler(syncId, chanceTable)
}

fun initMotifTableScreenHandler() = ModEvents.onInitialize {
    motifTableScreenHandlerType.register(Registries.SCREEN_HANDLER, Identifier(MirageFairy2024.modId, "motif_table"))
}

class MotifTableScreenHandler(syncId: Int, val chanceTable: List<CondensedMotifChance>) : ScreenHandler(motifTableScreenHandlerType, syncId) {
    override fun canUse(player: PlayerEntity) = true
    override fun quickMove(player: PlayerEntity, slot: Int) = EMPTY_ITEM_STACK
}
