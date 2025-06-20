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

import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class RingOfHaste extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_HASTE;
		buffClass = Haste.class;
	}

	@Override
	protected float multiplier() {
		return 1.175f;
	}

	@Override
	public String upgradeStat1(int level) {
		return formatBonus(level + 1);
	}

	@Override
	protected float cap() {
		return 2f;
	}

	@Override
	protected RingBuff buff( ) {
		return new Haste();
	}
	
	public static float speedMultiplier( Char target ){
		return Math.min(3f, (float)Math.pow(1.175, getBuffedBonus(target, Haste.class)));
	}
	
	public class Haste extends RingBuff {
	}
}
