package miragefairy2024.mixin.impl;

import miragefairy2024.mod.ExtraPlayerDataContainer;
import miragefairy2024.mod.ExtraPlayerDataContainerGetter;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public abstract class PlayerEntityMixin implements ExtraPlayerDataContainerGetter {
    @Unique
    private final ExtraPlayerDataContainer mirageFairy2024$extraPlayerDataContainer = new ExtraPlayerDataContainer((Player) (Object) this);

    @Override
    public @NotNull ExtraPlayerDataContainer mirageFairy2024$getExtraPlayerDataContainer() {
        return mirageFairy2024$extraPlayerDataContainer;
    }
}
