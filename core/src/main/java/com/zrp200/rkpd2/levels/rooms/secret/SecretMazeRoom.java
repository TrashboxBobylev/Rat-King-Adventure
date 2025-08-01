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

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.Maze;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class SecretMazeRoom extends SecretRoom {
	
	@Override
	public int minWidth() {
		return 14;
	}
	
	@Override
	public int minHeight() {
		return 14;
	}
	
	@Override
	public int maxWidth() {
		return 18;
	}
	
	@Override
	public int maxHeight() {
		return 18;
	}
	
	@Override
	public void paint(Level level) {
		Painter.fill(level, this, Terrain.WALL);
		Painter.fill(level, this, 1, Terrain.EMPTY);
		
		//true = space, false = wall
		Maze.allowDiagonals = false;
		boolean[][] maze = Maze.generate(this);
		boolean[] passable = new boolean[width()*height()];
		
		Painter.fill(level, this, 1, Terrain.EMPTY);
		for (int x = 0; x < maze.length; x++) {
			for (int y = 0; y < maze[0].length; y++) {
				if (maze[x][y] == Maze.FILLED) {
					Painter.fill(level, x + left, y + top, 1, 1, Terrain.WALL);
				}
				passable[x + width()*y] = maze[x][y] == Maze.EMPTY;
			}
		}
		
		PathFinder.setMapSize(width(), height());
		Point entrance = entrance();
		int entrancePos = (entrance.x - left) + width()*(entrance.y - top);
		
		PathFinder.buildDistanceMap( entrancePos, passable );
		
		int bestDist = 0;
		Point bestDistP = new Point();
		for (int i = 0; i < PathFinder.distance.length; i++){
			if (PathFinder.distance[i] != Integer.MAX_VALUE
					&& PathFinder.distance[i] > bestDist){
				bestDist = PathFinder.distance[i];
				bestDistP.x = (i % width()) + left;
				bestDistP.y = (i / width()) + top;
			}
		}
		
		Item prize;
		//1 floor set higher in probability, never cursed
		//1 floor set higher in probability, never cursed
		if (Random.Int(2) == 0) {
			prize = Generator.randomWeapon((Dungeon.scalingDepth() / 5) + 1, true);
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
		
		level.drop(prize, level.pointToCell(bestDistP)).type = Heap.Type.CHEST;
		
		PathFinder.setMapSize(level.width(), level.height());
		
		entrance().set(Door.Type.HIDDEN);
	}
}
