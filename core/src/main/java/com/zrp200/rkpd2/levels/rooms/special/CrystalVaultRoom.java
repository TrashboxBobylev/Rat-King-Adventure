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
import com.zrp200.rkpd2.actors.mobs.CrystalMimic;
import com.zrp200.rkpd2.actors.mobs.Mimic;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.keys.CrystalKey;
import com.zrp200.rkpd2.items.keys.IronKey;
import com.zrp200.rkpd2.items.trinkets.MimicTooth;
import com.zrp200.rkpd2.items.trinkets.RatSkull;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

public class CrystalVaultRoom extends SpecialRoom {

	//fixed size to improve presentation and provide space for crystal mimics
	@Override
	public int minHeight() { return 7; }
	public int maxHeight() { return 7; }
	public int minWidth() { return 7; }
	public int maxWidth() { return 7; }

	public void paint( Level level ) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );
		Painter.fill( level, this, 2, Terrain.EMPTY );
		
		int c = level.pointToCell(center());
		Random.shuffle(prizeClasses);
		
		Item i1, i2;
		i1 = prize();
		i2 = prize();

		int i1Pos, i2Pos;
		int doorPos = level.pointToCell(entrance());
		do {
			int neighbourIdx = Random.Int(PathFinder.CIRCLE8.length);
			i1Pos = c + PathFinder.CIRCLE8[neighbourIdx];
			i2Pos = c + PathFinder.CIRCLE8[(neighbourIdx+4)%8];
		} while (level.adjacent(i1Pos, doorPos) || level.adjacent(i2Pos, doorPos));

		level.drop( i1, i1Pos ).type = Heap.Type.CRYSTAL_CHEST;
		float altChance = 1/10f * RatSkull.exoticChanceMultiplier();
		if (altChance > 0.1f) altChance = (altChance+0.1f)/2f; //rat skull is 1/2 as effective here
		altChance *= MimicTooth.mimicChanceMultiplier(); //mimic tooth has full effectiveness
		if (Random.Float() < altChance){
			level.mobs.add(Mimic.spawnAt(i2Pos, CrystalMimic.class, i2));
		} else {
			level.drop(i2, i2Pos).type = Heap.Type.CRYSTAL_CHEST;
		}
		Painter.set(level, i1Pos, Terrain.PEDESTAL);
		Painter.set(level, i2Pos, Terrain.PEDESTAL);

		level.addItemToSpawn( new CrystalKey(Dungeon.depth) );
		
		entrance().set( Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey(Dungeon.depth) );
	}
	
	private Item prize() {
		Generator.Category cat = prizeClasses.remove(0);
		prizeClasses.add(cat);
		Item prize;
			do {
				prize = Generator.random(cat);
			} while (prize == null || Challenges.isItemBlocked(prize));
			return prize;
		}
	
	private ArrayList<Generator.Category> prizeClasses = new ArrayList<>(
			Arrays.asList(Generator.Category.WAND,
					Generator.Category.RING,
					Generator.Category.ARTIFACT));
}
