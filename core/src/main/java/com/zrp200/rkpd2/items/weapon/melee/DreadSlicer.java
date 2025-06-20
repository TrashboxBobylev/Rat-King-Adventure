package com.zrp200.rkpd2.items.weapon.melee;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Dread;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class DreadSlicer extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {
    {
        image = ItemSpriteSheet.DREAD_SWORD;
        hitSound = Assets.Sounds.HIT_SLASH;
        hitSoundPitch = 1.2f;

        tier = 6;
    }

    @Override
    public int min(int lvl) {
        // basically doubled
        return  tier*2 +
                lvl*2;
    }

    @Override
    public boolean canReach(Char owner, int target) {
        Char ch;
        if ((ch = Actor.findChar(target)) != null && ch.buff(Terror.class) != null){
            return canReach(owner, target, Dungeon.level.distance(owner.pos, ch.pos));
        }
        return super.canReach(owner, target);
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (!Dungeon.level.adjacent(attacker.pos, defender.pos))
            damage /= 2;
        if (Random.Int(2) == 0){
            Buff.prolong(defender, Terror.class, 8f).object = curUser.id();
        }
        if (defender.buff(Terror.class) != null){
            defender.buff(Terror.class).ignoreNextHit = true;
        }
        return super.proc(attacker, defender, damage);
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        if (!enemy.isImmune(Dread.class)){
            Buff.affect( enemy, Dread.class ).object = curUser.id();
        } else {
            Buff.affect( enemy, Terror.class, Terror.DURATION * 2f ).object = curUser.id();
        }
        return super.warriorAttack(damage, enemy)*2;
    }

    @Override
    public String targetingPrompt() {
        return Messages.get(this, "prompt");
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target) {
        return super.baseChargeUse(hero, target)*2;
    }

    @Override
    protected DuelistAbility duelistAbility() {
        return new MeleeAbility(2) {

            @Override
            protected void proc(Hero hero, Char enemy) {
                if (!enemy.isImmune(Dread.class)){
                    Buff.affect( enemy, Dread.class ).object = curUser.id();
                } else {
                    Buff.affect( enemy, Terror.class, Terror.DURATION * 2f ).object = curUser.id();
                }
            }
        };
    }
}
