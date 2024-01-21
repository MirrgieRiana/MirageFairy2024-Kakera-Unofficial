package miragefairy2024.mod.fairy

import miragefairy2024.mod.APPEARANCE_RATE_BONUS_TRANSLATION
import miragefairy2024.mod.MIRAGE_FLOUR_DESCRIPTION_TRANSLATION
import miragefairy2024.util.blue
import miragefairy2024.util.invoke
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.formatAs
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import kotlin.math.pow

class RandomFairySummoningItem(val appearanceRateBonus: Double, settings: Settings) : Item(settings) {
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        tooltip += text { (APPEARANCE_RATE_BONUS_TRANSLATION() + ": x"() + (appearanceRateBonus formatAs "%.3f").replace("""\.?0+$""".toRegex(), "")()).blue }
        tooltip += MIRAGE_FLOUR_DESCRIPTION_TRANSLATION().yellow
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        if (!user.isSneaking) {
            return TypedActionResult.pass(itemStack)
        } else {
            if (world.isClient) return TypedActionResult.success(itemStack)

            val chanceTable = COMMON_MOTIF_RECIPES.filter { it.biome == null || world.getBiome(user.blockPos).isIn(it.biome) }.map { recipe ->
                val rate = 0.1.pow(recipe.motif.rare / 2.0)
                val count = 1.0 // TODO
                CondensedMotifChance(motifRegistry.getId(recipe.motif)!!, rate, count)
            }

            user.openHandledScreen(object : ExtendedScreenHandlerFactory {
                override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
                    return MotifTableScreenHandler(syncId, chanceTable)
                }

                override fun getDisplayName() = itemStack.name

                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                    buf.writeInt(chanceTable.size)
                    chanceTable.forEach {
                        buf.writeString(it.motifId.string)
                        buf.writeDouble(it.rate)
                        buf.writeDouble(it.condensation)
                    }
                }
            })
            return TypedActionResult.consume(itemStack)
        }
    }
}

class CondensedMotifChance(val motifId: Identifier, val rate: Double, val condensation: Double)
