/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2020 Evan Debenham
 *
 * Experienced Pixel Dungeon
 * Copyright (C) 2019-2020 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.zrp200.rkpd2.items.wands;

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class WandOfUnstable extends Wand {

    {
        image = ItemSpriteSheet.WAND_UNSTABLE;
    }

    public static Class<?extends Wand>[] wands = new Class[]{
            WandOfBlastWave.class,
            WandOfCorrosion.class,
            WandOfCorruption.class,
            WandOfFrost.class,
            WandOfFirebolt.class,
            WandOfLightning.class,
            WandOfLivingEarth.class,
            WandOfDisintegration.class,
            WandOfMagicMissile.class,
            WandOfPrismaticLight.class,
            WandOfTransfusion.class,
            WandOfFireblast.class,
            WandOfRegrowth.class
    };

    private Wand wand;

    @Override
    public int collisionProperties(int target) {
        wand = Reflection.newInstance(Random.element(wands));
        if (wand != null) {
            return wand.collisionProperties(target);
        }
        return super.collisionProperties(target);
    }

    @Override
    public void onZap(Ballistica attack) {
        if (wand != null) {
            wand.onZap(attack);
            wand = null;
        }
    }

    @Override
    public void fx(Ballistica bolt, Callback callback) {
        if (wand != null) {
            wand.level(level());
            wand.fx(bolt, callback);
        }
    }

    @Override
    public void onHit(Weapon staff, Char attacker, Char defender, int damage) {
        wand = Reflection.newInstance(Random.element(wands));
        if (wand != null) {
            wand.level(level());
            wand.onHit(staff, attacker, defender, damage);
        }
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color(0x8ff161 );
        particle.am = 0.5f;
        particle.setLifespan(2f);
        particle.speed.set(Random.Int(-4, 4), Random.Int(-4, 4));
        particle.setSize( 1f, 3f);
    }
}
