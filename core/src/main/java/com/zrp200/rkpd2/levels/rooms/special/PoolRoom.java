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

package com.zrp200.rkpd2.levels.rooms.special;

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.mobs.Piranha;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.potions.PotionOfInvisibility;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.watabou.utils.Random;

public class PoolRoom extends SpecialRoom {

	private static final int NPIRANHAS	= 3;
	
	@Override
	public int minWidth() {
		return 6;
	}
	
	@Override
	public int minHeight() {
		return 6;
	}
	
	public void paint(Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.WATER );
		
		Door door = entrance();
		door.set( Door.Type.REGULAR );

		int x = -1;
		int y = -1;
		if (door.x == left) {
			
			x = right - 1;
			y = top + height() / 2;
			Painter.fill(level, left+1, top+1, 1, height()-2, Terrain.EMPTY_SP);
			
		} else if (door.x == right) {
			
			x = left + 1;
			y = top + height() / 2;
			Painter.fill(level, right-1, top+1, 1, height()-2, Terrain.EMPTY_SP);
			
		} else if (door.y == top) {
			
			x = left + width() / 2;
			y = bottom - 1;
			Painter.fill(level, left+1, top+1, width()-2, 1, Terrain.EMPTY_SP);
			
		} else if (door.y == bottom) {
			
			x = left + width() / 2;
			y = top + 1;
			Painter.fill(level, left+1, bottom-1, width()-2, 1, Terrain.EMPTY_SP);
			
		}
		
		int pos = x + y * level.width();
		level.drop( prize( level ), pos ).type = Heap.Type.CHEST;
		Painter.set( level, pos, Terrain.PEDESTAL );
		
		level.addItemToSpawn( new PotionOfInvisibility() );
		
		for (int i=0; i < NPIRANHAS; i++) {
			Piranha piranha = Piranha.random();
			do {
				piranha.pos = level.pointToCell(random());
			} while (level.map[piranha.pos] != Terrain.WATER|| level.findMob( piranha.pos ) != null);
			level.mobs.add( piranha );
		}
	}
	
	private static Item prize( Level level ) {

		Item prize;

		//33% chance for prize item
		if (Random.Int(3) == 0){
			prize = level.findPrizeItem();
			if (prize != null)
				return prize;
		}

		//1 floor set higher in probability, never cursed
		if (Random.Int(2) == 0) {
			prize = Generator.randomWeapon((Dungeon.scalingDepth() / 5) + 1);
			if (((Weapon)prize).hasCurseEnchant()){
				((Weapon) prize).enchant(null);
			}
		} else {
			prize = Generator.randomArmor((Dungeon.scalingDepth() / 5) + 1);
			if (((Armor)prize).hasCurseGlyph()){
				((Armor) prize).inscribe(null);
			}
		}
		prize.cursed = false;
		prize.cursedKnown = true;
		
		//33% chance for an extra update.
		if (Random.Int(3) == 0 && (!Dungeon.isChallenged(Challenges.REDUCED_POWER))){
			prize.upgrade();
		}

		return prize;
	}
}
