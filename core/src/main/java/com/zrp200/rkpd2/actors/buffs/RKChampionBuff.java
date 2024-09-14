package com.zrp200.rkpd2.actors.buffs;

import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.HeroSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
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
            return 65;
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
}
