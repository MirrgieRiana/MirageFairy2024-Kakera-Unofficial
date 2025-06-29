package miragefairy2024.client.mixins.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public interface RenderingEvent {
    Event<RenderItemDecorationsCallback> RENDER_ITEM_DECORATIONS = EventFactory.createArrayBacked(RenderItemDecorationsCallback.class, callbacks -> (graphics, font, stack, x, y, text) -> {
        for (RenderItemDecorationsCallback callback : callbacks) {
            callback.renderItemDecorations(graphics, font, stack, x, y, text);
        }
    });

    interface RenderItemDecorationsCallback {
        void renderItemDecorations(GuiGraphics graphics, Font font, ItemStack stack, int x, int y, String text);
    }
}
