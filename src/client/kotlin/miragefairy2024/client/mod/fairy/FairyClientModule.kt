package miragefairy2024.client.mod.fairy

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.HorizontalAlignment
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.core.VerticalAlignment
import miragefairy2024.MirageFairy2024
import miragefairy2024.api.client.inputEventsHandlers
import miragefairy2024.client.util.LimitedLabelComponent
import miragefairy2024.client.util.createOwoToast
import miragefairy2024.client.util.horizontalSpace
import miragefairy2024.client.util.registerClientPacketReceiver
import miragefairy2024.client.util.sendToServer
import miragefairy2024.client.util.verticalSpace
import miragefairy2024.mod.fairy.GAIN_FAIRY_DREAM_TRANSLATION
import miragefairy2024.mod.fairy.GainFairyDreamChannel
import miragefairy2024.mod.fairy.OPEN_SOUL_STREAM_KEY_TRANSLATION
import miragefairy2024.mod.fairy.OpenSoulStreamChannel
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.motifTableScreenHandlerType
import miragefairy2024.mod.fairy.soulStreamScreenHandlerType
import miragefairy2024.util.black
import miragefairy2024.util.darkBlue
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.text
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.gui.components.WidgetSprites
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import net.minecraft.client.KeyMapping as KeyBinding
import net.minecraft.client.Minecraft as MinecraftClient
import net.minecraft.client.gui.components.ImageButton as TexturedButtonWidget
import net.minecraft.client.gui.screens.MenuScreens as HandledScreens

lateinit var soulStreamKey: KeyBinding

var lastMousePositionInInventory: Pair<Double, Double>? = null

fun initFairyClientModule() {

    // GUI登録
    HandledScreens.register(motifTableScreenHandlerType) { gui, inventory, title -> MotifTableScreen(gui, inventory, title) }
    HandledScreens.register(soulStreamScreenHandlerType) { gui, inventory, title -> SoulStreamScreen(gui, inventory, title) }

    // パケットハンドラ登録
    GainFairyDreamChannel.registerClientPacketReceiver { motif ->
        val itemStack = motif.createFairyItemStack()
        val component = Containers.horizontalFlow(Sizing.fixed(160), Sizing.fixed(32)).apply {
            surface(Surface.tiled(MirageFairy2024.identifier("textures/gui/fairy_dream_toast.png"), 160, 32))
            padding(Insets.of(0, 0, 8, 8))
            verticalAlignment(VerticalAlignment.CENTER)
            child(Components.item(itemStack))
            child(horizontalSpace(6))
            child(Containers.verticalFlow(Sizing.content(), Sizing.content()).apply {
                child(Components.label(text { GAIN_FAIRY_DREAM_TRANSLATION().black }))
                child(verticalSpace(2))
                child(LimitedLabelComponent(itemStack.hoverName.darkBlue).horizontalSizing(Sizing.fixed(160 - 8 - 16 - 6 - 10 - 8)).margins(Insets.of(0, 0, 4, 0)))
            })
        }
        MinecraftClient.getInstance().toasts.addToast(createOwoToast(component))
    }

    // ソウルストリームのキーバインド
    soulStreamKey = KeyBinding(OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter(), GLFW.GLFW_KEY_K, KeyBinding.CATEGORY_INVENTORY)
    inputEventsHandlers += {
        while (soulStreamKey.consumeClick()) {
            lastMousePositionInInventory = null
            OpenSoulStreamChannel.sendToServer(Unit)
        }
    }
    KeyBindingHelper.registerKeyBinding(soulStreamKey)

    // インベントリ画面にソウルストリームのボタンを設置
    ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
        if (screen is InventoryScreen) {

            val onMouseClick = mutableListOf<() -> Unit>()

            // 中央揃えコンテナ
            val uiAdapter = OwoUIAdapter.create(screen, Containers::stack)
            uiAdapter.rootComponent.apply {
                alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)

                // 位置決定用パネル
                child(Containers.stack(Sizing.content(), Sizing.content()).apply {
                    alignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)

                    fun updatePosition() {
                        if (!screen.recipeBookComponent.isVisible) {
                            sizing(Sizing.fixed(146), Sizing.fixed(46))
                        } else {
                            sizing(Sizing.fixed(300), Sizing.fixed(46))
                        }
                    }
                    updatePosition()
                    onMouseClick += {
                        updatePosition()
                        uiAdapter.inflateAndMount()
                    }

                    // ボタン
                    val buttonWidgetSprites = WidgetSprites(MirageFairy2024.identifier("soul_stream_button"), MirageFairy2024.identifier("soul_stream_button_highlighted"))
                    child(Components.wrapVanillaWidget(TexturedButtonWidget(0, 0, 20, 20, buttonWidgetSprites) {
                        lastMousePositionInInventory = Pair(MinecraftClient.getInstance().mouseHandler.xpos(), MinecraftClient.getInstance().mouseHandler.ypos())
                        screen.onClose()
                        OpenSoulStreamChannel.sendToServer(Unit)
                    }).apply {
                        tooltip(text { OPEN_SOUL_STREAM_KEY_TRANSLATION() + "("() + Component.keybind(OPEN_SOUL_STREAM_KEY_TRANSLATION.keyGetter()) + ")"() })
                    })

                })

            }
            uiAdapter.inflateAndMount()

            ScreenMouseEvents.afterMouseClick(screen).register { _, _, _, _ ->
                onMouseClick.forEach {
                    it()
                }
            }

        }
    }

}
