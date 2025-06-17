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
import com.zrp200.rkpd2.items.Gold;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.levels.traps.DisintegrationTrap;
import com.zrp200.rkpd2.levels.traps.PoisonDartTrap;
import com.zrp200.rkpd2.levels.traps.RockfallTrap;
import com.zrp200.rkpd2.levels.traps.Trap;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

public class SecretHoardRoom extends SecretRoom {
	
	@Override
	public void paint(Level level) {
		Painter.fill(level, this, Terrain.WALL);
		Painter.fill(level, this, 1, Terrain.EMPTY);
		
		Class<? extends Trap> trapClass;
		if (Random.Int(2) == 0){
			trapClass = RockfallTrap.class;
		} else if (Dungeon.scalingDepth() >= 10){
			trapClass = DisintegrationTrap.class;
		} else {
			trapClass = PoisonDartTrap.class;
		}
		
		int goldPos;
		//half of the internal space of the room
		int totalGold = ((width()-2)*(height()-2))/2;
		Heap.Type type = Heap.Type.HEAP;
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.CHESTS))
			type = Heap.Type.CHEST;
		
		//no matter how much gold it drops, roughly equals 8 gold stacks.
		float goldRatio = 8 / (float)totalGold;
		for (int i = 0; i < totalGold; i++) {
			do {
				goldPos = level.pointToCell(random());
			} while (level.heaps.get(goldPos) != null);
			Item gold = new Gold().random();
			gold.quantity(Math.round(gold.quantity() * goldRatio));
			level.drop(gold, goldPos).type = type;
		}
		
		for (Point p : getPoints()){
			if (Random.Int(2) == 0 && level.map[level.pointToCell(p)] == Terrain.EMPTY){
				level.setTrap(Reflection.newInstance(trapClass).reveal(), level.pointToCell(p));
				Painter.set(level, p, Terrain.TRAP);
			}
		}
		
		entrance().set(Door.Type.HIDDEN);
	}
	
	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}
}
