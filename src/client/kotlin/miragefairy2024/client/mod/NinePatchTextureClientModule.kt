package miragefairy2024.mod

import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.util.NinePatchTexture

val NinePatchTextureCard.surface get() = Surface { context, component -> NinePatchTexture.draw(this.identifier, context, component) }
