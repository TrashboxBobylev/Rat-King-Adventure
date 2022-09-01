package com.zrp200.rkpd2.utils;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.buffs.*;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.levels.traps.SummoningTrap;
import com.zrp200.rkpd2.messages.Messages;

import java.util.HashMap;

public class WarpPile {

    public static final int COLOR = 0xc22ec9;

    //initial wait in turns before warp will start decaying
    public static final float DECAY_DELAY = 8;
    //the (1/X) portion of warp that will be used to use an effect
    public static final float EFFECT_BASE = 6;
    //the cap for how much warp you can have
    public static final float MAX_WARP = 150;
    //the (1/X) chance for effect to come out every time the warp decays
    public static final int EFFECT_CHANCE = 8;

    public interface WarpEffect extends Callback {
        void doEffect(Hero target, float warpAmount);

        @Override
        default void call(){
            float warpAmount = Warp.stacks();
            Warp.modify(-warpAmount/EFFECT_BASE);
            Sample.INSTANCE.play(Assets.Sounds.CURSED);
            GLog.d(Messages.get(this, "message"));
            doEffect(Dungeon.hero, warpAmount);
        }
    }

    public static float[][] categoryChances = {
            {90, 60, 20},
            {8, 30, 45},
            {2, 10, 35}
    };

    public static float[] getChanceCat(int warp){
        if (warp < UNCOMMON_THRESHOLD) return categoryChances[0];
        else if (warp < RARE_THRESHOLD) return categoryChances[1];
        else if (warp >= RARE_THRESHOLD) return categoryChances[2];
        return new float[0];
    }

    public static int COMMON_THRESHOLD = 10;
    public static HashMap<WarpEffect, Float> commonEffects = new HashMap<>();
    static {
        commonEffects.put(new VulnerableEffect(), 15f);
        commonEffects.put(new ScamEffect(), 12f);
        commonEffects.put(new VertigoEffect(), 10f);
        commonEffects.put(new AdrenalineEffect(), 9f);
        commonEffects.put(new FireEffect(), 7f);
        commonEffects.put(new DegradeEffect(), 6f);
    }

    public static int UNCOMMON_THRESHOLD = 50;
    public static HashMap<WarpEffect, Float> uncommonEffects = new HashMap<>();
    static {
        uncommonEffects.put(new ColdEffect(), 10f);
        uncommonEffects.put(new VisionEffect(), 5f);
    }

    public static int RARE_THRESHOLD = 100;
    public static HashMap<WarpEffect, Float> rareEffects = new HashMap<>();

    public static HashMap[] effectTypes = new HashMap[]{commonEffects, uncommonEffects, rareEffects};
    static {
        rareEffects.put(new SummonEffect(), 10f);
    }

    /** Common effects **/

    public static class DegradeEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.prolong(target, Degrade.class, 10 + warpAmount / 5);
        }
    }

    public static class VertigoEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.prolong(target, Vertigo.class, 6 + warpAmount / 8);
        }
    }

    public static class VulnerableEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.prolong(target, Vulnerable.class, 12 + warpAmount / 4);
        }
    }

    public static class ScamEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.prolong(target, Scam.class, 20 + warpAmount / 3);
        }
    }

    public static class AdrenalineEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.prolong(target, Adrenaline.class, 5 + warpAmount / 12);
        }
    }

    public static class FireEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.affect(target, Burning.class).reignite(target, 2);
        }
    }

    /** Uncommon effects **/

    public static class ColdEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.prolong(target, Frost.class, 9 + warpAmount / 8);
            Buff.prolong(target, Chill.class, 12 + warpAmount / 6);
        }
    }

    public static class VisionEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            Buff.prolong(target, MagicalSight.class, 5 + warpAmount / 12);
        }
    }

    /** Rare effects **/

    public static class SummonEffect implements WarpEffect {
        @Override
        public void doEffect(Hero target, float warpAmount) {
            new SummoningTrap().set(target.pos).activate();
        }
    }
}
