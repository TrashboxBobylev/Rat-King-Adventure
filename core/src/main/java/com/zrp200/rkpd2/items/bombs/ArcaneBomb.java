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

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.GooWarn;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.particles.ElmoParticle;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ArcaneBomb extends Bomb {
	
	{
		image = ItemSpriteSheet.ARCANE_BOMB;
	}
	
	@Override
	protected void onThrow(int cell) {
		super.onThrow(cell);
		if (fuse != null){
			PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 2 );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE)
					GameScene.add(Blob.seed(i, 3, GooWarn.class));
			}
		}
	}
	
	@Override
	public boolean explodesDestructively() {
		return false;
	}
	
	@Override
	public void explode(int cell) {
		super.explode(cell);
		
		ArrayList<Char> affected = new ArrayList<>();
		
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 2 );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				if (Dungeon.level.heroFOV[i]) {
					CellEmitter.get(i).burst(ElmoParticle.FACTORY, 10);
				}
				Char ch = Actor.findChar(i);
				if (ch != null){
					if (doNotDamageHero && ch instanceof Hero) {
						continue;
					}
					affected.add(ch);
				}
			}
		}
		
		for (Char ch : affected){
			// 100%/83%/67% bomb damage based on distance, but pierces armor.
			int damage = Math.round(Random.NormalIntRange( Dungeon.scalingDepth()+5, 10 + Dungeon.scalingDepth() * 2 ));
			float multiplier = 1f - (.16667f*Dungeon.level.distance(cell, ch.pos));
			ch.damage(Math.round(damage*multiplier), this);
			if (ch == Dungeon.hero && !ch.isAlive()){
				Badges.validateDeathFromFriendlyMagic();
				Dungeon.fail(this);
			}
		}
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (20 + 30);
	}
}
