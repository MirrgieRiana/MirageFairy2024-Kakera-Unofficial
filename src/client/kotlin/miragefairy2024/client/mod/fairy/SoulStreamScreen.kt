package miragefairy2024.client.mod.fairy

import io.wispforest.owo.ui.base.BaseOwoHandledScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.container.ScrollContainer
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.client.util.inventoryNameLabel
import miragefairy2024.client.util.slotContainer
import miragefairy2024.client.util.verticalScroll
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.fairy.SoulStreamScreenHandler
import miragefairy2024.util.size
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

class SoulStreamScreen(handler: SoulStreamScreenHandler, playerInventory: PlayerInventory, title: Text) : BaseOwoHandledScreen<FlowLayout, SoulStreamScreenHandler>(handler, playerInventory, title) {

    // カーソルをインベントリ画面での位置に戻す
    private var isFirst = true
    override fun render(vanillaContext: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        if (isFirst) {
            isFirst = false
            if (lastMousePositionInInventory != null) {
                InputUtil.setCursorParameters(this.client!!.window.handle, InputUtil.GLFW_CURSOR_NORMAL, lastMousePositionInInventory!!.first, lastMousePositionInInventory!!.second)
            }
        }
        super.render(vanillaContext, mouseX, mouseY, delta)
    }

    override fun createAdapter(): OwoUIAdapter<FlowLayout> = OwoUIAdapter.create(this, Containers::verticalFlow)
    override fun build(rootComponent: FlowLayout) {
        rootComponent.apply {
            surface(Surface.VANILLA_TRANSLUCENT)
            verticalAlignment(VerticalAlignment.CENTER)
            horizontalAlignment(HorizontalAlignment.CENTER)

            // GUIパネル外枠
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                surface(Surface.PANEL)
                padding(Insets.of(7))

                // メインコンテナ
                child(Containers.verticalFlow(Sizing.fixed(18 * 9 + 18), Sizing.content()).apply {

                    child(inventoryNameLabel(title))

                    child(verticalSpace(3))

                    child(verticalScroll(Sizing.fill(), Sizing.fixed(18 * 6), 18).apply {
                        scrollbar(ScrollContainer.Scrollbar.vanilla())
                        (0 until handler.soulStream.size).chunked(9).forEach { indices ->
                            child().child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                                indices.forEach { index ->
                                    child(slotContainer(slotAsComponent(9 * 3 + 9 + index)))
                                }
                            })
                        }
                    })

                    child(verticalSpace(3))

                    child(inventoryNameLabel(handler.playerInventory.name))

                    child(verticalSpace(1))

                    // プレイヤーインベントリ
                    repeat(3) { r ->
                        child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                            repeat(9) { c ->
                                child(slotContainer(slotAsComponent(9 * r + c)))
                            }
                        })
                    }
                    child(verticalSpace(4))
                    child(Containers.horizontalFlow(Sizing.fill(), Sizing.content()).apply {
                        repeat(9) { c ->
                            child(slotContainer(slotAsComponent(9 * 3 + c)))
                        }
                    })

                })

            })

        }
    }

    override fun drawForeground(context: DrawContext, mouseX: Int, mouseY: Int) = Unit

    // キー入力で閉じる
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (soulStreamKey.matchesKey(keyCode, scanCode)) {
            close()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

}
