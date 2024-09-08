package miragefairy2024.mod.magicplant.contents

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.BiomeCards
import miragefairy2024.mod.FAIRY_BIOME_TAG
import miragefairy2024.mod.magicplant.MutableTraitEffects
import miragefairy2024.mod.magicplant.Trait
import miragefairy2024.mod.magicplant.TraitSpawnCondition
import miragefairy2024.mod.magicplant.TraitSpawnConditionScope
import miragefairy2024.mod.magicplant.TraitSpawnRarity
import miragefairy2024.mod.magicplant.TraitSpawnSpec
import miragefairy2024.mod.magicplant.anywhere
import miragefairy2024.mod.magicplant.enJa
import miragefairy2024.mod.magicplant.end
import miragefairy2024.mod.magicplant.nether
import miragefairy2024.mod.magicplant.overworld
import miragefairy2024.mod.magicplant.traitRegistry
import miragefairy2024.mod.magicplant.unaryPlus
import miragefairy2024.util.HumidityCategory
import miragefairy2024.util.TemperatureCategory
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeKeys

class TraitCard(
    path: String,
    val enName: String,
    val jaName: String,
    enPoem: String,
    jaPoem: String,
    private val traitConditionCards: List<TraitConditionCard>,
    private val traitEffectKeyCardStacks: List<Pair<TraitEffectKeyCard, Double>>,
    private val spawnSpecConfigurator: MutableList<TraitSpawnSpec>.() -> Unit,
) {
    companion object {
        val entries = mutableListOf<TraitCard>()
        operator fun TraitCard.not() = also { entries += this }

        val ETHER_RESPIRATION = !TraitCard(
            "ether_respiration", "Ether Respiration", "エーテル呼吸",
            "Through the action of the astral radiation, ether is vaporized, generating a vortex of souls. Since the appearance of etherobacteria billions of years ago, the universe has been filled with life forms possessing will.",
            "情緒体の作用によってエーテルを気化し、魂の渦を生成する。数十億年前にエテロバクテリアが姿を現して以来、宇宙には意志を持った生命で溢れた。",
            listOf(), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.ALWAYS)
            register("0100", TraitSpawnRarity.RARE) { end }
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.S_RARE)
        }
        val PHOTOSYNTHESIS = !TraitCard(
            "photosynthesis", "Photosynthesis", "光合成",
            "Through the Dephlogistication Reaction, oxygen and organic matter are produced from water and carbon dioxide. Photosynthesis is a fundamental life activity of plants, alongside etheric respiration, and it supports the existence of many organisms, including herbivores, carnivores, and humans.",
            "脱フロギストン反応により、水と二酸化炭素から酸素と有機物が生産される。光合成はエーテル呼吸と並ぶ植物の生命活動の根幹であり、草食動物、肉食動物、人間など、多くの生物の存在を成り立たせている。",
            listOf(TraitConditionCard.LIGHT), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.SNOWY }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.DESERT }
            register("0010", TraitSpawnRarity.COMMON) { overworld }
            register("0001", TraitSpawnRarity.RARE) { overworld }
        }
        val PHAEOSYNTHESIS = !TraitCard(
            "phaeosynthesis", "Phaeosynthesis", "闇合成",
            "By fluctuations in aura concentration, ether crystals grow from organic elements in the environment. This process is the same as the formation of undead creatures.",
            "オーラ濃度のゆらぎにより、環境中の有機元素からエーテル結晶を成長させる。これはアンデッド生物の形成と同じ作用である。",
            listOf(TraitConditionCard.DARKNESS), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.NORMAL) { nether }
            register("0100", TraitSpawnRarity.RARE) { +BiomeKeys.SOUL_SAND_VALLEY }
            register("0010", TraitSpawnRarity.S_RARE) { nether }
            register("0001", TraitSpawnRarity.NORMAL) { +BiomeKeys.DEEP_DARK }
        }
        val OSMOTIC_ABSORPTION = !TraitCard(
            "osmotic_absorption", "Nutrient Absorption", "養分吸収",
            "When the ion concentration in the root cells is higher than that in the surrounding soil, osmosis causes the water in the soil, along with nutrients, to be absorbed into the roots. This process requires the transpiration action of the leaves.",
            "根の細胞のイオン濃度が周囲の土中に比べて高いとき、浸透圧によって土中の水分が養分とともに根に吸収される。この過程には葉の蒸散作用が不可欠である。",
            listOf(TraitConditionCard.FLOOR_MOISTURE), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.COMMON) { +BiomeKeys.SOUL_SAND_VALLEY }
            register("0100", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.PLAINS }
            register("0010", TraitSpawnRarity.COMMON) { overworld }
            register("0001", TraitSpawnRarity.RARE) { overworld }
        }
        val CRYSTAL_ABSORPTION = !TraitCard(
            "crystal_absorption", "Crystal Absorption", "鉱物吸収",
            "The shine aura contained within the gemstone is excited by natural astral rays, and the replication erg generated by enzymatic action triggers the self-replication of plant tissues.",
            "宝石質に含まれる光のオーラが自然アストラル線によって励起され、酵素の作用で生成された増殖のエルグが植物組織の自己複製作用を誘発する。",
            listOf(TraitConditionCard.FLOOR_CRYSTAL_ERG), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.BEACH }
            register("0001", TraitSpawnRarity.NORMAL) { nether }
        }

        val AIR_ADAPTATION = !TraitCard(
            "air_adaptation", "Spatial Adaptation", "空間適応",
            "By decomposing ether with high-energy astral radiation, life support is maintained in outer space. This process is essential for the terraforming of rocky planets.",
            "高エネルギーアストラル放射線によってエーテルを分解することで、宇宙空間での生命維持を行う。この作用は岩石惑星のテラフォーミングの上で重要である。",
            listOf(), listOf(TraitEffectKeyCard.NUTRITION to 0.03, TraitEffectKeyCard.TEMPERATURE to 0.03, TraitEffectKeyCard.HUMIDITY to 0.03),
        ) {
            register("1000", TraitSpawnRarity.ALWAYS)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.S_RARE)
        }
        val COLD_ADAPTATION = !TraitCard(
            "cold_adaptation", "Cold Adaptation", "低温適応",
            "Only plants that have adapted to low solar radiation, low temperatures, water freezing, snow cover, and harsh cold environments can survive under the extreme cold of the ice.",
            "低い日射量、低い気温、水分の凍結、雪による遮蔽、過酷な低温環境に適応した植物だけが極寒の氷の下で生き残ることができる。",
            listOf(TraitConditionCard.LOW_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.LOW }
            register("0100", TraitSpawnRarity.COMMON) { +TemperatureCategory.LOW }
            register("0010", TraitSpawnRarity.RARE) { +TemperatureCategory.LOW }
            register("0001", TraitSpawnRarity.NORMAL) { +TemperatureCategory.LOW }
        }
        val WARM_ADAPTATION = !TraitCard(
            "warm_adaptation", "Warm Adaptation", "中温適応",
            "In environments with comfortable temperatures, plants can save evolutionary energy that would otherwise be allocated to cold or heat resistance. As a result, they are able to devote more energy to growth and reproduction.",
            "快適な気温の環境では、耐寒や耐暑に割り当てる進化的エネルギーを節約できる。その結果、植物はより多くのエネルギーを成長や繁殖に割くことができる。",
            listOf(TraitConditionCard.MEDIUM_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.MEDIUM }
            register("0100", TraitSpawnRarity.COMMON) { +TemperatureCategory.MEDIUM }
            register("0010", TraitSpawnRarity.RARE) { +TemperatureCategory.MEDIUM }
            register("0001", TraitSpawnRarity.NORMAL) { +TemperatureCategory.MEDIUM }
        }
        val HOT_ADAPTATION = !TraitCard(
            "hot_adaptation", "Hot Adaptation", "高温適応",
            "The jungle, a paradise for plants, the scorching desert, the fiery inferno—though all these are high-temperature environments, their actual conditions vary greatly. This trait is merely a collection of minor techniques that prove useful across various high-temperature environments.",
            "植物の楽園であるジャングル地帯、炎天下の砂漠、灼熱地獄、一口に高温環境といっても、その実態は様々である。この形質は、高温環境全般で役に立つ小技の寄せ集めにすぎない。",
            listOf(TraitConditionCard.HIGH_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.HIGH }
            register("0100", TraitSpawnRarity.COMMON) { +TemperatureCategory.HIGH }
            register("0010", TraitSpawnRarity.RARE) { +TemperatureCategory.HIGH }
            register("0001", TraitSpawnRarity.NORMAL) { +TemperatureCategory.HIGH }
        }
        val ARID_ADAPTATION = !TraitCard(
            "arid_adaptation", "Arid Adaptation", "乾燥適応",
            "Evolution is often ruthless. The prosperity of the species as a whole does not necessarily lead to the happiness of each individual.",
            "進化とは、ときに非情である。種という全体の繁栄が、必ずしも個々の個体の幸福に繋がるとは限らないのだ。",
            listOf(TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.COMMON) { +HumidityCategory.LOW }
            register("0010", TraitSpawnRarity.RARE) { +HumidityCategory.LOW }
            register("0001", TraitSpawnRarity.NORMAL) { +HumidityCategory.LOW }
        }
        val MESIC_ADAPTATION = !TraitCard(
            "mesic_adaptation", "Mesic Adaptation", "中湿適応",
            "Moderate humidity provide comfort for plants as well, allowing a diverse range of species to thrive. In such an environment, a straightforward struggle for survival is required, with no possibility of escape or hiding, and no tricks. Winning in an ordinary world is the most challenging of all.",
            "極端でない湿度は、植物にとっても快適であり、多様な植物が繁栄する。ここでは、逃げも隠れもできない、小細工なしの生存競争が求められる。普通の世界で勝利することが最も難しいのだ。",
            listOf(TraitConditionCard.MEDIUM_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.MEDIUM }
            register("0100", TraitSpawnRarity.COMMON) { +HumidityCategory.MEDIUM }
            register("0010", TraitSpawnRarity.RARE) { +HumidityCategory.MEDIUM }
            register("0001", TraitSpawnRarity.NORMAL) { +HumidityCategory.MEDIUM }
        }
        val HUMID_ADAPTATION = !TraitCard(
            "humid_adaptation", "Humid Adaptation", "湿潤適応",
            "The plant that designs and operates the most efficient flow for converting H2O and CO2 into O2 and organic matter will ultimately dominate humid environments.",
            "H2OとCO2をO2と有機物に加工するフローを最も効率的に設計・運用した工場が、最終的に湿潤な環境を制覇する。",
            listOf(TraitConditionCard.HIGH_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.HIGH }
            register("0100", TraitSpawnRarity.COMMON) { +HumidityCategory.HIGH }
            register("0010", TraitSpawnRarity.RARE) { +HumidityCategory.HIGH }
            register("0001", TraitSpawnRarity.NORMAL) { +HumidityCategory.HIGH }
        }

        val SEEDS_PRODUCTION = !TraitCard(
            "seeds_production", "Seeds Production", "種子生成",
            "Strictly speaking, seeds refer to the reproductive structures formed in the lower part of the pistil. However, in many plants in the order Miragales, seeds tend to detach and disperse quickly. For convenience, bulbs are often treated as seeds in these plants.",
            "種子とは、狭義にはめしべの下部に形成される繁殖組織をいう。しかし、妖花目の植物は種子がすぐに欠落し拡散されるものが多いため、便宜上球根を種子同然に扱うことが多い。",
            listOf(), listOf(TraitEffectKeyCard.SEEDS_PRODUCTION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val FRUITS_PRODUCTION = !TraitCard(
            "fruits_production", "Fruits Production", "果実生成",
            "Human ancestors lived arboreal lives and primarily consumed fruit. While humans cannot eat raw meat, they can eat raw fruit. Interestingly, the nutritional values of fairies shares many similarities with that of common fruit.",
            "ヒトの祖先は樹上生活を行い、果実食であった。ヒトは肉を生食できないが、果実は生食することができる。ところで、興味深いことに妖精の栄養価は一般的な果実のそれと多くが共通している。",
            listOf(), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val LEAVES_PRODUCTION = !TraitCard(
            "leaves_production", "Leaves Production", "葉面生成",
            "Between 1.2 and 0.8 billion years ago, green plants emerged and developed the ability to fix carbon, while simultaneously losing their symbiotic relationship with etherobacteria. The fact that most modern plants lack intelligence is said to be a remnant of this evolutionary change.",
            "12-8億年前、緑色植物が出現し、炭素固定能力を発達させた一方で、エテロバクテリアとの細胞内共生を退化させた。現生植物の多くに知性が欠落している事実は、この進化の名残りと言われている。",
            listOf(), listOf(TraitEffectKeyCard.LEAVES_PRODUCTION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val RARE_PRODUCTION = !TraitCard(
            "rare_production", "Rare Production", "希少品生成",
            "This trait does not actually exist as a single gene, but is instead a collection of various genetic characteristics responsible for the formation of certain parts, such as the crystallization of phytoliths in the fruit, which humans deem rare or valuable.",
            "この特性は、実際には遺伝子上には存在せず、果実部におけるプラントオパールの結晶化など、人間が希少と判断するいくつかの部位を形成する遺伝的形質をまとめたものである。",
            listOf(), listOf(TraitEffectKeyCard.RARE_PRODUCTION to 0.1 * 0.03),
        ) {
            register("1000", TraitSpawnRarity.RARE)
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val EXPERIENCE_PRODUCTION = !TraitCard(
            "experience_production", "Xp Production", "経験値生成",
            "The question of whether plants experience consciousness has long been a subject of debate. The discovery of plants that generate experience orbs has brought this discussion to a new stage.",
            "植物は意識体験をするか？という問いかけは、長年の議論の対象であった。経験値オーブを生成する植物の発見は、この議論を新たな局面へと導いた。",
            listOf(), listOf(TraitEffectKeyCard.EXPERIENCE_PRODUCTION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE)
            //register("0100", )
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.S_RARE)
        }

        val FAIRY_BLESSING = !TraitCard(
            "fairy_blessing", "Fairy Blessing", "妖精の祝福",
            "A powerful light illuminates the world. A gentle breeze sways the trees. Mirage petals brush against the cheeks. Swirling pollen comes together, transforming into the body of a fairy. The fairy knows. The fate of everything in this world.",
            "力強い光が世界を照らす。朗らかな風が木々を揺らす。ミラージュの花びらが頬を撫でる。渦巻く花粉は一つになり、妖精へと姿を変える。妖精は知っている。この世のすべての運命を。",
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {
            //register("1000", )
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }

        val FOUR_LEAFED = !TraitCard(
            "four_leafed", "Four-leafed", "四つ葉",
            "The probability of a certain type of plant having a set of four leaves is typically less than 1 in 10,000, reflecting the high luck of the discoverer. Therefore, by adjusting random numbers so that all leaves are in sets of four, it is theoretically possible to maximize the luck of all humanity at all times.",
            "ある種の植物の葉が4枚組となる確率は通常1万分の1以下であり、これは発見者の運気の高さを反映している。したがって、すべての葉が4枚組となるように乱数調整を行うことで、理論上全人類の運気を常に最大化できる。",
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {
            register("1000", TraitSpawnRarity.NORMAL) { +BiomeKeys.WARPED_FOREST }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FLORAL }
            register("0010", TraitSpawnRarity.S_RARE)
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.PLAINS }
        }
        val NODED_STEM = !TraitCard(
            "noded_stem", "Noded Stem", "節状の茎",
            "The stems of plants in the order Miragales are known for having nodes, similar to bamboo or wheat. These nodes are deactivated growth points, and in principle, rapid growth is possible by activating growth at all nodes simultaneously.",
            "妖花目の植物の茎は、竹や麦などと同様に節目を持つことで知られる。これらの節目は失活した成長点であり、原理的にはすべての節目で成長を行うことにより高速な生育が可能である。",
            listOf(), listOf(TraitEffectKeyCard.GROWTH_BOOST to 0.1),
        ) {
            //r("1000", )
            register("0100", TraitSpawnRarity.COMMON) { +BiomeKeys.BAMBOO_JUNGLE }
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.BEACH }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.JUNGLE }
        }
        val FRUIT_OF_KNOWLEDGE = !TraitCard(
            "fruit_of_knowledge", "Forbidden Fruit", "禁断の果実",
            "Consciousness, memory, soul, and genes--all this information is produced by the vortex motion of the gaseous etheric body. It is prophesied that the astral vortex, bound by the forbidden fruit, will connect to the Akashic demon, who possesses all knowledge beyond time and space.",
            "意識、記憶、魂、遺伝子、これらすべての情報は、気相エーテル体の渦状運動によってもたらされる。禁断の果実に束縛されたアストラル渦は、時空を超えたありとあらゆる知識を持つというアカーシャの悪魔につながることが予言されている。",
            listOf(), listOf(TraitEffectKeyCard.EXPERIENCE_PRODUCTION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.HIGH }
            register("0100", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.JUNGLE }
            register("0010", TraitSpawnRarity.RARE) { +BiomeKeys.WARPED_FOREST }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FOREST }
        }
        val GOLDEN_APPLE = !TraitCard(
            "golden_apple", "Golden Apple", "金のリンゴ",
            "When a sheep is dyed with lapis lazuli powder and then sheared, it will subsequently grow blue wool. This phenomenon is known as exogenous pigment induction.",
            "ヒツジをラピスラズリの粉末で着色して毛を刈り取ると、そのヒツジは以後、青色の毛を生やすようになる。このような現象を外因性色素誘導と呼ぶ。",
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.FOREST }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FOREST }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.JUNGLE }
            register("0001", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.FOREST }
        }
        val SPINY_LEAVES = !TraitCard(
            "spiny_leaves", "Spiny Leaves", "棘のある葉",
            "Spines prevent the loss of water through transpiration and are also used for defense against herbivorous animals. The spines of plants in the Miragaceae family are made of sharp tissues containing silicates, which can easily pierce human skin and cause mild inflammation.",
            "棘は水分の蒸散を防ぎ、草食動物からの防御にも使われる。ミラージュ科の植物が持つ棘はケイ酸塩を含む鋭利な組織でできており、人間の皮膚を容易に傷つけ、軽度の炎症を引き起こす。",
            listOf(TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.1), // TODO 接触ダメージ
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.MESA }
            register("0010", TraitSpawnRarity.NORMAL) { nether }
            register("0001", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.DESERT }
        }
        val DESERT_GEM = !TraitCard(
            "desert_gem", "Desert Gem", "砂漠の宝石",
            "Legends of plants that bear beautiful gemstones can be found in various desert regions and are often revered. Physiologically, these gemstones are formed as a means for the plants to expel excess metal ions, absorbed due to the low moisture content in the soil.",
            "美しい宝石を実らせる植物の伝承は各地の砂漠地帯において見られ、信仰の対象とされる。生理学的には、土中の水分含有量が少ないために過剰に吸収した金属イオンを排出するために形成される。",
            listOf(TraitConditionCard.HIGH_TEMPERATURE, TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.RARE_PRODUCTION to 0.1 * 0.03),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.DESERT }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MESA }
            register("0001", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.DESERT }
        }
        val HEATING_MECHANISM = !TraitCard(
            "heating_mechanism", "Heating Mechanism", "発熱機構",
            "Bacteria symbiotic within the cells generate heat through their metabolism. This heat helps prevent growth from stalling in cold environments.",
            "細胞内に共生するバクテリアが、代謝によって熱を発生させる。この熱によって低温環境で成長が停滞することを防ぐ。",
            listOf(TraitConditionCard.LOW_TEMPERATURE), listOf(TraitEffectKeyCard.TEMPERATURE to 0.05, TraitEffectKeyCard.GROWTH_BOOST to 0.05),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +TemperatureCategory.LOW }
            register("0100", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.SNOWY }
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.ICY }
            register("0001", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.TAIGA }
        }
        val WATERLOGGING_TOLERANCE = !TraitCard(
            "waterlogging_tolerance", "Waterlogging Tolerance", "浸水耐性",
            "By developing hollow stems that actively transport oxygen, this plant prevents its roots from becoming oxygen-deprived even in waterlogged conditions. As a result, it can thrive in environments where other plants would suffer from root rot.",
            "中空の茎を発達させ、能動的に酸素を送り込むことで、浸水状態にあっても根が酸欠状態になるのを防ぐ。これにより、他の植物が根腐れを起こすような環境でも生育できる。",
            listOf(TraitConditionCard.HIGH_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.05, TraitEffectKeyCard.GROWTH_BOOST to 0.05),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.HIGH }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.RIVER }
            register("0010", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.SWAMP }
            register("0001", TraitSpawnRarity.NORMAL) { +BiomeKeys.LUSH_CAVES }
        }
        val ADVERSITY_FLOWER = !TraitCard(
            "adversity_flower", "Adversity Flower", "高嶺の花",
            "A flower that blooms in adversity is rarer and more beautiful than any other. Even if it shines only for a fleeting moment, it becomes an unattainable object of desire, forever etched in people's hearts.",
            "逆境に咲く花は、どんな花よりも希少で美しい。たとえそれが一瞬の輝きであったとしても、手に入れることのできない、あこがれの存在として人々の胸に刻まれる。",
            listOf(TraitConditionCard.HIGH_ALTITUDE), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION to 0.05, TraitEffectKeyCard.RARE_PRODUCTION to 0.05 * 0.03),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0100", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.EXTREME_HILLS }
        }
        val FLESHY_LEAVES = !TraitCard(
            "fleshy_leaves", "Fleshy Leaves", "肉厚の葉",
            "As a result of evolving to minimize surface area exposed to the air in order to prevent water loss, the leaves have become thick and are now capable of storing water internally.",
            "水分の蒸発を防ぐために空気との接触面積を最小化するように進化した結果、葉は肉厚になり、内部に水分を蓄えるようになった。",
            listOf(TraitConditionCard.LOW_HUMIDITY), listOf(TraitEffectKeyCard.HUMIDITY to 0.05, TraitEffectKeyCard.LEAVES_PRODUCTION to 0.05),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.LOW }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.SAVANNA }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.DESERT }
            register("0001", TraitSpawnRarity.RARE) { nether }
        }
        val NATURAL_ABSCISSION = !TraitCard(
            "natural_abscission", "Natural Abscission", "自然落果",
            "A part of the plant's body falls off and drops to the ground. The wind, animals, and sometimes even humans carry it far away. For the plant, the automation of harvesting is merely a means of reproduction.",
            "植物の体の一部が欠落し、地面に落ちる。それを風や動物、ときには人間が遠くへ運ぶ。収穫の自動化は、植物にとっては繁殖の手段にすぎないのだ。",
            listOf(), listOf(TraitEffectKeyCard.NATURAL_ABSCISSION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE)
            register("0100", TraitSpawnRarity.NORMAL) { +BiomeKeys.ICE_SPIKES }
            register("0010", TraitSpawnRarity.NORMAL) { +BiomeKeys.BASALT_DELTAS }
            register("0001", TraitSpawnRarity.NORMAL) { +BiomeKeys.DRIPSTONE_CAVES }
        }
        val CARNIVOROUS_PLANT = !TraitCard(
            "carnivorous_plant", "Carnivorous Plant", "食虫植物",
            "It captures small animals like insects and breaks them down with digestive fluids. This is one of the strategies for surviving in nutrient-poor soil.",
            "昆虫などの小動物を捕らえ、消化液により分解する。栄養の少ない土壌で生活するための戦略の一つである。",
            listOf(TraitConditionCard.OUTDOOR), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { nether }
            register("0100", TraitSpawnRarity.NORMAL) { +ConventionalBiomeTags.SWAMP }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.MOUNTAIN }
            register("0001", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.JUNGLE }
        }
        val ETHER_PREDATION = !TraitCard(
            "ether_predation", "Ether Predation", "エーテル捕食",
            "All living beings possess an etheric soul. If that is the case, where do the souls of fairies come from? In a sense, the Mirage is a carnivorous plant.",
            "生きとし生ける者はみな、エーテルの魂を持つ。なれば、妖精の魂はどこから来るのか？ミラージュはある意味肉食植物だ。",
            listOf(), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION to 0.05, TraitEffectKeyCard.NUTRITION to 0.05),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { end }
            register("0100", TraitSpawnRarity.NORMAL) { end }
            register("0010", TraitSpawnRarity.COMMON)
            register("0001", TraitSpawnRarity.RARE)
        }
        val PAVEMENT_FLOWERS = !TraitCard(
            "pavement_flowers", "Pavement Flowers", "アスファルトに咲く花",
            "The sight of a small flower piercing through hard ground and transforming bitumen into nutrients is known as a symbol of the powerful vitality possessed by plants.",
            "小さな花が硬い地面を穿ち瀝青を栄養に変えるその姿は、植物の持つ力強い生命力の象徴として知られる。",
            listOf(TraitConditionCard.FLOOR_HARDNESS), listOf(TraitEffectKeyCard.NUTRITION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { nether }
            register("0100", TraitSpawnRarity.COMMON) { nether }
            register("0010", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.CAVES }
            register("0001", TraitSpawnRarity.RARE) { +BiomeKeys.BASALT_DELTAS }
        }
        val PROSPERITY_OF_SPECIES = !TraitCard(
            "prosperity_of_species", "Prosperity of Species", "種の繁栄",
            "Those who bear many offspring, those who are flexible to change, those who are greedy, those who eliminate their enemies, and those who, in the end, leave behind many descendants are the ones who win the struggle for survival.",
            "多くの子を産む者、変化に柔軟な者、欲張りな者、敵を蹴落とす者、結果的に多くの子孫を遺せた者が生存競争を勝ち抜くのだ。",
            listOf(), listOf(TraitEffectKeyCard.SEEDS_PRODUCTION to 0.1),
        ) {
            register("1000", TraitSpawnRarity.S_RARE) { +HumidityCategory.MEDIUM }
            register("0100", TraitSpawnRarity.COMMON) { +ConventionalBiomeTags.PLAINS }
            register("0010", TraitSpawnRarity.RARE) { +ConventionalBiomeTags.FOREST }
            register("0001", TraitSpawnRarity.S_RARE) { +TemperatureCategory.MEDIUM }
        }
        val PHANTOM_FLOWER = !TraitCard(
            "phantom_flower", "Phantom Flower", "幻の花",
            "A phantom flower that blooms in the distant land of fairies. Is its true nature that of an illusory flower? Or is it a flower that shows illusions?",
            "遥か妖精の地に咲く幻の花。その正体は幻のような花か？幻を見せる花か？",
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.1),
        ) {
            //register("1000", )
            //register("0100", )
            register("0010", TraitSpawnRarity.COMMON) { +FAIRY_BIOME_TAG }
            register("0001", TraitSpawnRarity.RARE) { +FAIRY_BIOME_TAG }
        }
        val ETERNAL_TREASURE = !TraitCard(
            "eternal_treasure", "Eternal Treasure", "悠久の秘宝",
            "A mysterious fruit shown by nature. Many greedy humans attempted to cultivate it, but not a single one succeeded, or so the story goes.",
            "自然が見せる神秘の果実。多くの欲深き人間がその栽培化を試みたものの、成功した者は誰一人として居なかったという。",
            listOf(TraitConditionCard.NATURAL), listOf(TraitEffectKeyCard.RARE_PRODUCTION to 0.1 * 0.03),
        ) {
            register("1000", TraitSpawnRarity.COMMON)
            //register("0100", )
            register("0010", TraitSpawnRarity.COMMON)
            //register("0001", )
        }
        val TREASURE_OF_XARPA = !TraitCard(
            "treasure_of_xarpa", "Treasure of Xarpa", "シャルパの秘宝",
            "Since ancient times, people have revered what lies beyond their understanding, calling it mysterious or sacred. The formation of crystals is a natural ability in plants of the Miragales order, and it is easy to destroy the genes that prevent this. Humanity has transcended the mysterious.",
            "人々は昔から、理解を超えた対象を神秘と呼び、神聖視してきた。結晶の生成は妖花目の植物における生来の能力であり、これを妨げる遺伝子を破壊することは容易だ。人類は神秘を超越したのだ。",
            listOf(), listOf(TraitEffectKeyCard.RARE_PRODUCTION to 0.03 * 0.03),
        ) {
            //register("1000", ) // TODO シャルパの遺伝子改良
            register("0100", TraitSpawnRarity.S_RARE)
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.NORMAL)
        }
        val CROSSBREEDING = !TraitCard(
            "crossbreeding", "Crossbreeding", "交雑",
            "By crossbreeding with different species of plants within the same family, it is possible to introduce genetic traits that the original plant species does not naturally possess.",
            "同じ科の異種の植物との交配により、その植物種には本来備わらない遺伝的形質を持たせることができる。",
            listOf(), listOf(TraitEffectKeyCard.CROSSBREEDING to 0.1),
        ) {
            //register("1000", )
            //register("0100", )
            register("0010", TraitSpawnRarity.RARE)
            register("0001", TraitSpawnRarity.RARE)
        }
        val PLANTS_WITH_SELF_AWARENESS = !TraitCard(
            "plants_with_self_awareness", "Plants with Self-Awareness", "自我を持つ植物",
            "On a colonized planet lacking native flying animals, giving mobility to airborne reproductive cells is beneficial in terms of increasing the probability of encounters between individual organisms.",
            "固有の飛行性動物を欠いた開拓型惑星において、空中散布式の生殖細胞に運動能力を与えることは、個体同士の遭遇確率の点で有益である。",
            listOf(), listOf(TraitEffectKeyCard.FRUITS_PRODUCTION to 0.05, TraitEffectKeyCard.EXPERIENCE_PRODUCTION to 0.05),
        ) {
            //register("1000", TraitSpawnRarity.COMMON)
            register("0100", TraitSpawnRarity.S_RARE) { +FAIRY_BIOME_TAG }
            register("0010", TraitSpawnRarity.RARE) { +BiomeCards.FAIRY_FOREST.registryKey }
            register("0001", TraitSpawnRarity.COMMON)
        }
        val FLOWER_OF_THE_END = !TraitCard(
            "flower_of_the_end", "Flower of the End", "終焉の花",
            "When the vacuum decayed and the concept of the world met its end, that flower never lost its smile until the very last moment.",
            "真空が崩壊し、世界という概念が消滅を迎えるとき、その花は最後の瞬間まで笑顔を絶やさなかった。",
            listOf(), listOf(TraitEffectKeyCard.FORTUNE_FACTOR to 0.05, TraitEffectKeyCard.EXPERIENCE_PRODUCTION to 0.05),
        ) {
            register("1000", TraitSpawnRarity.ALWAYS)
            //register("0100", )
            //register("0010", )
            //register("0001", )
        }
    }

    init {
        check(traitEffectKeyCardStacks.isNotEmpty())
    }

    val identifier = MirageFairy2024.identifier(path)
    val poemTranslation = Translation({ identifier.toTranslationKey("${MirageFairy2024.modId}.trait", "poem") }, enPoem, jaPoem)
    val trait: Trait = object : Trait(traitEffectKeyCardStacks.first().first.traitEffectKey.style, text { poemTranslation() }) {
        override val spawnSpecs = mutableListOf<TraitSpawnSpec>().also { spawnSpecConfigurator(it) }

        override val conditions = traitConditionCards.map { it.traitCondition }
        override val primaryEffect = traitEffectKeyCardStacks.first().first.traitEffectKey
        override val effectStacks = traitEffectKeyCardStacks.map { Pair(it.first.traitEffectKey, it.second) }

        override fun getTraitEffects(world: World, blockPos: BlockPos, level: Int): MutableTraitEffects? {
            val factor = traitConditionCards.map { it.traitCondition.getFactor(world, blockPos) }.fold(1.0) { a, b -> a * b }
            return if (factor != 0.0) {
                val traitEffects = MutableTraitEffects()
                traitEffectKeyCardStacks.forEach {
                    traitEffects[it.first.traitEffectKey] = it.first.traitEffectKey.getValue(it.second * level) * factor
                }
                traitEffects
            } else {
                null
            }
        }
    }

}

private fun MutableList<TraitSpawnSpec>.register(binary: String, rarity: TraitSpawnRarity, conditionGetter: context(TraitSpawnConditionScope)() -> TraitSpawnCondition = { anywhere }) {
    this += TraitSpawnSpec(conditionGetter(TraitSpawnConditionScope), rarity, binary.toInt(2))
}


context(ModContext)
fun initTraitCard() {
    TraitCard.entries.forEach { card ->
        card.poemTranslation.enJa()
        card.trait.register(traitRegistry, card.identifier)
        card.trait.enJa(card.enName, card.jaName)
    }
}
