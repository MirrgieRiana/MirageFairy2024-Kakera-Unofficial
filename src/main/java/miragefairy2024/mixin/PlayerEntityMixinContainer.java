package miragefairy2024.mixin;

import miragefairy2024.mod.ExtraPlayerDataContainer;
import miragefairy2024.mod.ExtraPlayerDataContainerGetter;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixinContainer implements ExtraPlayerDataContainerGetter {
    @Unique
    private final ExtraPlayerDataContainer mirageFairy2024$extraPlayerDataContainer = new ExtraPlayerDataContainer((PlayerEntity) (Object) this);

    @Override
    public @NotNull ExtraPlayerDataContainer mirageFairy2024$getExtraPlayerDataContainer() {
        return mirageFairy2024$extraPlayerDataContainer;
    }
}
