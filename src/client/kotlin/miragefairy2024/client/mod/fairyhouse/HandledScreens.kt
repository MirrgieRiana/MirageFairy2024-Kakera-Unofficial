package miragefairy2024.client.mod.fairyhouse

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.fairyhouse.FairyCollectorCard
import miragefairy2024.mod.fairyhouse.FairyCollectorScreenHandler
import miragefairy2024.mod.fairyhouse.FairyFactoryScreenHandler
import miragefairy2024.mod.fairyhouse.FairyHouseCard
import net.minecraft.util.Identifier

class FairyHouseScreen(arguments: Arguments<FairyFactoryScreenHandler>) : FairyFactoryHandledScreen<FairyFactoryScreenHandler>(FairyHouseCard, arguments) {
    companion object {
        private val TEXTURE = Identifier(MirageFairy2024.modId, "textures/gui/container/fairy_house.png")
    }

    override fun getBackgroundTexture() = TEXTURE

}

class FairyCollectorScreen(arguments: Arguments<FairyCollectorScreenHandler>) : FairyFactoryHandledScreen<FairyCollectorScreenHandler>(FairyCollectorCard, arguments) {
    companion object {
        private val TEXTURE = Identifier(MirageFairy2024.modId, "textures/gui/container/fairy_collector.png")
    }

    override fun getBackgroundTexture() = TEXTURE

}
