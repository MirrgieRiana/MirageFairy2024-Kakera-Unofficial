package miragefairy2024.client.mod.fairyquest

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.TextureComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.PositionedRectangle
import io.wispforest.owo.ui.core.Sizing
import miragefairy2024.MirageFairy2024
import kotlin.math.roundToInt

private val FAIRY_QUEST_PROGRESS_BACKGROUND = MirageFairy2024.identifier("textures/gui/fairy_quest_progress_background.png")
private val FAIRY_QUEST_PROGRESS_OVERLAY = MirageFairy2024.identifier("textures/gui/fairy_quest_progress_overlay.png")

class FairyQuestProgress {
    private val textureComponent: TextureComponent
    val component = Containers.stack(Sizing.fixed(18), Sizing.fixed(18)).apply {
        padding(Insets.of(1))
        child(Components.texture(FAIRY_QUEST_PROGRESS_BACKGROUND, 0, 0, 16, 16, 16, 16))
        child(Components.texture(FAIRY_QUEST_PROGRESS_OVERLAY, 0, 0, 16, 16, 16, 16).apply {
            textureComponent = this
            visibleArea(PositionedRectangle.of(0, 0, 0, 16))
        })
    }

    fun setProgress(progress: Double) {
        textureComponent.visibleArea(PositionedRectangle.of(0, 0, (16 * progress).roundToInt(), 16))
    }
}
