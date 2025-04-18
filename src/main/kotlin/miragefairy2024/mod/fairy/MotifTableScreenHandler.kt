package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.register
import miragefairy2024.util.toIdentifier
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler

val motifTableScreenHandlerType = ExtendedScreenHandlerType { syncId, playerInventory, buf ->
    val length = buf.readInt()
    val chanceTable = mutableListOf<CondensedMotifChance>()
    repeat(length) {
        val showingItemStack = buf.readItem()
        val motifId = buf.readUtf()
        val rate = buf.readDouble()
        val count = buf.readDouble()
        chanceTable += CondensedMotifChance(showingItemStack, motifRegistry.get(motifId.toIdentifier())!!, rate, count)
    }
    MotifTableScreenHandler(syncId, chanceTable)
}

context(ModContext)
fun initMotifTableScreenHandler() {
    motifTableScreenHandlerType.register(Registries.MENU, MirageFairy2024.identifier("motif_table"))
}

class MotifTableScreenHandler(syncId: Int, val chanceTable: List<CondensedMotifChance>) : ScreenHandler(motifTableScreenHandlerType, syncId) {
    override fun stillValid(player: PlayerEntity) = true
    override fun quickMoveStack(player: PlayerEntity, slot: Int) = EMPTY_ITEM_STACK
}
