package miragefairy2024.mod.magicplant

import miragefairy2024.clientProxy
import miragefairy2024.util.darkGray
import miragefairy2024.util.darkRed
import miragefairy2024.util.green
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.style
import miragefairy2024.util.text
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.max
import mirrg.kotlin.hydrogen.unit
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.inventory.AbstractContainerMenu as ScreenHandler
import net.minecraft.world.inventory.ContainerLevelAccess as ScreenHandlerContext
import net.minecraft.world.item.ItemNameBlockItem as AliasedBlockItem
import net.minecraft.world.item.context.BlockPlaceContext as ItemPlacementContext
import net.minecraft.world.item.context.UseOnContext as ItemUsageContext

class MagicPlantSeedItem(block: Block, settings: Properties) : AliasedBlockItem(block, settings) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)
        val player = clientProxy?.getClientPlayer() ?: return
        val world = player.level() ?: return

        // 特性を得る、無い場合はクリエイティブ専用
        val traitStacks = stack.getTraitStacks() ?: run {
            tooltipComponents += text { CREATIVE_ONLY_TRANSLATION().yellow }
            return
        }

        // 機能説明
        tooltipComponents += text { GUI_TRANSLATION().yellow }

        // プレイヤーのメインハンドの種子の特性を得る
        val otherTraitStacks = if (player.mainHandItem.item is MagicPlantSeedItem) player.mainHandItem.getTraitStacks() else null

        // ヘッダー行
        run {
            val sections = mutableListOf<Component>()

            // ラベル
            sections += text { TRAIT_TRANSLATION() + ":"() } // Trait:

            // 特性の個数
            val traitCount = traitStacks.traitStackList.size
            sections += text { "x$traitCount"().let { if (otherTraitStacks != null) it.signColor(traitCount - otherTraitStacks.traitStackList.size) else it } } // x99

            // 特性の増減
            val plusTraitCount = if (otherTraitStacks != null) (traitStacks.traitStackMap.keys - otherTraitStacks.traitStackMap.keys).size else null
            val minusTraitCount = if (otherTraitStacks != null) (otherTraitStacks.traitStackMap.keys - traitStacks.traitStackMap.keys).size else null
            sections += listOfNotNull(
                if (plusTraitCount != null && plusTraitCount > 0) text { "+$plusTraitCount"().signColor(1) } else null, // +9
                if (minusTraitCount != null && minusTraitCount > 0) text { "-$minusTraitCount"().signColor(-1) } else null, // -9
            ).let { if (it.isNotEmpty()) listOf(text { "("() + it.join(" "()) + ")"() }) else listOf() } // (+9 -9)  (+9)  null

            // 区切り
            sections += text { "/"() } // /

            // 特性ビットの個数
            val bitCount = traitStacks.bitCount
            sections += text { "${bitCount}b"().let { if (otherTraitStacks != null) it.signColor(bitCount - otherTraitStacks.bitCount) else it } } // 99b

            // 特性ビットの増減
            val plusBitCount = if (otherTraitStacks != null) (traitStacks - otherTraitStacks).bitCount else null
            val minusBitCount = if (otherTraitStacks != null) (otherTraitStacks - traitStacks).bitCount else null
            sections += listOfNotNull(
                if (plusBitCount != null && plusBitCount > 0) text { "+${plusBitCount}b"().signColor(1) } else null, // +9b
                if (minusBitCount != null && minusBitCount > 0) text { "-${minusBitCount}b"().signColor(-1) } else null, // -9b
            ).let { if (it.isNotEmpty()) listOf(text { "("() + it.join(" "()) + ")"() }) else listOf() } // (+9b -9b)  (+9b)  null

            tooltipComponents += sections.join(text { " "() }) // Trait: x99 (+9 -9) / 99b (+9 -9)
        }

        // 特性行
        val traitStackMap = if (otherTraitStacks != null) otherTraitStacks.traitStackMap.mapValues { 0 } + traitStacks.traitStackMap else traitStacks.traitStackMap // 比較対象がある場合は空特性も表示
        traitStackMap.entries
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

                val traitEffects = trait.getTraitEffects(world, player.blockPosition(), world.getMagicPlantBlockEntity(player.blockPosition()), level)
                tooltipComponents += if (traitEffects != null) {
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

    override fun useOn(context: ItemUsageContext): InteractionResult {
        if (context.player?.isShiftKeyDown == true) return InteractionResult.PASS
        return super.useOn(context)
    }

    override fun place(context: ItemPlacementContext): InteractionResult {
        if (context.itemInHand.getTraitStacks() != null) {
            return super.place(context)
        } else {
            val player = context.player ?: return InteractionResult.FAIL
            if (!player.isCreative) return InteractionResult.FAIL
            return super.place(context)
        }
    }

    override fun isFoil(stack: ItemStack) = stack.isRare() || super.isFoil(stack)

    override fun use(world: Level, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        if (user.isShiftKeyDown) {
            val itemStack = user.getItemInHand(hand)
            if (world.isClientSide) return TypedActionResult.success(itemStack)
            val traitStacks = itemStack.getTraitStacks() ?: TraitStacks.EMPTY
            user.openMenu(object : ExtendedScreenHandlerFactory<TraitStacks> {
                override fun createMenu(syncId: Int, playerInventory: Inventory, player: PlayerEntity): ScreenHandler {
                    return TraitListScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(world, player.blockPosition()), traitStacks)
                }

                override fun getDisplayName() = text { traitListScreenTranslation() }

                override fun getScreenOpeningData(player: ServerPlayer) = traitStacks
            })
            return TypedActionResult.consume(itemStack)
        }
        return super.use(world, user, hand)
    }
}

private fun Component.signColor(number: Int): Component {
    return when {
        number > 0 -> this.green
        number < 0 -> this.darkRed
        else -> this.darkGray
    }
}

fun ItemStack.getTraitStacks() = this.get(TRAIT_STACKS_DATA_COMPONENT_TYPE)
fun ItemStack.setTraitStacks(traitStacks: TraitStacks?) = unit { this.set(TRAIT_STACKS_DATA_COMPONENT_TYPE, traitStacks) }

fun ItemStack.isRare() = (this.get(RARITY_DATA_COMPONENT_TYPE) ?: 0) >= 1
fun ItemStack.setRare(isRare: Boolean) = unit { this.set(RARITY_DATA_COMPONENT_TYPE, if (isRare) 1 else 0) }
