package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier

val motifRegistryKey: RegistryKey<Registry<Motif>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "motif"))
val motifRegistry: Registry<Motif> = FabricRegistryBuilder.createSimple(motifRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

interface Motif {
    val displayName: Text
    val skinColor: Int
    val frontColor: Int
    val backColor: Int
    val hairColor: Int
}


enum class MotifCard(
    path: String,
    enName: String,
    jaName: String,
    override val skinColor: Int,
    override val frontColor: Int,
    override val backColor: Int,
    override val hairColor: Int,
) : Motif {
    AIR(
        "air", "Airia", "空気精アイリャ",
        0xFFBE80, 0xDEFFFF, 0xDEFFFF, 0xB0FFFF,
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val translation = Translation({ "miragefairy2024.motif.${identifier.toTranslationKey()}" }, enName, jaName)
    override val displayName = translation()
}
