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

package com.zrp200.rkpd2.actors.blobs;

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.effects.BlobEmitter;
import com.zrp200.rkpd2.effects.particles.FlameParticle;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.scenes.GameScene;

public class Fire extends Blob {

	protected Class<? extends Burning> fireClass = Burning.class;

	@Override
	protected void evolve() {

		boolean[] flamable = Dungeon.level.flamable;
		int cell;
		int fire = 0;
		
		Freezing freeze = (Freezing)Dungeon.level.blobs.get( Freezing.class );

		boolean observe = false;

		for (int i = area.left-1; i <= area.right; i++) {
			for (int j = area.top-1; j <= area.bottom; j++) {
				cell = i + j * Dungeon.level.width();
				if (Dungeon.level.insideMap(cell)) {
					if (cur[cell] > 0) {

						if (freeze != null && freeze.volume > 0 && freeze.cur[cell] > 0) {
							freeze.clear(cell);
							off[cell] = cur[cell] = 0;
							continue;
						}

						burn(cell, fireClass);

						fire = cur[cell] - 1;

						if (Dungeon.isChallenged(Challenges.BURN)) {
							fire = cur[cell];
						}

						if (fire <= 0 && flamable[cell]) {

							Dungeon.level.destroy(cell);

							observe = true;
							GameScene.updateMap(cell);

						}

					} else if ((freeze == null || freeze.volume <= 0 || freeze.cur[cell] <= 0) && !Dungeon.isChallenged(Challenges.BURN)) {

						if (flamable[cell]
								&& (cur[cell - 1] > 0
								|| cur[cell + 1] > 0
								|| cur[cell - Dungeon.level.width()] > 0
								|| cur[cell + Dungeon.level.width()] > 0)) {
							fire = 4;
							burn(cell, fireClass);
							area.union(i, j);
						} else {
							fire = 0;
						}

					} else {
						if (!Dungeon.isChallenged(Challenges.BURN))
							fire = 0;
					}
					if (!Dungeon.isChallenged(Challenges.BURN))
						volume += (off[cell] = fire);
					else
						volume += 1;
				}
			}
		}

		if (observe) {
			Dungeon.observe();
		}
	}

	public static void burn (int pos){
		burn(pos, Burning.class);
	}
	
	public static void burn( int pos, Class<? extends Burning> fireClass) {
		Char ch = Actor.findChar( pos );
		if (ch != null && !ch.isImmune(fireClass)) {
			Buff.affect( ch, fireClass ).reignite( ch );
		}
		
		Heap heap = Dungeon.level.heaps.get( pos );
		if (heap != null) {
			heap.burn();
		}

		Plant plant = Dungeon.level.plants.get( pos );
		if (plant != null){
			plant.wither();
		}
	}
	
	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( FlameParticle.FACTORY, 0.03f );
	}
	
	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
