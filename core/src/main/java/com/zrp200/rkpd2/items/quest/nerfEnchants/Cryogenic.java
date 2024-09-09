package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Chill;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Cryogenic extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 5)) {
            new FlavourBuff(){
                {actPriority = VFX_PRIO;}
                public boolean act() {
                    Buff.affect(target, Frost.class, Frost.DURATION*1.5f);
                    Buff.affect(target, Chill.class, Frost.DURATION*6);
                    return super.act();
                }
            }.attachTo(defender);
            return Math.round(damage*1.25f);
        }
        return damage;
    }

    public static ItemSprite.Glowing BLUE = new ItemSprite.Glowing( 0x66b3ff );

    @Override
    public ItemSprite.Glowing glowing() {
        return BLUE;
    }
}
