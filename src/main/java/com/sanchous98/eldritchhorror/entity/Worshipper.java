package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultDefinition;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.cult.CultService;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.cult.Cults;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Worshipper — the cult faction NPC ({@code design/25-bestiary-and-entities.md}: "faction NPC that
 * trades, teaches rites, guards altars"). It is deliberately <b>not</b> a monster: base is
 * {@link PathfinderMob}, which has no hostile shape, no {@code Enemy} marker and no attack goal, so
 * it does <b>not</b> attack on sight. Retaliation is intentionally absent too — a worshipper defends
 * its home by presence, not by swinging.
 *
 * <p>It carries a {@link CultIdentity} (owning cult + rank band), guards the spot it spawned on
 * (a vanilla home restriction, {@link MoveTowardsRestrictionGoal}), and reacts to the visiting
 * player's standing with its own cult. Interaction (<b>what is implemented</b>): right-click opens
 * a chat report naming the cult, the player's reputation and rank, and the cult's rank-gated
 * services with their requirement. There is <b>no trade GUI</b> in this batch; see
 * {@link #servicesForDisplay} for the explicit hook that a later batch replaces with a menu.
 *
 * <p>What is <b>deferred</b>: the actual trade inventory, offering/consumption, rite teaching on
 * the spot, altar guards as a site population, and cult-specific texture/skins. This batch ships
 * the identity, the guard behaviour, the reputation reaction and the read-only interaction only.
 *
 * <p>Model: placeholder villager model ({@code VillagerModel} + {@code VillagerRenderState}), the
 * verified 26.3 shape for a passive humanoid.
 */
public final class Worshipper extends PathfinderMob {

    private final CultIdentity identity = new CultIdentity(CultistSupport.defaultCult(), CultRank.NEUTRAL);

    public Worshipper(EntityType<? extends Worshipper> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.6));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // No targetSelectors: a worshipper never attacks, even when struck.
    }

    /** Base attributes: 20 HP, no attack, slow stroll. A person, not a soldier. */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        // Eggs and commands draw a real identity too, so a spawned worshipper is never factionless.
        CultIdentity rolled = CultIdentity.random(this.getRandom());
        this.identity.set(rolled.cultId(), rolled.rank());
        this.setPersistenceRequired();
        this.setHomeTo(this.blockPosition(), ModConfig.WORSHIPPER_HOME_RADIUS.get());
        return data;
    }

    /** Binds this worshipper to a specific cult (used when a Fallen city is taken over). */
    public void setCult(String cultId, CultRank rank) {
        this.identity.set(cultId, rank);
        this.setPersistenceRequired();
        this.setHomeTo(this.blockPosition(), ModConfig.WORSHIPPER_HOME_RADIUS.get());
    }

    /** @return the id of the cult this worshipper belongs to. */
    public String cultId() {
        return this.identity.cultId();
    }


    /**
     * The explicit service hook: the services this cult offers to the interacting player, with the
     * rank each needs. A later batch replaces {@link #mobInteract} with a trade/teach menu over this
     * same list; today it is shown read-only.
     */
    public java.util.List<CultService> servicesForDisplay() {
        CultDefinition def = Cults.byId(this.identity.cultId());
        return def == null ? java.util.List.of() : def.services();
    }

    /**
     * Right-click report. Server-authoritative; the client just predicts a hand swing. Reports the
     * cult, the player's reputation/rank, and each service with its unlock state. Does not mutate
     * reputation or perform a service (that stays with {@code /eh service} in this batch).
     */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        String cultId = this.identity.cultId();
        CultDefinition def = Cults.byId(cultId);
        int rep = CultSystem.get(serverPlayer, cultId);
        CultRank playerRank = CultSystem.rankOf(serverPlayer, cultId);
        serverPlayer.sendSystemMessage(Component.translatable("worshipper.eldritch_horror.greeting",
                def == null ? Component.literal(cultId) : Component.translatable("cult.eldritch_horror." + cultId)));
        serverPlayer.sendSystemMessage(Component.translatable("worshipper.eldritch_horror.reputation",
                rep, playerRank.display()));
        serverPlayer.sendSystemMessage(Component.translatable("worshipper.eldritch_horror.rank",
                this.identity.rank().display()));
        for (CultService service : this.servicesForDisplay()) {
            boolean unlocked = playerRank.atLeast(service.minRank());
            serverPlayer.sendSystemMessage(Component.translatable(
                    unlocked ? "worshipper.eldritch_horror.service.unlocked"
                             : "worshipper.eldritch_horror.service.locked",
                    service.name(), service.minRank().display()));
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * The reputation reaction, applied server-side each tick: a worshipper is neutral/friendly above
     * the threshold and <b>hostile</b> below it. "Hostile" here is expressed as fleeing the visitor
     * (the passive-mob equivalent of enmity) rather than attacking — the design keeps the NPC
     * non-aggressive. A later batch can swap this for a dialogue branch.
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        ServerPlayer nearest = this.level().getNearestPlayer(this, 12.0) instanceof ServerPlayer sp ? sp : null;
        boolean hostile = nearest != null
                && CultistSupport.hostileTo(nearest, this.identity.cultId(), ModConfig.CULT_HOSTILE_BELOW.get());
        if (hostile && nearest != null) {
            double dx = this.getX() - nearest.getX();
            double dz = this.getZ() - nearest.getZ();
            double d = Math.max(Math.sqrt(dx * dx + dz * dz), 0.01);
            this.getNavigation().moveTo(this.getX() + dx / d * 4.0, this.getY(), this.getZ() + dz / d * 4.0, 1.0);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.identity.save(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.identity.load(input);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.WORSHIPPER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.WORSHIPPER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.WORSHIPPER_AMBIENT.get();
    }
}
