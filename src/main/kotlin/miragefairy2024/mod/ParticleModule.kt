package miragefairy2024.mod

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.register
import miragefairy2024.util.string
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.particle.DefaultParticleType
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

enum class ParticleTypeCard(
    path: String,
    textureName: String,
    alwaysSpawn: Boolean,
) {
    MISSION("mission", "mission", true),
    COLLECTING_MAGIC("collecting_magic", "magic", false),
    DESCENDING_MAGIC("descending_magic", "magic", false),
    MIRAGE_FLOUR("mirage_flour", "mirage_flour", false),
    ATTRACTING_MAGIC("attracting_magic", "mission", false),
    AURA("aura", "mission", false),
    CHAOS_STONE("chaos_stone", "chaos_stone", false),
    HAIMEVISKA_BLOSSOM("haimeviska_blossom", "haimeviska_blossom", false),
    DRIPPING_HAIMEVISKA_SAP("dripping_haimeviska_sap", "minecraft:drip_hang", false),
    FALLING_HAIMEVISKA_SAP("falling_haimeviska_sap", "minecraft:drip_fall", false),
    LANDING_HAIMEVISKA_SAP("landing_haimeviska_sap", "minecraft:drip_land", false),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val texture = if (":" in textureName) Identifier(textureName) else MirageFairy2024.identifier(textureName)
    val particleType: DefaultParticleType = FabricParticleTypes.simple(alwaysSpawn)
}

context(ModContext)
fun initParticleModule() {
    ParticleTypeCard.entries.forEach { card ->
        card.particleType.register(Registries.PARTICLE_TYPE, card.identifier)
        DataGenerationEvents.onGenerateParticles {
            val data = jsonObject(
                "textures" to jsonArray(
                    card.texture.string.jsonElement,
                ),
            )
            it(card.identifier, data)
        }
    }
}
