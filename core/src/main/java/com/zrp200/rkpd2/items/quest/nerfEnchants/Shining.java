package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.items.bombs.Flashbang;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Shining extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 6)) {
            for (int i : PathFinder.NEIGHBOURS9){
                Char ch = Actor.findChar(defender.pos + i);
                if (ch != null) {
                    new Flare(8, 25).color(0xFFFFFF, true).show(ch.sprite, 1.2f);
                    Buff.affect(ch, Blindness.class, ch == defender ? Blindness.DURATION*1.5f : Blindness.DURATION);
                    Buff.affect(ch, Cripple.class, ch == defender ? Blindness.DURATION/1.33f : Blindness.DURATION/2);
                    ch.damage(Math.round(level*1.5f), new Flashbang());
                }
            }
        }
        return damage;
    }

    private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0xd9d9d9 );

    @Override
    public ItemSprite.Glowing glowing() {
        return GREY;
    }
}
