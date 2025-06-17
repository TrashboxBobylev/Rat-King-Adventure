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

package com.zrp200.rkpd2.levels.rooms.secret;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.potions.PotionOfLiquidFlame;
import com.zrp200.rkpd2.items.stones.StoneOfEnchantment;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.watabou.utils.Point;

public class SecretRunestoneRoom extends SecretRoom {
	
	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill(level, this, 1, Terrain.EMPTY);
		
		Door entrance = entrance();
		Point center = center();
		
		if (entrance.x == left || entrance.x == right){
			Painter.drawLine(level,
					new Point(center.x, top+1),
					new Point(center.x, bottom-1),
					Terrain.BOOKSHELF);
			if (entrance.x == left) {
				Painter.fill(level, center.x+1, top+1, right-center.x-1, height()-2, Terrain.EMPTY_SP);
			} else {
				Painter.fill(level, left+1, top+1, center.x-left-1, height()-2, Terrain.EMPTY_SP);
			}
		} else {
			Painter.drawLine(level,
					new Point(left+1, center.y),
					new Point(right-1, center.y),
					Terrain.BOOKSHELF);
			if (entrance.y == top) {
				Painter.fill(level, left+1, center.y+1, width()-2, bottom-center.y-1, Terrain.EMPTY_SP);
			} else {
				Painter.fill(level, left+1, top+1, width()-2, center.y-top-1, Terrain.EMPTY_SP);
			}
		}
		
		level.addItemToSpawn(new PotionOfLiquidFlame());

		Heap.Type type = Heap.Type.HEAP;
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.CHESTS))
			type = Heap.Type.CHEST;

		int dropPos;
		
		do{
			dropPos = level.pointToCell(random());
		} while (level.map[dropPos] != Terrain.EMPTY);
		level.drop( Generator.randomUsingDefaults(Generator.Category.STONE), dropPos).type = type;
		
		do{
			dropPos = level.pointToCell(random());
		} while (level.map[dropPos] != Terrain.EMPTY || level.heaps.get(dropPos) != null);
		level.drop( Generator.randomUsingDefaults(Generator.Category.STONE), dropPos).type = type;
		
		do{
			dropPos = level.pointToCell(random());
		} while (level.map[dropPos] != Terrain.EMPTY_SP);
		level.drop( new StoneOfEnchantment(), dropPos).type = type;
		
		entrance.set(Door.Type.HIDDEN);
	}
	
	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}
	
	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}
	
	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		return super.canPlaceCharacter(p, l) && l.map[l.pointToCell(p)] != Terrain.EMPTY_SP;
	}
}
