package miragefairy2024.client.mod

import io.wispforest.owo.ui.core.Surface
import io.wispforest.owo.ui.util.NinePatchTexture
import miragefairy2024.mod.NinePatchTextureCard

val NinePatchTextureCard.surface get() = Surface { context, component -> NinePatchTexture.draw(this.identifier, context, component) }
