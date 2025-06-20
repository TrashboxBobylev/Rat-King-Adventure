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

public class RingOfTenacity extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_TENACITY;
		buffClass = Tenacity.class;
	}

	@Override
	protected float multiplier() {
		return 0.85f;
	}

	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return formatBonus(level + 1) + "%";
	}

	@Override
	protected float cap() {
		return 0.66f;
	}

	@Override
	protected RingBuff buff( ) {
		return new Tenacity();
	}

	public static float damageMultiplier( Char t ){
		//(HT - HP)/HT = heroes current % missing health.
		float ten = Math.max(0.33f, (float) Math.pow(0.85, getBuffedBonus(t, Tenacity.class) * ((float) (t.HT - t.HP) / t.HT)));
		Hunger hunger = Dungeon.hero.buff(Hunger.class);
		if (hunger != null && hunger.accumulatingDamage > 0){
			ten /= Math.max(0.75f, 1f - (float)hunger.accumulatingDamage/t.HT/2);
		}
		return ten;
	}

	public class Tenacity extends RingBuff {
	}
}

