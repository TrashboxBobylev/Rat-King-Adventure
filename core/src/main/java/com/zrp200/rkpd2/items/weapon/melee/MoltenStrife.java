package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.bombs.ShrapnelBomb;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;

import java.text.DecimalFormat;

public class MoltenStrife extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {
    {
        image = ItemSpriteSheet.MOLTEN_STRIFE;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.6f;

        tier = 6;
    }

    @Override
    public int max(int lvl) {
        return 4*(tier+1) + tier*lvl;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (Random.Int(5+buffedLvl()) < (buffedLvl()+1)) {
            Bomb.doNotDamageHero = true;
            new Bomb().explode(defender.pos);
            Bomb.doNotDamageHero = false;
        }

        return super.proc(attacker, defender, damage);
    }

    @Override
    public String statsInfo() {
        if (isIdentified())
            return Messages.get(this, "stats_desc", new DecimalFormat("#.#").
                    format(100 * ((buffedLvl()+1f) / (5f+buffedLvl()))));
        return Messages.get(this, "stats_desc", new DecimalFormat("#.#").format(20));
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        Bomb.doNotDamageHero = true;
        new ShrapnelBomb().explode(enemy.pos);
        new ShrapnelBomb().explode(enemy.pos);
        Bomb.doNotDamageHero = false;
        return super.warriorAttack(damage, enemy);
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected DuelistAbility duelistAbility() {
        return new Ignite();
    }

    public static class Ignite extends MeleeAbility {
        @Override
        public float dmgMulti(Char enemy) {
            return 0f;
        }

        @Override
        public void afterHit(Char enemy, boolean hit) {
            if (enemy.isAlive()) {
                Buff.affect(enemy, BombDebuff.class).increment(7f);
            }
        }
    }

    public static class BombDebuff extends Buff {

        {
            immunities.add(Bomb.class);
        }

        protected float left;

        private static final String LEFT	= "left";

        {
            type = buffType.NEGATIVE;
            announced = true;
        }

        @Override
        public void storeInBundle( Bundle bundle ) {
            super.storeInBundle( bundle );
            bundle.put( LEFT, left );
        }

        @Override
        public void restoreFromBundle( Bundle bundle ) {
            super.restoreFromBundle( bundle );
            left = bundle.getFloat( LEFT );
        }

        public void increment(float duration) {
            this.left += duration;
        }

        @Override
        public boolean act() {
            if (target.isAlive()) {
                new Bomb().explode(target.pos);

                spend( TICK );
                if ((left -= TICK) <= 0) {
                    detach();
                }
            } else {
                detach();
            }

            return true;
        }

        @Override
        public void fx(boolean on) {
            if (on) target.sprite.add(CharSprite.State.BURNING);
            else target.sprite.remove(CharSprite.State.BURNING);
        }

        @Override
        public int icon() {
            return BuffIndicator.FIRE;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0xf8b659);
        }

        @Override
        public String iconTextDisplay() {
            return dispTurns(left);
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", dispTurns(left));
        }
    }


}
