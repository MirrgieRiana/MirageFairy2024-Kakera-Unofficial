package miragefairy2024.mixins.api;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface ExtraPlayerDataContainerGetter {
    @NotNull
    Object mirageFairy2024$getExtraPlayerDataContainer(Function<@NotNull Player, @NotNull Object> creator);
}
