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
import com.zrp200.rkpd2.actors.blobs.Alchemy;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.items.EnergyCrystal;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.journal.AlchemyPage;
import com.zrp200.rkpd2.items.keys.IronKey;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.journal.Document;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.painters.Painter;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

public class LaboratoryRoom extends SpecialRoom {

	public void paint( Level level ) {
		
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );
		
		Door entrance = entrance();
		
		Point pot = null;
		if (entrance.x == left) {
			pot = new Point( right-1, Random.Int( 2 ) == 0 ? top + 1 : bottom - 1 );
		} else if (entrance.x == right) {
			pot = new Point( left+1, Random.Int( 2 ) == 0 ? top + 1 : bottom - 1 );
		} else if (entrance.y == top) {
			pot = new Point( Random.Int( 2 ) == 0 ? left + 1 : right - 1, bottom-1 );
		} else if (entrance.y == bottom) {
			pot = new Point( Random.Int( 2 ) == 0 ? left + 1 : right - 1, top+1 );
		}
		Painter.set( level, pot, Terrain.ALCHEMY );
		
		int chapter = 1 + Dungeon.depth/5;
		Blob.seed( pot.x + level.width() * pot.y, 1, Alchemy.class, level );

		Heap.Type type = Heap.Type.HEAP;
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.CHESTS))
			type = Heap.Type.CHEST;

		int pos;
		do {
			pos = level.pointToCell(random());
		} while (
				level.map[pos] != Terrain.EMPTY_SP ||
						level.heaps.get( pos ) != null);
		if (!Dungeon.isChallenged(Challenges.NO_ALCHEMY))
			level.drop( new EnergyCrystal().random(), pos ).type = type;
		else
			level.drop( new EnergyCrystal().quantity(1 + chapter*7 + Random.NormalIntRange(0, 8)), pos).type = type;

		int n = Random.NormalIntRange( 1, 2 );
		for (int i=0; i < n; i++) {
			do {
				pos = level.pointToCell(random());
			} while (
				level.map[pos] != Terrain.EMPTY_SP ||
				level.heaps.get( pos ) != null);
			level.drop( prize( level ), pos ).type = type;
		}
		
		//guide pages
		Collection<String> allPages = Document.ALCHEMY_GUIDE.pageNames();
		ArrayList<String> missingPages = new ArrayList<>();
		for ( String page : allPages){
			if (!Document.ALCHEMY_GUIDE.isPageFound(page)){
				missingPages.add(page);
			}
		}
		
		//5 pages in sewers, 10 in prison+
		int chapterTarget;
		if (missingPages.size() <= 5){
			chapterTarget = 2;
		} else {
			chapterTarget = 1;
		}
		
		if(!missingPages.isEmpty() && chapter >= chapterTarget){
			
			//for each chapter ahead of the target chapter, drop 1 additional page
			int pagesToDrop = Math.min(missingPages.size(), (chapter - chapterTarget) + 1);
			
			for (int i = 0; i < pagesToDrop; i++) {
				AlchemyPage p = new AlchemyPage();
				p.page(missingPages.remove(0));
				do {
					pos = level.pointToCell(random());
				} while (
						level.map[pos] != Terrain.EMPTY_SP ||
								level.heaps.get(pos) != null);
				level.drop(p, pos).type = type;
			}
		}

		entrance.set( Door.Type.LOCKED );
		level.addItemToSpawn( new IronKey(Dungeon.depth) );
		
	}
	
	private static Item prize( Level level ) {

		Item prize = level.findPrizeItem( Potion.class );
		if (prize == null)
			prize = Generator.random( Random.oneOf( Generator.Category.POTION, Generator.Category.STONE ));

		return prize;
	}
}
