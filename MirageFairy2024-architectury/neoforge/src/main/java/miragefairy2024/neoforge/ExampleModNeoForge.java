package miragefairy2024.neoforge;

import net.neoforged.fml.common.Mod;

import miragefairy2024.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
