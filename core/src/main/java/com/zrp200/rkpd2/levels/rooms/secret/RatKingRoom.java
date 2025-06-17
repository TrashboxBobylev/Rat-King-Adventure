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
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.mobs.npcs.RatKing;
import com.zrp200.rkpd2.items.Gold;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.quest.NerfGun;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.levels.rooms.Room;
import com.zrp200.rkpd2.levels.rooms.sewerboss.SewerBossEntranceRoom;
import com.watabou.utils.Random;

public class RatKingRoom extends SecretRoom {
	
	@Override
	public boolean canConnect(Room r) {
		//never connects at the entrance
		return !(r instanceof SewerBossEntranceRoom) && super.canConnect(r);
	}
	
	//reduced max size to limit chest numbers.
	// normally would gen with 8-28, this limits it to 8-16
	@Override
	public int maxHeight() { return 7; }
	public int maxWidth() { return 7; }
	
	public void paint(Level level ) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );
		
		Door entrance = entrance();
		entrance.set( Door.Type.HIDDEN );
		int door = entrance.x + entrance.y * level.width();
		
		for (int i=left + 1; i < right; i++) {
			addChest( level, (top + 1) * level.width() + i, door );
			addChest( level, (bottom - 1) * level.width() + i, door );
		}
		
		for (int i=top + 2; i < bottom - 1; i++) {
			addChest( level, i * level.width() + left + 1, door );
			addChest( level, i * level.width() + right - 1, door );
		}

		int pos = level.pointToCell(random(2));
		if(Dungeon.hero.heroClass.isExact(HeroClass.RAT_KING)) {
			Painter.set(level,pos,Terrain.SIGN); // home sweet home sign, see hero#actMove
		} else {
			RatKing king = new RatKing();
			king.pos = pos;
			level.mobs.add(king);
		}
	}
	
	private static void addChest( Level level, int pos, int door ) {
		
		if (pos == door - 1 ||
			pos == door + 1 ||
			pos == door - level.width() ||
			pos == door + level.width()) {
			return;
		}
		
		Item prize = new Gold().random();
		if (Random.Int(10) == 0){
			prize = new NerfGun().random();
		}
		
		level.drop( prize, pos ).type = Heap.Type.CHEST;
	}
}
