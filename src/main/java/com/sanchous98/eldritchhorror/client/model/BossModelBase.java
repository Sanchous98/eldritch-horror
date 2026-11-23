package com.sanchous98.eldritchhorror.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Tiny animation kit shared by every Ancient One model.
 *
 * <p>Each model registers the parts it wants animated once in its constructor:
 * <ul>
 *   <li>{@link #sway} – additive idle sine sway on any part (tentacles, robes, cloaks),</li>
 *   <li>{@link #leg}/{@link #arm} – walk cycle driven by the vanilla walk animation fields,</li>
 *   <li>{@link #flap} – wing flap (speeds up while moving),</li>
 *   <li>{@link #pulse} – uniform scale pulse for breathing blobs and flames,</li>
 *   <li>{@link #breatheWith} – subtle scale breathing on a body part.</li>
 * </ul>
 *
 * <p>{@link #setupAnim} resets the pose (as all 26.3 models must), applies the generic layers, then
 * gives subclasses two hooks: {@link #applyIdle} for boss-specific motion and
 * {@link #applyLunge} for the attack animation. The lunge uses the swing progress captured in
 * {@link BossRenderState}, so no external animation library and no server changes are needed.
 */
@OnlyIn(Dist.CLIENT)
public abstract class BossModelBase extends EntityModel<BossRenderState> {

    private static final float WALK_FREQUENCY = 0.6662F;

    private final List<Swayer> swayers = new ArrayList<>();
    private final List<Limb> legs = new ArrayList<>();
    private final List<Limb> arms = new ArrayList<>();
    private final List<Flapper> flappers = new ArrayList<>();
    private final List<Pulser> pulsers = new ArrayList<>();
    private ModelPart breather;
    private float breatheAmount = 0.03F;

    protected BossModelBase(ModelPart root) {
        super(root);
    }

    /** A part that idles with a sine wave. */
    protected record Swayer(ModelPart part, float phase, float xAmp, float yAmp, float zAmp) {
    }

    /** A two-state walk limb. */
    protected record Limb(ModelPart part, float phase, float amplitude) {
    }

    /** A wing-like part that flaps around Z. */
    protected record Flapper(ModelPart part, float phase, float amplitude, float speed) {
    }

    /** A part whose scale pulses in place. */
    protected record Pulser(ModelPart part, float phase, float amplitude, float speed) {
    }

    protected final void sway(ModelPart part, float phase, float xAmp, float yAmp, float zAmp) {
        this.swayers.add(new Swayer(part, phase, xAmp, yAmp, zAmp));
    }

    protected final void sway(ModelPart part) {
        this.sway(part, 0.0F, 0.06F, 0.10F, 0.04F);
    }

    protected final void leg(ModelPart part, float phase, float amplitude) {
        this.legs.add(new Limb(part, phase, amplitude));
    }

    protected final void arm(ModelPart part, float phase, float amplitude) {
        this.arms.add(new Limb(part, phase, amplitude));
    }

    protected final void flap(ModelPart part, float phase, float amplitude, float speed) {
        this.flappers.add(new Flapper(part, phase, amplitude, speed));
    }

    protected final void pulse(ModelPart part, float phase, float amplitude, float speed) {
        this.pulsers.add(new Pulser(part, phase, amplitude, speed));
    }

    protected final void breatheWith(ModelPart part, float amount) {
        this.breather = part;
        this.breatheAmount = amount;
    }

    @Override
    public void setupAnim(BossRenderState state) {
        super.setupAnim(state);

        float time = state.ageInTicks;
        float breath = Mth.sin(time * 0.08F);
        if (this.breather != null) {
            float scale = 1.0F + breath * this.breatheAmount;
            this.breather.xScale = scale;
            this.breather.yScale = 1.0F - breath * this.breatheAmount * 0.5F;
            this.breather.zScale = scale;
        }

        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        for (Limb limb : this.legs) {
            limb.part().xRot = Mth.cos(walkPos * WALK_FREQUENCY + limb.phase()) * 1.2F * walkSpeed * limb.amplitude();
        }
        for (Limb limb : this.arms) {
            limb.part().xRot = Mth.cos(walkPos * WALK_FREQUENCY + limb.phase()) * 0.8F * walkSpeed * limb.amplitude();
        }
        for (Flapper flapper : this.flappers) {
            float activity = 0.35F + 0.65F * Math.min(1.0F, Math.abs(walkSpeed) * 2.0F);
            flapper.part().zRot += Mth.sin(time * flapper.speed() + flapper.phase()) * flapper.amplitude() * activity;
        }
        for (Pulser pulser : this.pulsers) {
            float scale = 1.0F + Mth.sin(time * pulser.speed() + pulser.phase()) * pulser.amplitude();
            pulser.part().xScale = scale;
            pulser.part().yScale = scale;
            pulser.part().zScale = scale;
        }
        for (Swayer swayer : this.swayers) {
            swayer.part().xRot += Mth.sin(time * 0.05F + swayer.phase()) * swayer.xAmp();
            swayer.part().yRot += Math.sin(time * 0.04F + swayer.phase() * 1.7F) * swayer.yAmp();
            swayer.part().zRot += Mth.cos(time * 0.045F + swayer.phase()) * swayer.zAmp();
        }

        this.applyLunge(state);
        this.applyIdle(state, time, breath);
    }

    /**
     * Attack lunge. The default thrusts the whole silhouette forward along the facing axis and tips
     * it forward a little; models with a head or weapon override this to lean harder.
     */
    protected void applyLunge(BossRenderState state) {
        float lunge = state.lunge;
        this.root.z = -2.5F * lunge;
        this.root.y = 0.8F * lunge;
    }

    /** Boss-specific idle motion (head bob, neck sway, flame flicker...). */
    protected void applyIdle(BossRenderState state, float time, float breath) {
    }

    /** Adds an additive head/neck tilt clamped to a sane range. */
    protected static void tiltHead(ModelPart part, float xRot, float yRot) {
        part.xRot += Mth.clamp(xRot, -1.4F, 1.4F);
        part.yRot += Mth.clamp(yRot, -1.2F, 1.2F);
    }
}
