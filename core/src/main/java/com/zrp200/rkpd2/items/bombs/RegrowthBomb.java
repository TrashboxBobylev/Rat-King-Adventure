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

package com.zrp200.rkpd2.items.bombs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Regrowth;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.potions.PotionOfHealing;
import com.zrp200.rkpd2.items.wands.WandOfRegrowth;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.plants.Starflower;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class RegrowthBomb extends Bomb {
	
	{
		//TODO visuals
		image = ItemSpriteSheet.REGROWTH_BOMB;
	}
	
	@Override
	public boolean explodesDestructively() {
		return false;
	}

	@Override
	protected int explosionRange() {
		return 3;
	}

	@Override
	public void explode(int cell) {
		super.explode(cell);
		
		if (Dungeon.level.heroFOV[cell]) {
			Splash.at(cell, 0x00FF00, 30);
		}
		
		ArrayList<Integer> plantCandidates = new ArrayList<>();
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), explosionRange() );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Char ch = Actor.findChar(i);
				int t = Dungeon.level.map[i];
				if (ch != null){
					if (ch.alignment == Dungeon.hero.alignment) {
						//same as a healing potion
						PotionOfHealing.cure(ch);
						PotionOfHealing.heal(ch);
					}
				} else if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMBERS
						|| t == Terrain.GRASS || t == Terrain.FURROWED_GRASS || t == Terrain.HIGH_GRASS)
						&& Dungeon.level.plants.get(i) == null){
					plantCandidates.add(i);
				}
				GameScene.add( Blob.seed( i, 10, Regrowth.class ) );
			}
		}

		int plants = Random.chances(new float[]{0, 0, 2, 1});

		for (int i = 0; i < plants; i++) {
			Integer plantPos = Random.element(plantCandidates);
			if (plantPos != null) {
				Dungeon.level.plant((Plant.Seed) Generator.randomUsingDefaults(Generator.Category.SEED), plantPos);
				plantCandidates.remove(plantPos);
			}
		}
		
		Integer plantPos = Random.element(plantCandidates);
		if (plantPos != null){
			Plant.Seed plant;
			switch (Random.chances(new float[]{0, 6, 3, 1})){
				case 1: default:
					plant = new WandOfRegrowth.Dewcatcher.Seed();
					break;
				case 2:
					plant = new WandOfRegrowth.Seedpod.Seed();
					break;
				case 3:
					plant = new Starflower.Seed();
					break;
			}
			Dungeon.level.plant( plant, plantPos);
		}
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (20 + 30);
	}
}
