package com.zrp200.rkpd2.actors.buffs;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpiritHawk;
import com.zrp200.rkpd2.actors.hero.abilities.rogue.ShadowClone;
import com.zrp200.rkpd2.actors.mobs.Bee;
import com.zrp200.rkpd2.actors.mobs.SpectreRat;
import com.zrp200.rkpd2.actors.mobs.npcs.MirrorImage;
import com.zrp200.rkpd2.actors.mobs.npcs.PrismaticImage;
import com.zrp200.rkpd2.actors.mobs.npcs.Sheep;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfSirensSong;
import com.zrp200.rkpd2.items.spells.SummonElemental;
import com.zrp200.rkpd2.items.wands.WandOfLivingEarth;
import com.zrp200.rkpd2.items.wands.WandOfRegrowth;
import com.zrp200.rkpd2.items.wands.WandOfWarding;
import com.zrp200.rkpd2.items.weapon.melee.BloomingPick;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.HeroSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.SafeCast;
import com.zrp200.rkpd2.windows.WndRkChampion;

public class RKChampionBuff extends Buff implements ActionIndicator.Action {

    {
        actPriority = BUFF_PRIO - 1;
    }

    public static class RainbowRat extends Image{

        private float phase;
        private boolean glowUp;
        private ItemSprite.Glowing glowing;

        public RainbowRat() {
            super();
            copy(HeroSprite.avatar(Dungeon.hero.heroClass, 6));
        }

        @Override
        public void update() {
            super.update();
            if (glowing == null) glowing = new ItemSprite.Glowing();
            glowing.period = 0.66f;
            if (visible) {
                if (glowUp && (phase += Game.elapsed) > glowing.period) {

                    glowUp = false;
                    phase = glowing.period;

                } else if (!glowUp && (phase -= Game.elapsed) < 0) {

                    glowUp = true;
                    phase = 0;

                }

                float value = phase / glowing.period * 0.9f;
                glowing.tempColor.fromHsv(Random.Float(360), Random.Float(0.15f, 0.5f), 0.9f);
                glowing.red = glowing.tempColor.r;
                glowing.blue = glowing.tempColor.b;
                glowing.green = glowing.tempColor.g;

                rm = gm = bm = 1 - value;
                ra = glowing.red * value;
                ga = glowing.green * value;
                ba = glowing.blue * value;
            }
        }
    }

    public static class ChampionCooldown extends Talent.Cooldown {
        @Override
        public float duration() {
            return 50;
        }
        public int icon() { return BuffIndicator.LASTSTAND; }
        public void tintIcon(Image icon) { icon.hardlight(1f, 1f, 0f); }
    }

    @Override
    public boolean attachTo(Char target) {
        boolean attach = super.attachTo(target);
        if (target.buff(ChampionCooldown.class) == null)
            ActionIndicator.setAction(this);
        else
            ActionIndicator.clearAction(this);
        return attach;
    }

    @Override
    public boolean act() {
        if (target.buff(ChampionCooldown.class) == null)
            ActionIndicator.setAction(this);
        else
            ActionIndicator.clearAction(this);

        Hero hero = SafeCast.cast(target, Hero.class);
        if (hero != null){
            if (hero.hasTalent(Talent.RK_ANTIMAGIC)){
                Buff.affect(hero, Talent.ThaumicForcefieldTracker.class);
            }
            if (hero.hasTalent(Talent.RK_PALADIN)){
                hero.updateHT(false);
            }
        }

        spend(TICK);
        return true;
    }

    @Override
    public Image primaryVisual() {
        return new RainbowRat();
    }

    @Override
    public int indicatorColor() {
        return 0xFFFFFF;
    }

    @Override
    public void doAction() {
        GameScene.show(new WndRkChampion(this));
    }

    public void useTitle(Class<? extends ChampionEnemy> title){
        Dungeon.hero.sprite.emitter().burst(Speck.factory(Speck.STAR), 20);
        Sample.INSTANCE.play(Assets.Sounds.READ, 1f, 0.66f);
        Dungeon.hero.busy();
        Dungeon.hero.sprite.operate(Dungeon.hero.pos, () -> {
            Dungeon.hero.sprite.idle();
            Dungeon.hero.ready();
            for (Buff buff : Dungeon.hero.buffs().toArray(new Buff[0])){
                if (buff instanceof ChampionEnemy){
                    buff.detach();
                }
            }
            if (title != null) {
                Buff.affect(Dungeon.hero, title);
            }
            Dungeon.hero.sprite.resetColor();
            Talent.Cooldown.affectHero(ChampionCooldown.class);
            ActionIndicator.clearAction(this);
            Dungeon.hero.spendAndNext(1f);
        });
    }

    @Override
    public boolean usable() {
        return target.buff(ChampionCooldown.class) == null;
    }

    private static final Class<? extends Char>[] rkPaladinAllyClasses = new Class[]{
        MirrorImage.class, PrismaticImage.class, DriedRose.GhostHero.class,
        WandOfWarding.Ward.class, WandOfLivingEarth.EarthGuardian.class, Bee.class,
        SpiritHawk.HawkAlly.class, ShadowClone.ShadowAlly.class, Ratmogrify.SummonedRat.class,
        Ratmogrify.SummonedAlbino.class, Ratmogrify.TransmogRat.class,
        WandOfRegrowth.Lotus.class, Sheep.class, SpectreRat.class,
    };

    private static final Class<? extends Buff>[] rkPaladinAllyBuffs   = new Class[]{
        ScrollOfSirensSong.Enthralled.class, Corruption.class, SummonElemental.InvisAlly.class,
        BloomingPick.VineCovered.class, SpiritBuff.WraithMark.class
    };

    public static int rkPaladinUniqueAllies(){
        int allies = 0;
        for (Class<? extends Char> pattern: rkPaladinAllyClasses){
            Char potentialAlly = Actor.findByClass(pattern);
            if (potentialAlly != null && Dungeon.level.heroFOV[potentialAlly.pos]){
                if (potentialAlly.alignment == Char.Alignment.ALLY)
                    allies++;
            }
        }
        for (Class<? extends Buff> pattern: rkPaladinAllyBuffs){
            allyBuffCheck:
            {
                for (Char potentialAlly : Actor.chars()) {
                    if (Dungeon.level.heroFOV[potentialAlly.pos]) {
                        for (Buff potentialAllyBuff : potentialAlly.buffs()) {
                            if (potentialAllyBuff.getClass().isInstance(pattern)) {
                                allies++;
                                break allyBuffCheck;
                            }
                        }
                    }
                }
            }
        }

        return allies;
    }
}
