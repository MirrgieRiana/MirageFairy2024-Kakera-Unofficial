package miragefairy2024.client.mod

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.Color
import io.wispforest.owo.ui.core.Component as OwoComponent
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.client.util.ClickableContainer
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.topBorderLayout
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.TraitListScreenHandler
import miragefairy2024.mod.magicplant.TraitStack
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.style
import miragefairy2024.mod.magicplant.texture
import miragefairy2024.mod.magicplant.traitListScreenHandlerType
import miragefairy2024.mod.magicplant.traitListScreenTranslation
import miragefairy2024.util.invoke
import miragefairy2024.util.join
import miragefairy2024.util.plus
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.gui.screens.MenuScreens as HandledScreens
import net.minecraft.world.entity.player.Inventory as PlayerInventory
import net.minecraft.network.chat.Component

fun initMagicPlantClientModule() {
    HandledScreens.register(traitListScreenHandlerType) { gui, inventory, title -> TraitListScreen(gui, inventory, title) }
}

class TraitListScreen(handler: TraitListScreenHandler, playerInventory: PlayerInventory, title: Component) : BaseOwoHandledScreen<FlowLayout, TraitListScreenHandler>(handler, playerInventory, title) {
    private lateinit var traitCardContainer: FlowLayout

    private fun setTraitCardContent(component: OwoComponent?) {
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
                    child(inventoryNameLabel(text { traitListScreenTranslation() }, HorizontalAlignment.CENTER)) // GUI名
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
                                menu.traitStacks.traitStackList.forEach { traitStack ->
                                    child(ClickableContainer(Sizing.fill(100), Sizing.content(), { // 特性
                                        setTraitCardContent(createTraitCardContent(traitStack))
                                        true
                                    }) {
                                        Components.label(text {
                                            val texts = mutableListOf<Component>()
                                            texts += traitStack.trait.getName().style(traitStack.trait.style)
                                            texts += traitStack.level.toString(2)().style(traitStack.trait.style)
                                            if (traitStack.trait.conditions.isNotEmpty()) texts += traitStack.trait.conditions.map { it.emoji }.join() + " →"()
                                            if (traitStack.trait.effectStacks.isNotEmpty()) texts += traitStack.trait.effectStacks.map { it.first.emoji.style(it.first.style) }.join()
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

        val traitStack = menu.traitStacks.traitStackList.firstOrNull()
        if (traitStack != null) setTraitCardContent(createTraitCardContent(traitStack))
    }

    private fun createTraitCardContent(traitStack: TraitStack): OwoComponent {
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
                    val allFactor = if (player != null) {
                        traitStack.trait.conditions
                            .map { it.getFactor(player.level(), player.blockPosition()) }
                            .fold(1.0) { a, b -> a * b }
                    } else {
                        1.0
                    }
                    child(Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).apply { // 条件
                        horizontalAlignment(HorizontalAlignment.LEFT)
                        verticalAlignment(VerticalAlignment.BOTTOM)

                        traitStack.trait.conditions.forEach { condition ->
                            val factor = if (player != null) condition.getFactor(player.level(), player.blockPosition()) else 1.0
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
                            val text = text { (it.second * traitStack.level * allFactor * 100.0 formatAs "%.1f%%")() + " "() + it.first.emoji.style(it.first.style) }
                            val tooltip = text { it.first.name + " ("() + (it.second * traitStack.level * 100.0 formatAs "%.1f%%")() + " x "() + (allFactor * 100.0 formatAs "%.1f%%")() + ")"() }
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
