package miragefairy2024.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.ParticleTypeCard
import miragefairy2024.mod.SoundEventCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
import miragefairy2024.util.configure
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerEntityTypeTagGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSpawn
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.ai.goal.ActiveTargetGoal
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal
import net.minecraft.entity.ai.goal.Goal
import net.minecraft.entity.ai.goal.RevengeGoal
import net.minecraft.entity.ai.goal.WanderAroundFarGoal
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.SmallFireballEntity
import net.minecraft.item.Item
import net.minecraft.item.SpawnEggItem
import net.minecraft.loot.condition.KilledByPlayerLootCondition
import net.minecraft.loot.condition.RandomChanceWithLootingLootCondition
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.EntityTypeTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import net.minecraft.world.biome.BiomeKeys
import org.joml.Quaternionf
import java.util.EnumSet
import kotlin.math.sqrt

object ChaosCubeCard {
    val spawnGroup = SpawnGroup.MONSTER
    val width = 0.8F
    val height = 1.5F
    fun createEntity(entityType: EntityType<ChaosCubeEntity>, world: World) = ChaosCubeEntity(entityType, world)
    val identifier = MirageFairy2024.identifier("chaos_cube")
    val name = EnJa("Chaos Cube", "混沌のキューブ")
    val entityType: EntityType<ChaosCubeEntity> = FabricEntityTypeBuilder.create(spawnGroup) { entityType, world -> createEntity(entityType, world) }
        .dimensions(EntityDimensions.fixed(width, height))
        .build()
    val spawnEggItem = SpawnEggItem(entityType, 0xB36235, 0xFFC21D, Item.Settings())

    context(ModContext)
    fun init() {
        entityType.register(Registries.ENTITY_TYPE, identifier)
        val attributes = HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.4)
            .add(EntityAttributes.GENERIC_ARMOR, 8.0)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 20.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 4.0)
        FabricDefaultAttributeRegistry.register(entityType, attributes)
        entityType.enJa(name)
        entityType.registerEntityTypeTagGeneration { EntityTypeTags.FALL_DAMAGE_IMMUNE }
        entityType.registerLootTableGeneration {
            LootTable(
                LootPool(ItemLootPoolEntry(MaterialCard.CHAOS_STONE.item)).configure {
                    conditionally(KilledByPlayerLootCondition.builder())
                    conditionally(RandomChanceWithLootingLootCondition.builder(0.3F, 0.1F))
                },
            )
        }

        entityType.registerSpawn(SpawnGroup.MONSTER, 2, 2, 4) { +BiomeKeys.DRIPSTONE_CAVES }

        spawnEggItem.register(Registries.ITEM, identifier * "_egg")
        spawnEggItem.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        spawnEggItem.registerModelGeneration(Model(Identifier("minecraft", "item/template_spawn_egg")))
        spawnEggItem.enJa(EnJa("${name.en} Spawn Egg", "${name.ja}のスポーンエッグ"))
    }
}

class ChaosCubeEntity(entityType: EntityType<out ChaosCubeEntity>, world: World) : HostileEntity(entityType, world) {

    class Segment(val partIndex: Int) {
        val prevRotation = Quaternionf()
        val rotation = Quaternionf()
    }

    private var isFirstClientTick = true
    val segments = (0 until 8).map { Segment(it) }.toTypedArray()

    private val animator = sequence {

        while (true) {
            fun swap(a: Int, b: Int, c: Int, d: Int) {
                val tmp1 = segments[a]
                segments[a] = segments[b]
                segments[b] = segments[c]
                segments[c] = segments[d]
                segments[d] = tmp1
            }

            val duration = 40

            // ++++ ----    vv   vv       <==>
            // 0123 4567  0132 4576  0132 6754
            // 0145 2367  0154 2376  0154 6732
            // 0246 1357  0264 1375  0264 5731

            repeat(duration) {
                segments[0].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 1, 3, 2)
            swap(6, 7, 5, 4)

            repeat(duration) {
                segments[0].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 1, 5, 4)
            swap(6, 7, 3, 2)

            repeat(duration) {
                segments[0].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 2, 6, 4)
            swap(5, 7, 3, 1)

        }

    }.iterator()


    init {
        experiencePoints = 20
    }

    override fun initGoals() {
        goalSelector.add(4, ShootGoal(this))
        goalSelector.add(5, GoToWalkTargetGoal(this, 1.0))
        goalSelector.add(7, WanderAroundFarGoal(this, 0.5, 0.0F))
        targetSelector.add(1, RevengeGoal(this).setGroupRevenge())
        targetSelector.add(2, ActiveTargetGoal(this, PlayerEntity::class.java, true))
    }


    override fun getAmbientSound() = SoundEventCard.ENTITY_CHAOS_CUBE_AMBIENT.soundEvent
    override fun getHurtSound(source: DamageSource) = SoundEventCard.ENTITY_CHAOS_CUBE_HURT.soundEvent
    override fun getDeathSound() = SoundEventCard.ENTITY_CHAOS_CUBE_DEATH.soundEvent

    override fun tickMovement() {
        super.tickMovement()

        if (!isOnGround && velocity.y < 0.0) velocity = velocity.multiply(1.0, 0.6, 1.0)

        if (world.isClient) {

            if (isFirstClientTick) {
                isFirstClientTick = false
                animator.next()
                segments.forEach {
                    it.prevRotation.x = it.rotation.x
                    it.prevRotation.y = it.rotation.y
                    it.prevRotation.z = it.rotation.z
                    it.prevRotation.w = it.rotation.w
                }
            } else {
                segments.forEach {
                    it.prevRotation.x = it.rotation.x
                    it.prevRotation.y = it.rotation.y
                    it.prevRotation.z = it.rotation.z
                    it.prevRotation.w = it.rotation.w
                }
                animator.next()
            }

            if (world.random.nextInt(10) == 0) {
                world.addParticle(
                    ParticleTypeCard.CHAOS_STONE.particleType,
                    x + width / 2.0 * (2.0 * random.nextDouble() - 1.0) * 0.8,
                    y + 1.0,
                    z + width / 2.0 * (2.0 * random.nextDouble() - 1.0) * 0.8,
                    0.0,
                    -0.05,
                    0.0,
                )
            }

        }

    }

    override fun mobTick() {

        val livingEntity = target
        if (livingEntity != null && livingEntity.eyeY > eyeY && canTarget(livingEntity)) {
            val vec3d = velocity
            velocity = velocity.add(0.0, (0.3 - vec3d.y) * 0.3, 0.0)
            velocityDirty = true
        }

        super.mobTick()
    }

    class ShootGoal(private val entity: ChaosCubeEntity) : Goal() {
        private var count = 0
        private var cooldown = 0
        private var targetNotVisibleTicks = 0

        init {
            controls = EnumSet.of(Control.MOVE, Control.LOOK)
        }

        override fun canStart(): Boolean {
            val livingEntity = entity.target
            return livingEntity != null && livingEntity.isAlive && entity.canTarget(livingEntity)
        }

        override fun start() {
            count = 0
        }

        override fun stop() {
            targetNotVisibleTicks = 0
        }

        override fun shouldRunEveryTick() = true

        override fun tick() {

            if (cooldown > 0) cooldown--

            val target = entity.target ?: return

            // 見えるかどうか判定、見えている時間の更新
            val canSee = entity.visibilityCache.canSee(target)
            if (canSee) {
                targetNotVisibleTicks = 0
            } else {
                targetNotVisibleTicks++
            }

            val squaredDistance = entity.squaredDistanceTo(target)
            if (squaredDistance < 4.0) { // 至近距離
                if (!canSee) return // 至近距離なのに遮蔽されているので抜ける

                // クールタイムが満了しているなら打撃
                if (cooldown <= 0) {
                    cooldown = 20
                    entity.tryAttack(target)
                }

                // ターゲットに近づく
                entity.getMoveControl().moveTo(target.x, target.y, target.z, 1.0)

            } else if (squaredDistance < getFollowRange() * getFollowRange() && canSee) { // 感知範囲内かつ見える

                // クールタイムが満了しているなら射撃を撃つ
                if (cooldown <= 0) {
                    count++
                    if (count == 1) {
                        cooldown = 60
                        //entity.setFireActive(true)
                    } else if (count <= 4) {
                        cooldown = 6
                    } else {
                        // リセット
                        cooldown = 100
                        count = 0
                        //entity.setFireActive(false)
                    }

                    if (count > 1) { // count 2 .. 5

                        // エフェクト
                        if (!entity.isSilent) entity.world.syncWorldEvent(null, WorldEvents.BLAZE_SHOOTS, entity.blockPos, 0)

                        // 発射体の生成
                        val sqrtDistance = sqrt(sqrt(squaredDistance)) * 0.5
                        val smallFireballEntity = SmallFireballEntity(
                            entity.world,
                            entity,
                            entity.getRandom().nextTriangular(target.x - entity.x, 2.297 * sqrtDistance),
                            target.getBodyY(0.5) - entity.getBodyY(0.5),
                            entity.getRandom().nextTriangular(target.z - entity.z, 2.297 * sqrtDistance),
                        )
                        smallFireballEntity.setPosition(
                            smallFireballEntity.x,
                            entity.getBodyY(0.5) + 0.5,
                            smallFireballEntity.z
                        )
                        entity.world.spawnEntity(smallFireballEntity)

                    }
                }

                // ターゲットを見る
                entity.getLookControl().lookAt(target, 10.0F, 10.0F)

            } else if (targetNotVisibleTicks < 5) { // 遮蔽された直後なら

                // ターゲットに近づく
                entity.getMoveControl().moveTo(target.x, target.y, target.z, 1.0)

            }

            super.tick()
        }

        private fun getFollowRange() = entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)
    }
}
