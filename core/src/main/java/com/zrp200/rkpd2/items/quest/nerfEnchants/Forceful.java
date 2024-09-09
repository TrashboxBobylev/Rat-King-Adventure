package com.zrp200.rkpd2.items.quest.nerfEnchants;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.items.wands.WandOfBlastWave;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.sprites.ItemSprite;

public class Forceful extends Weapon.Enchantment {
    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int level = Math.max(0, weapon.buffedLvl());
        if (Weapon.Enchantment.proc(attacker, level, 1, 3)) {
            //trace a ballistica to our target (which will also extend past them
            Ballistica trajectory = new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_TARGET);
            //trim it to just be the part that goes past them
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size()-1), Ballistica.PROJECTILE);
            //knock them back along that ballistica
            WandOfBlastWave.throwChar(defender,
                    trajectory,
                    6,
                    !(weapon instanceof MissileWeapon || weapon instanceof SpiritBow),
                    true,
                    getClass());
        }
        return damage/3*2;
    }

    public static ItemSprite.Glowing GRAY = new ItemSprite.Glowing( 0x919999 );

    @Override
    public ItemSprite.Glowing glowing() {
        return GRAY;
    }
}
