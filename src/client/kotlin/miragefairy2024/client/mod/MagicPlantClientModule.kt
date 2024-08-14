package miragefairy2024.client.mod

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.MirageFairy2024
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.TraitListScreenHandler
import miragefairy2024.mod.magicplant.getName
import miragefairy2024.mod.magicplant.traitListScreenHandlerType
import miragefairy2024.mod.magicplant.traitListScreenTranslation
import miragefairy2024.util.invoke
import miragefairy2024.util.style
import miragefairy2024.util.text
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Identifier

fun initMagicPlantClientModule() {
    HandledScreens.register(traitListScreenHandlerType) { gui, inventory, title -> TraitListScreen(gui, inventory, title) }
}

class TraitListScreen(handler: TraitListScreenHandler, private val playerInventory: PlayerInventory, title: Text) : BaseOwoHandledScreen<FlowLayout, TraitListScreenHandler>(handler, playerInventory, title) {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply { // 外枠
                surface(Surface.PANEL)
                padding(Insets.of(7))

                child(inventoryNameLabel(traitListScreenTranslation(), HorizontalAlignment.CENTER)) // GUI名
                child(verticalSpace(3))
                child(Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply { // リスト・特性セクション
                    child(verticalScroll(Sizing.fixed(80), Sizing.fixed(160), 5).apply { // リスト
                        surface(Surface.TOOLTIP)
                        padding(Insets.of(5))

                        child().child(Containers.verticalFlow(Sizing.fill(100), Sizing.content()).apply {
                            handler.traitStacks.traitStackList.flatMap { listOf(it, it, it, it, it, it, it, it)/* TODO */ }.forEach { traitStack ->
                                child(Components.label(text { (traitStack.trait.getName() + " ${traitStack.level}"()).style(traitStack.trait.style) }))
                            }
                        })
                    })
                    child(horizontalSpace(2))
                    child(Containers.verticalFlow(Sizing.fixed(80), Sizing.fixed(160)).apply { // 特性カード
                        surface(NinePatchTextureCard.TRAIT_BACKGROUND.surface)
                        padding(Insets.of(5))

                        child(Components.texture(Identifier(MirageFairy2024.modId, "textures/gui/traits/waterlogging_tolerance.png"), 0, 0, 32, 32, 32, 32).apply {

                        })

                    })
                })
            })
        }
    }
}
