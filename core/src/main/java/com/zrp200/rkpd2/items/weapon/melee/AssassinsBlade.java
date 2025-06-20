/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.zrp200.rkpd2.items.weapon.melee;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Cripple;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class AssassinsBlade extends Dirk {

	{
		image = ItemSpriteSheet.ASSASSINS_BLADE;
		hitSoundPitch = 0.9f;

		tier = 4;
	}
		//20 base, down from 25
		//scaling unchanged

	@Override
	protected int maxDist() {
		return 3;
	}

    @Override
    public int warriorAttack(int damage, Char enemy) {
        if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(Dungeon.hero)){
            Buff.prolong(enemy, Blindness.class, 4f);
            Buff.prolong(enemy, Cripple.class, 4f);
        }
        return super.warriorAttack(damage, enemy);
    }
}