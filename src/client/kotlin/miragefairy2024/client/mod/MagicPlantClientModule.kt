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
import miragefairy2024.mod.magicplant.TraitListScreenHandler
import miragefairy2024.mod.magicplant.traitListScreenHandlerType
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

            // GUI外枠描画用
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                surface(Surface.PANEL)
                padding(Insets.of(7))

                // 横幅固定メインコンテナ
                child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                    surface(Surface.TOOLTIP)
                    padding(Insets.of(7))

                    child(Components.texture(Identifier(MirageFairy2024.modId, "textures/gui/traits/waterlogging_tolerance.png"), 0, 0, 32, 32, 32, 32).apply {

                    })

                })

            })

        }
    }
}
