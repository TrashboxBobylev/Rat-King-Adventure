package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Chill;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Hex;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.Ooze;
import com.zrp200.rkpd2.actors.buffs.Poison;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.buffs.Slow;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.Weakness;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class ElementalDirk extends AssassinsBlade {
    {
        image = ItemSpriteSheet.ELEMENTAL_DIRK;
        tier = 6;
    }

    @Override
    public int max(int lvl) {
        return  6*(tier+1) +    //36 base, down from 30
                lvl*(tier+1);   //scaling unchanged
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
            switch (Random.Int(4)) {
                case 0:
                    Buff.affect(defender, Burning.class).reignite(defender, 5);
                    break;
                case 1:
                    Buff.affect(defender, Chill.class, 5f);
                    break;
                case 2:
                    Buff.affect(defender, Ooze.class);
                    break;
                case 3:
                    Buff.affect(defender, Poison.class).set(12);
                    break;
            }
        return super.proc(attacker, defender, damage);
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        Buff.affect(enemy, Ooze.class).set(30);
        Buff.affect(enemy, Chill.class, 15);
        Buff.affect(enemy, Burning.class).reignite(enemy, 10);
        Buff.affect(enemy, Poison.class).set(15);
        Buff.affect(enemy, Hex.class, 15);
        Buff.affect(enemy, Vulnerable.class, 15);
        Buff.affect(enemy, Weakness.class, 15);
        Buff.affect(enemy, Vertigo.class, 15);
        Buff.affect(enemy, Terror.class, 15).object = Dungeon.hero.id();
        return super.warriorAttack(damage, enemy);
    }

    enum ElementalDirkPower {
        SOLAR(MagicMissile.FIRE_CONE, 0xFFD836, Burning.class, Vulnerable.class, Vertigo.class),
        VORTEX(MagicMissile.FOLIAGE_CONE, 0x55ECC0, Ooze.class, Roots.class, Slow.class),
        NEBULA(MagicMissile.POISON, 0xD75BD7, Poison.class, Hex.class, Blindness.class),
        STARDUST(MagicMissile.FROST_CONE, 0x6BC5EA, Chill.class, Weakness.class, Cripple.class);

        final int zapType;
        final int color;
        final Class<? extends Buff>[] debuffs;

        @SafeVarargs
        ElementalDirkPower(final int zapType, final int zapCollideColor, final Class<? extends Buff>... debuffs){
            this.zapType = zapType;
            this.color = zapCollideColor;
            this.debuffs = debuffs;
        }
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target) {
        return 1;
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return new Ballistica( user.pos, dst, Ballistica.FRIENDLY_MAGIC_BOLT ).collisionPos;
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        if (target == null || hero.pos == target) {
            GLog.i( Messages.get(Wand.class, "self_target") );
            return;
        }

        hero.busy();

        beforeAbilityUsed(hero, null);
        hero.sprite.zap(target);
        Invisibility.dispel();

        ElementalDirkPower currentPower = Random.element(ElementalDirkPower.values());
        final Ballistica shot = new Ballistica( curUser.pos, target, Ballistica.FRIENDLY_MAGIC_BOLT,
                curUser.buff(ChampionEnemy.Projecting.class) != null && curUser.pointsInTalent(Talent.RK_PROJECT) == 3);
        final int DEBUFF_DURATION = 10;

        Sample.INSTANCE.play( Assets.Sounds.ZAP );

        MagicMissile.boltFromChar( hero.sprite.parent,
            currentPower.zapType,
            hero.sprite,
            shot.collisionPos,
            () -> {
                Char ch = Actor.findChar(shot.collisionPos);

                if (ch != null){
                    ch.damage(Math.round(damageRoll(hero) * 0.75f), ElementalDirk.this);
                    Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );
                    ch.sprite.burst( 0xEE000000 + currentPower.color, buffedLvl() + 4 );
                    for (Class<? extends Buff> debuff: currentPower.debuffs){
                        //setups for non-flavour buffs
                        if (debuff == Burning.class){
                            Buff.affect(ch, Burning.class).reignite(ch, DEBUFF_DURATION);
                        } else if (debuff == Ooze.class){
                            Buff.affect(ch, Ooze.class).set(DEBUFF_DURATION);
                        } else if (debuff == Poison.class){
                            Buff.affect(ch, Poison.class).set(DEBUFF_DURATION);
                        } else {
                            Buff.affect(ch, debuff).postpone(ch.resist(debuff)*DEBUFF_DURATION);
                        }
                    }
                } else {
                    Dungeon.level.pressCell(shot.collisionPos);
                }

                updateQuickslot();
                hero.spendAndNext(delayFactor(hero));
                afterAbilityUsed(hero);
            });
    }
}
