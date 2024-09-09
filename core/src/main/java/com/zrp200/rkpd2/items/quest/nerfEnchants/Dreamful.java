package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Drowsy;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.enchantments.Grim;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Dreamful extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 4)) {
            //need to delay this so damage from the dart doesn't break wandering
            new FlavourBuff(){
                {actPriority = VFX_PRIO;}
                public boolean act() {
                    if (((Mob) defender).state == ((Mob) defender).HUNTING || ((Mob) defender).state == ((Mob) defender).FLEEING){
                        ((Mob) defender).state = ((Mob) defender).WANDERING;
                    }
                    ((Mob) defender).beckon(Dungeon.level.randomDestination(defender));
                    defender.sprite.showLost();
                    Buff.affect(defender, Drowsy.class);
                    return super.act();
                }
            }.attachTo(defender);
        }

        int enemyHealth = defender.HP - damage;
        if (enemyHealth <= 0) return damage; //no point in proccing if they're already dead.

        //scales from 0 - 40% based on how low hp the enemy is, plus 4% per level
        float maxChance = 0.4f + .05f*level;
        float chanceMulti = (float)Math.pow( ((defender.HT - enemyHealth) / (float)defender.HT), 2);
        float chance = maxChance * chanceMulti;

        chance *= procChanceMultiplier(attacker);

        if (Random.Float() < chance) {

            defender.damage( defender.HP, this );
            defender.sprite.centerEmitter().start( Speck.factory( Speck.NOTE ), 0.3f, 5 );

            if (!defender.isAlive() && attacker instanceof Hero
                    //this prevents unstable from triggering grim achievement
                    && weapon.hasEnchant(Grim.class, attacker)) {
                Badges.validateGrimWeapon();
            }

        }
        return damage;
    }

    public static ItemSprite.Glowing DREAM = new ItemSprite.Glowing( 0xff4ce3 );

    @Override
    public ItemSprite.Glowing glowing() {
        return DREAM;
    }
}
