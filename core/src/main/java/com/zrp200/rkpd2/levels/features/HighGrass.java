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

package com.zrp200.rkpd2.levels.features;

import static com.zrp200.rkpd2.utils.SafeCast.cast;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.mobs.ArmoredStatue;
import com.zrp200.rkpd2.actors.mobs.npcs.Blacksmith;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.items.Dewdrop;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.armor.glyphs.Camouflage;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.artifacts.SandalsOfNature;
import com.zrp200.rkpd2.items.food.Berry;
import com.zrp200.rkpd2.items.trinkets.PetrifiedSeed;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.MiningLevel;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.scenes.GameScene;
import com.watabou.utils.Random;

public class HighGrass {
	
	//prevents items dropped from grass, from trampling that same grass.
	//yes this is a bit ugly, oh well.
	private static boolean freezeTrample = false;

	public static boolean plant(int cell) {
		int t = Dungeon.level.map[cell];
		if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMBERS
				|| t == Terrain.GRASS || t == Terrain.FURROWED_GRASS)
				&& Dungeon.level.plants.get(cell) == null){
			Level.set(cell, Terrain.HIGH_GRASS);
			GameScene.updateMap(cell);
			return true;
		}
		return false;
	}

	public static void playVFX(int pos) {
		CellEmitter.get(pos).burst(LeafParticle.LEVEL_SPECIFIC, 4);
	}

	public static void trample( Level level, int pos ) {
		
		if (freezeTrample) return;
		
		Char ch = Actor.findChar(pos);
		boolean furrow = ch instanceof Hero && ((Hero)ch).heroClass.is(HeroClass.HUNTRESS);
		if (level.map[pos] == Terrain.FURROWED_GRASS){
			if (ch instanceof Hero && furrow){
				//Do nothing
				freezeTrample = true;
			} else {
				Level.set(pos, Terrain.GRASS);
			}
			
		} else {
			if (ch instanceof Hero && furrow){
				Level.set(pos, Terrain.FURROWED_GRASS);
				freezeTrample = true;
			} else {
				Level.set(pos, Terrain.GRASS);
			}
			
			int naturalismLevel = 0;
			
			if (ch != null) {
				SandalsOfNature.Naturalism naturalism = ch.buff( SandalsOfNature.Naturalism.class );
				if (naturalism != null) {
					if (!naturalism.isCursed()) {
						naturalismLevel = naturalism.itemLevel() + 1;
						naturalism.charge();
					} else {
						naturalismLevel = -1;
					}
				}

				//berries try to drop on floors 2/3/4/6/7/8, to a max of 4/6
				Hero hero = cast(ch, Hero.class);
				int totalBerries = hero == null
						// drawback of using shifted points is that I have to make this check
						|| !hero.hasTalent(Talent.NATURES_BOUNTY, Talent.ROYAL_PRIVILEGE)
						? 0 : (int)hero.byTalent(false, true,
													Talent.NATURES_BOUNTY , 3,
													Talent.ROYAL_PRIVILEGE, 2);
				if (totalBerries > 0){

					// this was removed in shattered but I still need it
					//pre-1.3.0 saves
					Talent.NatureBerriesAvailable oldAvailable = ch.buff(Talent.NatureBerriesAvailable.class);
					if (oldAvailable != null){
						Buff.affect(ch, Talent.NatureBerriesDropped.class).countUp(totalBerries - oldAvailable.count());
						oldAvailable.detach();
					}

					Talent.NatureBerriesDropped dropped = Buff.affect(ch, Talent.NatureBerriesDropped.class);
					// this is strictly equivalent to what shattered did.
					int droppedBerries = (int)dropped.count();

					if (totalBerries > droppedBerries) {
						int targetFloor = droppedBerries + (droppedBerries >= 5 ? 3 : 2);

						//If we're behind: 1/10, if we're on page: 1/30, if we're ahead: 1/90;
						int difference = Dungeon.depth - targetFloor;
						if (Random.Int(
								difference > 0
									? 10 :
								difference == 0
									? 30
									: 90
						) == 0) {
							dropped.countUp(1);
							level.drop(new Berry(), pos).sprite.drop();
						}
					}

				}
			}

			//grass gives 1/3 the normal amount of loot in fungi level
			if (Dungeon.level instanceof MiningLevel
					&& Blacksmith.Quest.Type() == Blacksmith.Quest.FUNGI
					&& Random.Int(3) != 0){
				naturalismLevel = -1;
			}
			
			if (naturalismLevel >= 0) {
				// TODO NERF
				// sigh.
				int points = Dungeon.hero.pointsInTalent(Talent.NATURES_BETTER_AID);
				// Seed, scales from 1/25 to 1/9
                // NBA increases by 17%/33%/50%
				float lootChance = (1+points/6f)/(25f - naturalismLevel*4f);

				// absolute max drop rate is ~1/6.5 with footwear of nature, ~1/18 without
				lootChance *= PetrifiedSeed.grassLootMultiplier();

				if (Random.Float() < lootChance) {
					if (Random.Float() < PetrifiedSeed.stoneInsteadOfSeedChance()) {
						level.drop(Generator.randomUsingDefaults(Generator.Category.STONE), pos).sprite.drop();
					} else {
						level.drop(Generator.random(Generator.Category.SEED), pos).sprite.drop();
					}
				}
				
				// Dew, scales from 1/6 to 1/4
                // NBA increases by 1/12 / 1/6 / 1/4
                // fixme this probably isn't right.
				lootChance = 3*(1 + .25f*(points/3f))/(24 - naturalismLevel*2);

				//grassy levels spawn half as much dew
				if (Dungeon.level != null && Dungeon.level.feeling == Level.Feeling.GRASS){
					lootChance /= 2;
				}

				if (Random.Float() < lootChance) {
					level.drop(new Dewdrop(), pos).sprite.drop();
				}
			}

			if (ch != null) {
				Camouflage.activate(ch, ch.glyphLevel(Camouflage.class));
			}
			
		}
		
		freezeTrample = false;
		
		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.updateMap(pos);
			
			playVFX(pos);
			if (Dungeon.level.heroFOV[pos]) Dungeon.observe();
		}
	}
}
