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

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class Dirk extends Dagger {

	{
		image = ItemSpriteSheet.DIRK;
		hitSoundPitch = 1f;

		tier = 2;
		bones = true;

		//12 base, down from 15
		//scaling unchanged

		//deals 67% toward max to max on surprise, instead of min to max.
		surpriseTowardMax = 0.67f;
	}

	@Override
	protected int maxDist() {
		return 4;
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		Buff.affect(enemy, Bleeding.class).set(damage);
		return 0;
	}

	@Override
	public float warriorDelay() {
		return 2f;
	}
}
