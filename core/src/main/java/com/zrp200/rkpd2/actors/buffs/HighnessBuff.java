package com.zrp200.rkpd2.actors.buffs;

import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.items.DuelistGrass;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;

import java.util.ArrayList;

public class HighnessBuff extends Buff implements ActionIndicator.Action, Wand.RechargeSource {

    {
        type = buffType.POSITIVE;
    }

    public enum State{
        NORMAL, ENERGIZED, RECOVERING
    }
    public State state = State.NORMAL;

    public int currentPower;
    public int consumedGrass;

    public float maxRecovery;
    public float currentRecovery;

    private static final String CUR_POWER = "current_power";
    private static final String STATE     = "state";
    private static final String GRASS     = "consumed_grass";
    private static final String CUR_REC   = "current_recovery";
    private static final String MAX_REC   = "max_recovery";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(CUR_POWER, currentPower);
        bundle.put(STATE, state);
        bundle.put(GRASS, consumedGrass);
        bundle.put(CUR_REC, currentRecovery);
        bundle.put(MAX_REC, maxRecovery);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        currentPower = bundle.getInt(CUR_POWER);
        state = bundle.getEnum(STATE, State.class);
        consumedGrass = bundle.getInt(GRASS);
        currentRecovery = bundle.getFloat(CUR_REC);
        maxRecovery = bundle.getFloat(MAX_REC);
    }

    public int availablePower(){
        if (Dungeon.hero == null){
            return 0;
        } else {
            DuelistGrass grass = Dungeon.hero.belongings.getItem(DuelistGrass.class);
            return grass != null ? grass.quantity() : 0;
        }
    }

    @Override
    public boolean usable() {
        return availablePower() >= 1 && state != State.RECOVERING;
    }

    @Override
    public boolean act() {
        if (state == State.NORMAL){
            if (availablePower() >= 1)
                ActionIndicator.setAction(this);
        } else if (state == State.ENERGIZED){
            if (--currentPower <= 0){
                boolean consumed = consumeGrass(1);
                if (!consumed){
                    startRecovering();
                    spend(TICK);
                    return true;
                }
            }
            ActionIndicator.setAction(this);
            ArtifactRecharge.chargeArtifacts((Hero)target, 1.5f);

            ArrayList<Char> targets = new ArrayList<>();
            targets.add(target);

            if (Dungeon.hero.pointsInTalent(Talent.PARTY_FEELING) > 1){
                for (Char ch: Dungeon.level.mobs){
                    if (ch.alignment == Char.Alignment.ALLY && Dungeon.level.heroFOV[ch.pos]){
                        targets.add(ch);
                    }
                }
            }

            for (Char target: targets){
                if (target.HP < target.HT && !((Hero)target).isStarving()) {
                    if (Regeneration.regenOn() && currentPower % 2 == 1) {
                        target.HP += 1;
                        if (target.HP == target.HT) {
                            ((Hero) target).resting = false;
                        }
                    }
                }
            }
        } else {
            if (--currentRecovery <= 0){
                state = State.NORMAL;
                currentRecovery = maxRecovery = 0;
                SpellSprite.show(target, SpellSprite.PURITY, 0.74f, 0.796f, 1f);
                Sample.INSTANCE.play(Assets.Sounds.BURNING, 1f, 2f);
                ActionIndicator.refresh();
                BuffIndicator.refreshHero();
            }
        }
        spend(TICK);
        return true;
    }

    public int grassValue(){
        return 4 + Dungeon.hero.pointsInTalent(Talent.PROLONGED_JOY)*2;
    }

    private static final float RAMPING_START_CONST = 18;

    public float recoveryAmount(int consumedGrass){
        float recTime = consumedGrass * grassValue() * 2;
        if (recTime >= RAMPING_START_CONST){
            recTime = (float) (RAMPING_START_CONST + (Math.pow(2 * (recTime - RAMPING_START_CONST) + 1, 2) - 1)/
                    (64.0f - Dungeon.hero.pointsInTalent(Talent.PROLONGED_JOY)*6));
        }
        return recTime;
    }

    public boolean consumeGrass(int amount){
        if (availablePower() < amount){
            return false;
        } else {
            DuelistGrass grass = Dungeon.hero.belongings.getItem(DuelistGrass.class);
            if (grass.quantity() > amount){
                grass.quantity(grass.quantity()-amount);
            } else {
                grass.detachAll(Dungeon.hero.belongings.backpack);
            }
            currentPower += grassValue() * amount;
            consumedGrass += amount;
            maxRecovery = 10 + recoveryAmount(consumedGrass);
            Dungeon.hero.sprite.centerEmitter().burst( LeafParticle.LEVEL_SPECIFIC, 15 * amount );
            Sample.INSTANCE.play(Assets.Sounds.PLANT);
            Item.updateQuickslot();
            return true;
        }
    }

    public void startRecovering(){
        currentRecovery = maxRecovery;
        consumedGrass = 0;
        currentPower = 0;
        state = State.RECOVERING;
        GameScene.flash(0x70bdcbff);
        Sample.INSTANCE.play(Assets.Sounds.DEGRADE, 1f, 4f);
        fx(false);
        BuffIndicator.refreshHero();
        ActionIndicator.clearAction(this);
    }

    public float remainder() {
        return Math.min(1f, this.cooldown())*1.5f;
    }

    public static boolean isPartying(Char ch){
        return !(ch instanceof Hero) && isEnergized() &&
               Dungeon.hero.hasTalent(Talent.PARTY_FEELING) && Dungeon.level.heroFOV[ch.pos] && ch.alignment == Char.Alignment.ALLY;
    }

    public static boolean isEnergized(){
        return Dungeon.hero != null && Dungeon.hero.buff(HighnessBuff.class) != null && Dungeon.hero.buff(HighnessBuff.class).state == State.ENERGIZED;
    }

    @Override
    public int icon() {
        if (state != State.NORMAL)
            return BuffIndicator.HIGHNESS;
        return super.icon();
    }

    @Override
    public void tintIcon(Image icon) {
        if (state == State.RECOVERING){
            icon.tint(0x153e00, 0.66f);
        }
    }

    @Override
    public int indicatorColor() {
        return 0x205C00;
    }

    @Override
    public Visual secondaryVisual() {
        BitmapText txt = new BitmapText(PixelScene.pixelFont);
        txt.text(String.valueOf(availablePower()));
        txt.hardlight(state == State.NORMAL ? CharSprite.NEUTRAL : CharSprite.POSITIVE);
        txt.measure();
        return txt;
    }

    @Override
    public void doAction() {
        if (state == State.NORMAL) {
            SpellSprite.show(target, SpellSprite.PURITY, 0f, 1f, 0.07f);
            GameScene.flash(0x702be538);
            Sample.INSTANCE.play(Assets.Sounds.CHALLENGE, 1f, 4f);
            consumeGrass(1);
            state = State.ENERGIZED;
        } else if (state == State.ENERGIZED){
            startRecovering();
        }

        fx(true);

        BuffIndicator.refreshHero();

        ActionIndicator.refresh();
    }

    @Override
    public void fx(boolean on) {
        if (on && state == State.ENERGIZED){
            target.sprite.add(CharSprite.State.ILLUMINATED);
            target.sprite.aura( 0x7bff84 );
        } else {
            target.sprite.remove(CharSprite.State.ILLUMINATED);
            target.sprite.clearAura();
        }
    }

    @Override
    public void detach() {
        super.detach();
        ActionIndicator.clearAction(this);
    }

    @Override
    public float iconFadePercent() {
        switch (state){
            case RECOVERING:
                return (currentRecovery / maxRecovery);
            case NORMAL: default:
                return 1;
            case ENERGIZED:
                return  currentPower / (float)grassValue();
        }
    }

    public String iconTextDisplay(){
        switch (state){
            case NORMAL: default:
                return "";
            case RECOVERING:
                return Integer.toString((int) currentRecovery);
            case ENERGIZED:
                return Integer.toString(currentPower);
        }
    }

    @Override
    public String name() {
        if (state == State.RECOVERING)
            return Messages.get(this, "recovering");
        return super.name();
    }

    @Override
    public String desc() {
        switch (state){
            case NORMAL:
                return "";
            case ENERGIZED:
                return Messages.get(this, "energized_desc", grassValue());
            case RECOVERING:
                return Messages.get(this, "recovering_desc", dispTurns(currentRecovery));
        }
        return super.desc();
    }

    @Override
    public int actionIcon() {
        switch (state){
            case NORMAL:
                return HeroIcon.HIGHNESS;
            case ENERGIZED:
                return HeroIcon.HIGHNESS_STOP;
            case RECOVERING: default:
                return HeroIcon.NONE;
        }
    }

    @Override
    public String actionName() {
        if (state == State.ENERGIZED)
            return Messages.get(this, "stop_action_name");
        return ActionIndicator.Action.super.actionName();
    }
}
