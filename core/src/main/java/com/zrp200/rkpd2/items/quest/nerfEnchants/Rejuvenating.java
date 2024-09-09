package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.bombs.RegrowthBomb;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Rejuvenating extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 10)) {
            new RegrowthBomb().explode(defender.pos);
            int healAmt = Math.round(damage * 1.33f);
            healAmt = Math.min( healAmt, attacker.HT - attacker.HP );

            if (healAmt > 0 && attacker.isAlive()) {

                attacker.HP += healAmt;
                attacker.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 1 );
                attacker.sprite.showStatus( CharSprite.POSITIVE, Integer.toString( healAmt ) );

            }
        }
        return damage;
    }

    public static ItemSprite.Glowing GREEN = new ItemSprite.Glowing( 0x2ee62e );

    @Override
    public ItemSprite.Glowing glowing() {
        return GREEN;
    }
}
