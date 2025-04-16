package miragefairy2024.mod.magicplant

import miragefairy2024.clientProxy
import miragefairy2024.util.boolean
import miragefairy2024.util.darkGray
import miragefairy2024.util.darkRed
import miragefairy2024.util.get
import miragefairy2024.util.green
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.style
import miragefairy2024.util.text
import miragefairy2024.util.wrapper
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.max
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.world.level.block.Block
import net.minecraft.world.item.TooltipFlag as TooltipContext
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.world.item.ItemNameBlockItem as AliasedBlockItem
import net.minecraft.world.item.context.BlockPlaceContext as ItemPlacementContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext as ItemUsageContext
import net.minecraft.network.FriendlyByteBuf as PacketByteBuf
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.network.chat.Component as Text
import net.minecraft.world.InteractionResult as ActionResult
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.world.level.Level as World

class MagicPlantSeedItem(block: Block, settings: Properties) : AliasedBlockItem(block, settings) {
    override fun appendHoverText(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendHoverText(stack, world, tooltip, context)
        if (world == null) return
        val player = clientProxy?.getClientPlayer() ?: return

        // 特性を得る、無い場合はクリエイティブ専用
        val traitStacks = stack.getTraitStacks() ?: run {
            tooltip += text { CREATIVE_ONLY_TRANSLATION().yellow }
            return
        }

        // 機能説明
        tooltip += text { GUI_TRANSLATION().yellow }

        // プレイヤーのメインハンドの種子の特性を得る
        val otherTraitStacks = if (player.mainHandItem.item == this) player.mainHandItem.getTraitStacks() else null

        // ヘッダー行
        run {
            val countText = when {
                otherTraitStacks == null -> text { "${traitStacks.traitStackList.size}"() }
                traitStacks.traitStackList.size > otherTraitStacks.traitStackList.size -> text { "${traitStacks.traitStackList.size}"().green }
                traitStacks.traitStackList.size == otherTraitStacks.traitStackList.size -> text { "${traitStacks.traitStackList.size}"().darkGray }
                else -> text { "${traitStacks.traitStackList.size}"().darkRed }
            }
            val bitCountText = when {
                otherTraitStacks == null -> text { "${traitStacks.bitCount}"() }
                traitStacks.bitCount > otherTraitStacks.bitCount -> text { "${traitStacks.bitCount}"().green }
                traitStacks.bitCount == otherTraitStacks.bitCount -> text { "${traitStacks.bitCount}"().darkGray }
                else -> text { "${traitStacks.bitCount}"().darkRed }
            }
            tooltip += text { TRAIT_TRANSLATION() + ": x"() + countText + " ("() + bitCountText + "b)"() }
        }

        // 特性行
        traitStacks.traitStackMap.entries
            .sortedBy { it.key }
            .forEach { (trait, level) ->
                val levelText = when {
                    otherTraitStacks == null -> text { level.toString(2)() }

                    else -> {
                        val otherLevel = otherTraitStacks.traitStackMap[trait] ?: 0
                        val bits = (level max otherLevel).toString(2).length
                        (bits - 1 downTo 0).map { bit ->
                            val mask = 1 shl bit
                            val possession = if (level and mask != 0) 1 else 0
                            val otherPossession = if (otherLevel and mask != 0) 1 else 0
                            when {
                                possession > otherPossession -> text { "$possession"().green }
                                possession == otherPossession -> text { "$possession"().darkGray }
                                else -> text { "$possession"().darkRed }
                            }
                        }.join()
                    }
                }

                val traitEffects = trait.getTraitEffects(world, player.blockPosition(), level)
                tooltip += if (traitEffects != null) {
                    val description = text {
                        traitEffects.effects
                            .map {
                                fun <T : Any> TraitEffect<T>.render() = text { this@render.key.emoji + this@render.key.renderValue(this@render.value) }
                                it.render()
                            }
                            .reduce { a, b -> a + " "() + b }
                    }
                    text { ("  "() + trait.getName() + " "() + levelText + " ("() + description + ")"()).style(trait.style) }
                } else {
                    text { ("  "() + trait.getName() + " "() + levelText + " ("() + INVALID_TRANSLATION() + ")"()).darkGray }
                }
            }

    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (context.player?.isShiftKeyDown == true) return ActionResult.PASS
        return super.useOnBlock(context)
    }

    override fun place(context: ItemPlacementContext): ActionResult {
        if (context.stack.getTraitStacks() != null) {
            return super.place(context)
        } else {
            val player = context.player ?: return ActionResult.FAIL
            if (!player.isCreative) return ActionResult.FAIL
            return super.place(context)
        }
    }

    override fun isFoil(stack: ItemStack) = stack.isRare() || super.isFoil(stack)

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (user.isShiftKeyDown) {
            val itemStack = user.getItemInHand(hand)
            if (world.isClientSide) return TypedActionResult.success(itemStack)
            val traitStacks = itemStack.getTraitStacks() ?: TraitStacks.EMPTY
            user.openMenu(object : ExtendedScreenHandlerFactory {
                override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
                    return TraitListScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, player.blockPosition()), traitStacks)
                }

                override fun getDisplayName() = text { traitListScreenTranslation() }

                override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                    TraitListScreenHandler.write(buf, traitStacks)
                }
            })
            return TypedActionResult.consume(itemStack)
        }
        return super.use(world, user, hand)
    }
}

fun ItemStack.getTraitStacks(): TraitStacks? {
    val nbt = this.tag ?: return null
    return TraitStacks.readFromNbt(nbt)
}

fun ItemStack.setTraitStacks(traitStacks: TraitStacks) {
    getOrCreateTag().put("TraitStacks", traitStacks.toNbt())
}

fun ItemStack.isRare() = this.tag.or { return false }.wrapper["Rare"].boolean.get().or { false }
fun ItemStack.setRare(isRare: Boolean) = this.getOrCreateTag().wrapper["Rare"].boolean.set(if (isRare) true else null)
