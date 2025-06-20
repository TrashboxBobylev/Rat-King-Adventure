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

public class RingOfEvasion extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_EVASION;
		buffClass = Evasion.class;
	}

	@Override
	protected float multiplier() {
		return 1.125f;
	}


	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return formatBonus(level + 1) + "%";
	}
	
	@Override
	protected float cap() {
		return 1.5f;
	}

	@Override
	protected RingBuff buff( ) {
		return new Evasion();
	}
	
	public static float evasionMultiplier( Char target ){
		float eva = Math.min(2.5f, (float) Math.pow(1.125, getBuffedBonus(target, Evasion.class)));
		Hunger hunger = Dungeon.hero.buff(Hunger.class);
		if (hunger != null && hunger.accumulatingDamage > 0){
			eva *= Math.max(0.5f, 1f - (float)hunger.accumulatingDamage/target.HT/2);
		}
		return eva;
	}

	public class Evasion extends RingBuff {
	}
}
