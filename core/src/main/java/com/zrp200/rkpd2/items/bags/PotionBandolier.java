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

package com.zrp200.rkpd2.items.bags;

import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.LiquidMetal;
import com.zrp200.rkpd2.items.Waterskin;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class PotionBandolier extends Bag {

	{
		image = ItemSpriteSheet.BANDOLIER;
	}

	@Override
	public boolean canHold( Item item ) {
		if (item instanceof Potion || item instanceof LiquidMetal || item instanceof Waterskin){
			return super.canHold(item);
		} else {
			return false;
		}
	}

	public int capacity(){
		return 19;
	}

	@Override
	public int value() {
		return 40;
	}

}
