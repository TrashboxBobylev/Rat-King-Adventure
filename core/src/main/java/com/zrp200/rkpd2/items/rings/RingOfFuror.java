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

package com.zrp200.rkpd2.items.rings;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class RingOfFuror extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_FUROR;
		buffClass = Furor.class;
	}

	@Override
	protected float multiplier() {
		return MULTIPLIER;
	}

	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return formatBonus(level + 1) + "%";
	}

	@Override
	protected RingBuff buff( ) {
		return new Furor();
	}

	private static final float MULTIPLIER = 1.09051f;

	@Override
	protected float cap() {
		return 1f;
	}

	public static float attackSpeedMultiplier(Char target ){
		float min = Math.min(2f, (float) Math.pow(MULTIPLIER, getBuffedBonus(target, Furor.class)));
		Hunger hunger = Dungeon.hero.buff(Hunger.class);
		if (hunger != null && hunger.accumulatingDamage > 0){
			min *= Math.max(0.5f, 1f - (float)hunger.accumulatingDamage/target.HT/2);
		}
		return min;
	}

	public class Furor extends RingBuff {
	}
}
