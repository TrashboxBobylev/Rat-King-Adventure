package com.zrp200.rkpd2.actors.buffs;

import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.WarpPile;

public class Warp extends Buff {
    {
        revivePersists = true;
    }

    private float stacks = 0;
    private float decay = 0;
    private float totalDuration = 0;
    private float timer = 0;

    @Override
    public int icon() {
        return BuffIndicator.WARP;
    }

    @Override
    public String toString() {
        return Messages.get(this, "name");
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", getStacks(), getDecay());
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString(Math.round(getStacks() * getDecay()));
    }

    @Override
    public float iconFadePercent() {
        return (totalDuration - (getDecay() * getStacks())) / totalDuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean act() {
        if (target.isAlive()) {

            spend( TICK );
            timer++;
            stacks -= 1/decay;
            if (timer >= WarpPile.effectTimer(stacks)){
                timer = 0;
                float[] category = WarpPile.getChanceCat(Math.round(getStacks()));
                int categoryID = Random.chances(category);
                WarpPile.WarpEffect effect = (WarpPile.WarpEffect) Random.chances(WarpPile.effectTypes[categoryID]);
                effect.call();
            }
            if (stacks <= 0) {
                detach();
            }

        } else {

            detach();

        }
        return true;
    }

    public static final String STACKS = "enemy_stacks";
    public static final String DAMAGE = "damage_inc";
    public static final String DURATION = "duration";
    public static final String TIMER = "timer";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(STACKS, getStacks());
        bundle.put(DAMAGE, getDecay());
        bundle.put(DURATION, totalDuration);
        bundle.put(TIMER, timer);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        stacks = bundle.getFloat(STACKS);
        decay = bundle.getFloat(DAMAGE);
        totalDuration = bundle.getFloat(DURATION);
        timer = bundle.getFloat(TIMER);
    }

    public static Warp inflict(float stacks, float decay){
        Warp effect = Buff.affect(Dungeon.hero, Warp.class);
        if (effect.stacks > 0) {
            effect.setStacks(effect.stacks + stacks);
            if (effect.cooldown() <= 0)
                effect.postpone(WarpPile.DECAY_DELAY / 2);
        } else {
            effect.setStacks(stacks);
            effect.postpone(WarpPile.DECAY_DELAY);
        }
        if (effect.decay > 0)
            effect.setDecay((effect.decay*2 + decay)/3);
        else
            effect.setDecay(decay);
        effect.totalDuration = effect.getStacks() * effect.getDecay() + effect.cooldown();
        return effect;
    }

    public static Warp modify(float stacks){
        Warp effect = Buff.affect(Dungeon.hero, Warp.class);
        float initialStacks = effect.stacks;
        effect.setStacks(effect.stacks + stacks);
        if (initialStacks < effect.stacks) {
            effect.postpone(WarpPile.DECAY_DELAY / 2);
            effect.totalDuration = effect.getStacks() * effect.getDecay() + effect.cooldown();
        }
        return effect;
    }

    public static float stacks(){
        return Buff.affect(Dungeon.hero, Warp.class).stacks;
    }

    public float getStacks() {
        return stacks;
    }

    public void setStacks(float stacks) {
        this.stacks = Math.min(WarpPile.getMaxWarp(), stacks);
    }

    public float getDecay() {
        return decay;
    }

    public void setDecay(float decay) {
        this.decay = decay;
    }
}
