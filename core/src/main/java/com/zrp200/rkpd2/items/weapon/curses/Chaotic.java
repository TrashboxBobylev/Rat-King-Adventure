package com.zrp200.rkpd2.items.weapon.curses;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.trinkets.WondrousResin;
import com.zrp200.rkpd2.items.wands.CursedWand;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.sprites.ItemSprite;

import java.util.HashMap;

public class Chaotic extends Weapon.Enchantment{

    HashMap<Class<? extends Weapon.Enchantment>, Weapon.Enchantment> curses = new HashMap();

    @Override
    public boolean curse() {
        return true;
    }

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        Weapon.Enchantment curse = Weapon.Enchantment.randomCurse(
                // explosive is redundant with cursed wand effect
                Explosive.class
        );
        if(!(curse instanceof Wayward)) {
            // ensure wayward works properly (it's supposed to detach if you successfully hit)
            Buff.detach(attacker, Wayward.WaywardBuff.class);
        }
        if(curse instanceof Chaotic) {
            boolean positiveOnly = attacker instanceof Hero && Random.Float() < WondrousResin.positiveCurseEffectChance();
            Ballistica aim = new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_TARGET);
            CursedWand.randomValidEffect(weapon, attacker, aim, positiveOnly)
                    .effect(weapon, attacker, aim, positiveOnly);
            return damage;
        }
        Class<? extends Weapon.Enchantment> curseClass = curse.getClass();
        if(curses.containsKey(curseClass)) curse = curses.get(curseClass);
        else curses.put(curseClass, curse);
        return curse.proc(weapon, attacker, defender, damage);
    }

    @Override
    public ItemSprite.Glowing glowing() {
        // black
        return new ItemSprite.Glowing(0);
    }

    private static final String CURSES = "CURSES";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(CURSES, curses.values());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        for(Bundlable b : bundle.getCollection(CURSES)) {
            Weapon.Enchantment curse = (Weapon.Enchantment) b;
            curses.put(curse.getClass(), curse);
        }
    }
}
