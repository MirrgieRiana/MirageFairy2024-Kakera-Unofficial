package miragefairy2024.client.mod

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.Component
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.topBorderLayout
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.SeedBagCard
import miragefairy2024.mod.magicplant.SeedBagItem
import miragefairy2024.mod.magicplant.SeedBagScreenHandler
import miragefairy2024.mod.magicplant.TraitListScreenHandler
import miragefairy2024.mod.magicplant.TraitStack
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.magicplant.traitListScreenHandlerType
import miragefairy2024.mod.magicplant.traitListScreenTranslation
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Style
import net.minecraft.text.Text

fun initMagicPlantClientModule() {
    HandledScreens.register(traitListScreenHandlerType) { gui, inventory, title -> TraitListScreen(gui, inventory, title) }
    HandledScreens.register(SeedBagCard.screenHandlerType) { gui, inventory, title -> SeedBagScreen(gui, inventory, title) }
}

class TraitListScreen(handler: TraitListScreenHandler, private val playerInventory: PlayerInventory, title: Text) : BaseOwoHandledScreen<FlowLayout, TraitListScreenHandler>(handler, playerInventory, title) {
    private lateinit var traitCardContainer: FlowLayout

    private fun setTraitCardContent(component: Component?) {
        traitCardContainer.clearChildren()
        if (component != null) traitCardContainer.child(component)
        uiAdapter.inflateAndMount()
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply { // 外枠
                surface(Surface.PANEL)
                padding(Insets.of(7))

                child(Containers.verticalFlow(Sizing.fixed(18 * 9), Sizing.content()).apply { // 内枠
                    child(inventoryNameLabel(traitListScreenTranslation(), HorizontalAlignment.CENTER)) // GUI名
                    child(verticalSpace(3))
                    traitCardContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(18 * 6)).apply { // 特性カードコンテナ
                        surface(NinePatchTextureCard.TRAIT_BACKGROUND.surface)
                        padding(Insets.of(5))
                    }
                    child(traitCardContainer)
                    child(verticalSpace(4))
                    child(Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(18 * 4)).apply { // 特性リスト
                        surface(Surface.TOOLTIP)
                        padding(Insets.of(5))

                        child(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5).apply {
                            scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                            child().child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                                handler.traitStacks.traitStackList.forEach { traitStack ->
                                    child(ClickableContainer(Sizing.fill(100), Sizing.content(), { // 特性
                                        setTraitCardContent(createTraitCardContent(traitStack))
                                        true
                                    }) {
                                        Components.label(text {
                                            val texts = mutableListOf<Text>()
                                            texts += traitStack.trait.getName().style(traitStack.trait.style)
                                            texts += traitStack.level.toString(2)().style(traitStack.trait.style)
                                            if (traitStack.trait.conditions.isNotEmpty()) texts += traitStack.trait.conditions.map { it.emoji }.join() + " →"()
                                            if (traitStack.trait.effectStacks.isNotEmpty()) texts += traitStack.trait.effectStacks.map { it.first.emoji.style(Style.EMPTY.withColor(it.first.color)) }.join()
                                            texts.join(" "())
                                        })
                                    })
                                }
                            })
                        })
                    })
                })
            })
        }

        val traitStack = handler.traitStacks.traitStackList.firstOrNull()
        if (traitStack != null) setTraitCardContent(createTraitCardContent(traitStack))
    }

    private fun createTraitCardContent(traitStack: TraitStack): Component {
        return topBorderLayout(Sizing.fill(100), Sizing.fill(100)).apply {
            gap = 5

            child1(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                child(Components.label(text { traitStack.trait.getName().style(traitStack.trait.style) }).apply { // 特性名
                    sizing(Sizing.fill(100), Sizing.content())
                    horizontalTextAlignment(HorizontalAlignment.CENTER)
                })
                child(verticalSpace(5))
                child(Containers.stack(Sizing.fill(100), Sizing.fixed(32)).apply { // 特性アイコン欄
                    val player = MinecraftClient.getInstance().player
                    val factor = if (player != null) {
                        traitStack.trait.conditions
                            .map { it.getFactor(player.world, player.blockPos) }
                            .fold(1.0) { a, b -> a * b }
                    } else {
                        1.0
                    }
                    child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 条件
                        horizontalAlignment(HorizontalAlignment.LEFT)
                        verticalAlignment(VerticalAlignment.BOTTOM)

                        traitStack.trait.conditions.forEach { condition ->
                            val factor = if (player != null) condition.getFactor(player.world, player.blockPos) else 1.0
                            val text = text { condition.emoji + " "() + (factor * 100.0 formatAs "%.1f%%")() }
                            child(Components.label(text).tooltip(condition.name))
                        }
                    })
                    child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 特性アイコン
                        horizontalAlignment(HorizontalAlignment.CENTER)
                        verticalAlignment(VerticalAlignment.CENTER)

                        child(Components.texture(traitStack.trait.texture, 0, 0, 32, 32, 32, 32))
                    })
                    child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 効果
                        horizontalAlignment(HorizontalAlignment.RIGHT)
                        verticalAlignment(VerticalAlignment.BOTTOM)

                        traitStack.trait.effectStacks.forEach {
                            val text = text { (it.second * traitStack.level * factor * 100.0 formatAs "%.1f%%")() + " "() + it.first.emoji.style(Style.EMPTY.withColor(it.first.color)) }
                            val tooltip = text { it.first.name + " ("() + (it.second * traitStack.level * 100.0 formatAs "%.1f%%")() + " x "() + (factor * 100.0 formatAs "%.1f%%")() + ")"() }
                            child(Components.label(text).tooltip(tooltip))
                        }
                    })
                })
            })
            child2(verticalScroll(Sizing.fill(100), Sizing.fill(100), 5, overlapped = true).apply { // 特性ポエム
                scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF.toInt())))

                child().child(Components.label(text { traitStack.trait.poem }).apply {
                    sizing(Sizing.fill(100), Sizing.content())
                    horizontalTextAlignment(HorizontalAlignment.LEFT)
                })
            })
        }
    }
}

class SeedBagScreen(handler: SeedBagScreenHandler, private val playerInventory: PlayerInventory, title: Text) : BaseOwoHandledScreen<FlowLayout, SeedBagScreenHandler>(handler, playerInventory, title) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply { // 外枠
                surface(Surface.PANEL)
                padding(Insets.of(7))

                child(Containers.verticalFlow(Sizing.fixed(18 * SeedBagItem.INVENTORY_WIDTH), Sizing.content()).apply { // 内枠

                    child(inventoryNameLabel(title)) // GUI名

                    child(verticalSpace(3))

                    // カバンインベントリ
                    repeat(6) { r ->
                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                            repeat(SeedBagItem.INVENTORY_WIDTH) { c ->
                                child(slotContainer(slotAsComponent(9 * 4 + SeedBagItem.INVENTORY_WIDTH * r + c)))
                            }
                        })
                    }

                    child(verticalSpace(3))

                    child(inventoryNameLabel(playerInventory.name))

                    child(verticalSpace(1))

                    // プレイヤーインベントリ
                    repeat(3) { r ->
                        child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                            child(horizontalSpace(18 * 4))
                            repeat(9) { c ->
                                child(slotContainer(slotAsComponent(9 * r + c)))
                            }
                        })
                    }
                    child(verticalSpace(4))
                    child(Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).apply {
                        child(horizontalSpace(18 * 4))
                        repeat(9) { c ->
                            child(slotContainer(slotAsComponent(9 * 3 + c)))
                        }
                    })

                })
            })
        }
    }
}
