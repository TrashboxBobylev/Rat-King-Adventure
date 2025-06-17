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

package com.zrp200.rkpd2.levels.rooms.quest;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.mobs.npcs.Blacksmith;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.LevelTransition;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.levels.rooms.standard.StandardRoom;
import com.zrp200.rkpd2.levels.traps.BurningTrap;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.tiles.CustomTilemap;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class BlacksmithRoom extends StandardRoom {
	
	@Override
	public int minWidth() {
		return Math.max(super.minWidth(), 6);
	}
	
	@Override
	public int minHeight() {
		return Math.max(super.minHeight(), 6);
	}
	
	public void paint(Level level ) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.TRAP );

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
			Painter.drawInside( level, this, door, 2, Terrain.EMPTY );
		}

		Painter.fill( level, this, 2, Terrain.EMPTY_SP );

		Heap.Type type = Heap.Type.HEAP;
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.CHESTS))
			type = Heap.Type.CHEST;

		for (int i=0; i < 2; i++) {
			int pos;
			do {
				pos = level.pointToCell(random());
			} while (level.map[pos] != Terrain.EMPTY_SP);
			level.drop(
				Generator.random( Random.oneOf(
					Generator.Category.ARMOR,
					Generator.Category.WEAPON,
					Generator.Category.MISSILE
				) ), pos ).type = type;
		}

		Blacksmith npc = new Blacksmith();
		do {
			npc.pos = level.pointToCell(random( 2 ));
		} while (level.heaps.get( npc.pos ) != null);
		level.mobs.add( npc );

		int entrancePos;
		do {
			entrancePos = level.pointToCell(random( 2 ));
		} while (level.heaps.get( npc.pos ) != null || entrancePos == npc.pos);

		QuestEntrance vis = new QuestEntrance();
		vis.pos(entrancePos, level);
		level.customTiles.add(vis);

		level.transitions.add(new LevelTransition(level,
				entrancePos,
				LevelTransition.Type.BRANCH_EXIT,
				Dungeon.depth,
				Dungeon.branch + 1,
				LevelTransition.Type.BRANCH_ENTRANCE));
		Painter.set(level, entrancePos, Terrain.EXIT);

		for(Point p : getPoints()) {
			int cell = level.pointToCell(p);
			if (level.map[cell] == Terrain.TRAP){
				level.setTrap(new BurningTrap().reveal(), cell);
			}
		}
	}

	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		if (l.map[l.pointToCell(p)] == Terrain.EXIT){
			return false;
		} else {
			return super.canPlaceCharacter(p, l);
		}
	}

	public static class QuestEntrance extends CustomTilemap {

		{
			texture = Assets.Environment.CAVES_QUEST;

			tileW = tileH = 1;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( new int[]{0}, 1 );
			return v;
		}

		@Override
		public String name(int tileX, int tileY) {
			return Messages.get(this, "name");
		}

		@Override
		public String desc(int tileX, int tileY) {
			return Messages.get(this, "desc");
		}

	}
}
