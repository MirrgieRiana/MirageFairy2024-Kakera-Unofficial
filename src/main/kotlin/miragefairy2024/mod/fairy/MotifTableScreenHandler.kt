package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.register
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler

val MOTIF_TABLE_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, List<CondensedMotifChance>> {
    override fun encode(`object`: RegistryFriendlyByteBuf, object2: List<CondensedMotifChance>) {
        `object`.writeInt(object2.size)
        object2.forEach {
            ItemStack.STREAM_CODEC.encode(`object`, it.showingItemStack)
            `object`.writeUtf(it.motif.getIdentifier()!!.string)
            `object`.writeDouble(it.rate)
            `object`.writeDouble(it.count)
        }
    }

    override fun decode(`object`: RegistryFriendlyByteBuf): List<CondensedMotifChance> {
        val length = `object`.readInt()
        val chanceTable = mutableListOf<CondensedMotifChance>()
        repeat(length) {
            val showingItemStack = ItemStack.STREAM_CODEC.decode(`object`)
            val motifId = `object`.readUtf()
            val rate = `object`.readDouble()
            val count = `object`.readDouble()
            chanceTable += CondensedMotifChance(showingItemStack, motifRegistry.get(motifId.toIdentifier())!!, rate, count)
        }
        return chanceTable
    }
}

val motifTableScreenHandlerType = ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
    MotifTableScreenHandler(syncId, buf)
}, MOTIF_TABLE_STREAM_CODEC)

context(ModContext)
fun initMotifTableScreenHandler() {
    motifTableScreenHandlerType.register(Registries.MENU, MirageFairy2024.identifier("motif_table"))
}

class MotifTableScreenHandler(syncId: Int, val chanceTable: List<CondensedMotifChance>) : ScreenHandler(motifTableScreenHandlerType, syncId) {
    override fun stillValid(player: PlayerEntity) = true
    override fun quickMoveStack(player: PlayerEntity, slot: Int) = EMPTY_ITEM_STACK
}
