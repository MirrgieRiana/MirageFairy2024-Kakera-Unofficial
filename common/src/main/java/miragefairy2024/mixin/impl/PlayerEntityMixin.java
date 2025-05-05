package miragefairy2024.mixin.impl;

import miragefairy2024.mixin.api.ExtraPlayerDataContainerGetter;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements ExtraPlayerDataContainerGetter {
    @Unique
    private Object mirageFairy2024$extraPlayerDataContainer;

    @Override
    public @NotNull Object mirageFairy2024$getExtraPlayerDataContainer(Function<@NotNull Player, @NotNull Object> creator) {
        if (mirageFairy2024$extraPlayerDataContainer == null) mirageFairy2024$extraPlayerDataContainer = creator.apply((Player) (Object) this);
        return mirageFairy2024$extraPlayerDataContainer;
    }
}
