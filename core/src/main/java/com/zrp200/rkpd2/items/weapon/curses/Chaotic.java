package com.zrp200.rkpd2.items.weapon.curses;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.HashMap;

import static com.zrp200.rkpd2.items.wands.CursedWand.cursedEffect;

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
            cursedEffect(weapon, attacker, defender);
            return damage;
        }
        Class<? extends Weapon.Enchantment> curseClass = curse.getClass();
        if(curses.containsKey(curseClass)) curse = curses.get(curseClass);
        else curses.put(curseClass, curse);
        return curse.proc(weapon, attacker, defender, damage);
    }

    @Override
    public ItemSprite.Glowing glowing() {
        // chaotic
        return new ItemSprite.Glowing();
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
