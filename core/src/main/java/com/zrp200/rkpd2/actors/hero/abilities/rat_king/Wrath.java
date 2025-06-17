package com.zrp200.rkpd2.actors.hero.abilities.rat_king;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.DLCAllyBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.StuckBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpectralBlades;
import com.zrp200.rkpd2.actors.hero.abilities.mage.ElementalBlast;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.SmokeBomb;
import com.zrp200.rkpd2.actors.hero.abilities.warrior.Shockwave;
import com.zrp200.rkpd2.actors.mobs.SpectreRat;
import com.zrp200.rkpd2.effects.particles.BloodParticle;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.levels.features.Chasm;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ConeAOE;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.MobSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.HashSet;

import static com.zrp200.rkpd2.actors.hero.Talent.*;

public class Wrath extends ArmorAbility {

    {
        baseChargeUse = 70;
    }

    @Override public String targetingPrompt() {
        return new SmokeBomb().targetingPrompt();
    }

    @Override public int icon() { return HeroIcon.WRATH; }

    @Override public Talent[] talents() {
        return new Talent[]{AFTERSHOCK, Talent.RAT_BLAST, SMOKE_AND_MIRRORS, SEA_OF_BLADES, FUN};
    }

    @Override public float chargeUse(Hero hero) {
        float chargeUse = super.chargeUse(hero);
        chargeUse += hero.pointsInTalent(FUN)*6;
        if (SmokeBomb.isShadowStep(hero)){
            // shadow step: reduced charge use by 16%/30%/41%/50%
            chargeUse *= Math.pow(0.84, hero.pointsInTalent(SMOKE_AND_MIRRORS));
        }
        return chargeUse;
    }

    @Override
    public String shortDesc() {
        if(SmokeBomb.isShadowStep(Dungeon.hero)) return Messages.get(this, "desc_shadow_step");
        return super.shortDesc();
    }

    private static final float JUMP_DELAY=2f;

    private ClassArmor armor;
    private Hero hero;
    private int target;
    private boolean[] stages;

    @Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
        if(target == null) return;
        this.armor = armor;
        this.hero = hero;
        this.target = target;

        stages = new boolean[3]; // jump/blast/blades

        // smoke bomb -> shockwave aoe -> blast -> blades
        if(!SmokeBomb.isShadowStep(hero) && target == hero.pos) stages[0] = false; // self-targeting skips the jump portion if not shadow-stepping.
        else if(!doSmokeBomb()) return;
        else stages[0] = true;

        Invisibility.dispel();

        if(stages[0]) {
            doShockwave( () -> doBlast(this::doSpectralBlades) );
        }
        else {
            doBlast(this::doSpectralBlades);
        }
    }

    private boolean doSmokeBomb() {
        if( !SmokeBomb.isValidTarget(hero, target, 6) ) return false;

        boolean isShadowStep = SmokeBomb.isShadowStep(hero);
        if(!isShadowStep) {
            SmokeBomb.blindAdjacentMobs(hero);
            SmokeBomb.doBodyReplacement(hero, SMOKE_AND_MIRRORS, RatStatue.class);
            if (hero.hasTalent(SMOKE_AND_MIRRORS) && hero.pointsInTalent(Talent.FUN) > 2){
                for (int i : PathFinder.NEIGHBOURS4){
                    int poss = hero.pos+i;
                    if (Dungeon.level.insideMap(poss) && Dungeon.level.passable[poss] && Actor.findChar(poss) == null){
                        SpectreRat rat = new SpectreRat();
                        rat.pos = poss;
                        rat.HP = rat.HT = rat.HT/2;
                        Buff.affect(rat, DLCAllyBuff.class);
                        Buff.affect(rat, StuckBuff.class);
                        GameScene.add(rat);
                    }
                }
            }
        }
        SmokeBomb.throwSmokeBomb(hero, target);

        if(isShadowStep) {
            // shadow step: end the whole thing right here.
            armor.useCharge(hero, this);
            hero.next();
            return false;
        }

        return true;
    }
    private void doShockwave(Callback next) {
        // *technically* it should actually go nowhere, at +4 its range is 1.5 lol.
        // but for the sake of sanity I'm roughly doubling it, thus 1/1-2/2/2-3/3
        // TODO find a way to do "half" distance intervals, like rogue searching, the randomness can be a put-off.
        // note that I've nerfed most of its mechanics by ~20% to try to compensate for the range liberties.
        Shockwave.activate(hero, hero.pos,
                360, Random.round((2+hero.pointsInTalent(AFTERSHOCK))/2f),
                next);
    }
    private void doBlast(Callback next) {
        // elemental blast talent lines up almost perfectly so there's very little additional logic for it, aside from the custom fx.
        if(stages[1] = ElementalBlast.activate(hero, next)) {
            Sample.INSTANCE.play(Assets.Sounds.CHARGEUP,0.5f); // this sound is disproportionately loud.
        }
    }

    private void doSpectralBlades(){
        doSpectralBlades(true);
    }

    private void doSpectralBlades(boolean doubleBlades) { // fixme this code....aaaaaaaaaaaaaaaaaaaaaaa
        Ballistica b = new Ballistica(hero.pos, target, Ballistica.WONT_STOP);
        final HashSet<Char> targets = new HashSet<>();

        ConeAOE cone = new ConeAOE(b, 360);
        for (Ballistica ray : cone.rays) {
            Char toAdd = SpectralBlades.findChar(ray, hero,
                    2 * hero.pointsInTalent(SEA_OF_BLADES), targets);
            if (toAdd != null && toAdd.isAlive() && hero.fieldOfView[toAdd.pos]) {
                targets.add(toAdd);
            }
        }
        // todo should I have it proc enchant at boosted rate to better reflect actual spectral blades?
        // 1x/1.5x/2x/2.5x/3x total effectiveness
        final float mult = Math.min(1,
                (1 + .5f * hero.pointsInTalent(SEA_OF_BLADES)) / targets.size());

        final HashSet<Callback> callbacks = new HashSet<>();

        stages[2] = !targets.isEmpty();
        if(!stages[2]) {
            finish();
            return;
        }
        // turn towards the average point of all enemies being shot at.
        Point sum = new Point();
        for (Char ch : targets) sum.offset(Dungeon.level.cellToPoint(ch.pos));
        sum.scale(1f / targets.size());
        hero.sprite.zap(Dungeon.level.pointToCell(sum), () -> {
            Callback onComplete = () -> {
                if (hero.hasTalent(FUN) && doubleBlades){
                    doSpectralBlades(false);
                } else {
                    finish();
                }
            };
            for (Char ch : targets){
                if (hero.hasTalent(FUN)) Sample.INSTANCE.play(Assets.Sounds.BIRD);
                SpectralBlades.shoot(
                        hero, ch,
                        mult, 1 + 1/4f * hero.pointsInTalent(SEA_OF_BLADES),
                        SEA_OF_BLADES, SeaOfBladesTracker.class,
                        callbacks, onComplete);
            }
            hero.busy();
        });
    }
    public static class SeaOfBladesTracker extends Talent.SpiritBladesTracker {
        private float effectiveness;
        @Override public float getModifier() { return super.getModifier() * effectiveness; }
        @Override public void setModifier(float modifier) { effectiveness = modifier; }
    }

    private void finish() {
        // make sure the hero ends up idling.
        hero.sprite.doAfterAnim(hero.sprite::idle);

        int delay = 0;
        if(stages[1]) delay++; // blast
        if(stages[2]) delay += hero.attackDelay(); // spectral blades
        if(stages[0]) {
            if(hero.hasTalent(SMOKE_AND_MIRRORS)) {
                Actor.addDelayed(new Actor() {
                    { actPriority = HERO_PRIO; } // this is basically the hero acting.
                    @Override protected boolean act() {
                        SmokeBomb.applyHastyRetreat(hero);
                        remove(this);
                        return true;
                    }
                },(delay += JUMP_DELAY)-1);
            }
            else delay += JUMP_DELAY;
        }

        hero.spendAndNext(delay);
        for(boolean stage : stages) if(stage) { armor.useCharge(hero, this); return; }
    }

    @Override
    public boolean isTracked(Hero hero) {
        return Actor.containsClass(RatStatue.class);
    }

    public static class RatStatue extends SmokeBomb.NinjaLog {
        {
            spriteClass = Sprite.class;
            talent = SMOKE_AND_MIRRORS;
            drScaling = 3;
        }

        @Override
        public void die(Object cause) {
            super.die(cause);
            if(cause != Chasm.class) {
                ( (Sprite) sprite ).playDieFX(cause != null);
            }
        }

        public static class Sprite extends MobSprite {
            // there's literally only one frame, and it is 'null'
            private final static Object[] NULL = {null};

            public Sprite() {
                texture( Assets.Sprites.RAT_STATUE );

                TextureFilm frames = new TextureFilm( texture );

                idle = new Animation( 0, true );
                idle.frames( frames, NULL );

                run = attack = zap = idle;

                die = new Animation( 1, false);
                die.frames( frames, NULL );

                play( idle );

            }

            private void playDieFX(final boolean burst) {
                if( !isVisible() ) return;

                if(burst) Sample.INSTANCE.play(Assets.Sounds.SHATTER, 1, 1.4f);

                emitter().burst( (emitter, index, x, y) -> {
                    BloodParticle blood = emitter.recycle(BloodParticle.class);
                    blood.color( blood() );
                    if(burst) blood.resetBurst(x,y); else blood.reset(x,y);
                }, 20);

                killAndErase();
            }

            @Override public void showAlert() {/*do nothing*/}
            @Override public int blood() { return 0xFFcdcdb7; }
        }
    }
}
