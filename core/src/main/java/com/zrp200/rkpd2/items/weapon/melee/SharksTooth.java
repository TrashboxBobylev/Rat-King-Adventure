package com.zrp200.rkpd2.items.weapon.melee;

import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.Weakness;
import com.zrp200.rkpd2.actors.buffs.WellFed;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.Chains;
import com.zrp200.rkpd2.effects.Effects;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;

public class SharksTooth extends MeleeWeapon {
    {
        image = ItemSpriteSheet.SHARKS_TOOTH;
        tier = 6;

        RCH = 8;

        DLY = 1.5f;
    }

    @Override
    public int max(int lvl) {
        return  6*(tier+1) +    //42 base, up from 35
                lvl*(tier+2); //+8 per level, up from +7
    }

    private static float spinBonus = 1f;

    @Override
    public int damageRoll(Char owner) {
        int dmg = Math.round(super.damageRoll(owner) * spinBonus);
        if (spinBonus > 1f) Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
        spinBonus = 1f;
        return dmg;
    }

    @Override
    public float accuracyFactor(Char owner, Char target) {
        Flail.SpinAbilityTracker spin = owner.buff(Flail.SpinAbilityTracker.class);
        if (spin != null) {
            Actor.add(new Actor() {
                { actPriority = VFX_PRIO; }
                @Override
                protected boolean act() {
                    if (owner instanceof Hero && !target.isAlive()){
                        onAbilityKill((Hero)owner, target);
                    }
                    Actor.remove(this);
                    return true;
                }
            });
            //we detach and calculate bonus here in case the attack misses (e.g. vs. monks)
            spin.detach();
            spinBonus = 1f + (spin.spins/3f);
            return Float.POSITIVE_INFINITY;
        } else {
            spinBonus = 1f;
            return super.accuracyFactor(owner, target);
        }
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        damage = super.proc(attacker, defender, damage);

        int dist = Dungeon.level.distance(attacker.pos, defender.pos);

        damage = (int) Math.round(damage * Math.pow(1.25f, dist - 1));

        if (dist > 1 && !defender.properties().contains(Char.Property.IMMOVABLE)){
            attacker.spend(-delayFactor(attacker));

            Ballistica chain = new Ballistica(attacker.pos, defender.pos, Ballistica.PROJECTILE);

            int newPos = -1;
            for (int i : chain.subPath(1, chain.dist)) {
                if (!Dungeon.level.solid[i] && Actor.findChar(i) == null) {
                    newPos = i;
                    break;
                }
            }

            if (newPos != -1) {
                final int newPosFinal = newPos;

                new Item().throwSound();
                Sample.INSTANCE.play(Assets.Sounds.CHAINS);
                Dungeon.hero.busy();
                attacker.sprite.parent.add(new Chains(attacker.sprite.center(),
                        defender.sprite.destinationCenter(),
                        Effects.Type.CHAIN,
                        new Callback() {
                            public void call() {
                                attacker.spend(SharksTooth.this.delayFactor(attacker));
                                Actor.add(new Pushing(defender, defender.pos, newPosFinal, new Callback() {
                                    public void call() {
                                        defender.pos = newPosFinal;
                                        defender.sprite.place(newPosFinal);
                                        Dungeon.level.occupyCell(defender);
                                        Buff.prolong(defender, Cripple.class, 5f);
                                        Buff.prolong(defender, Vulnerable.class, 5f);
                                        Buff.prolong(defender, Weakness.class, 5f);
                                        Dungeon.observe();
                                        GameScene.updateFog();
                                    }
                                }));
                                Dungeon.hero.ready();
                                attacker.next();
                            }
                        }));
            } else {
                attacker.spend(SharksTooth.this.delayFactor(attacker));
            }
        }

        return damage;
    }

    @Override
    public int warriorAttack(int damage, Char enemy) {
        Buff.affect(Dungeon.hero, WellFed.class).add(damage);
        return super.warriorAttack(damage, enemy);
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target){
        if (Dungeon.hero.buff(Flail.SpinAbilityTracker.class) != null){
            return 0;
        } else {
            return 3;
        }
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {

        Flail.SpinAbilityTracker spin = hero.buff(Flail.SpinAbilityTracker.class);

        beforeAbilityUsed(hero, null);
        if (spin == null){
            spin = Buff.affect(hero, Flail.SpinAbilityTracker.class, 3f);
        }

        spin.spins++;
        Buff.prolong(hero, Flail.SpinAbilityTracker.class, 3f);
        Sample.INSTANCE.play(Assets.Sounds.CHAINS, 1, 1, 0.9f + 0.1f*spin.spins);
        hero.sprite.operate(hero.pos);
        hero.spendAndNext(Actor.TICK);
        BuffIndicator.refreshHero();

        afterAbilityUsed(hero);
    }
}
