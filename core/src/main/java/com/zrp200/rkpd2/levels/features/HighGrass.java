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

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
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
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.MiningLevel;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.scenes.GameScene;
import com.watabou.utils.Random;

import static com.zrp200.rkpd2.utils.SafeCast.cast;

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
				if (ch instanceof Hero && MonkEnergy.isFeelingEmpowered(Level.Feeling.GRASS)){
					Level.set(pos, Terrain.FURROWED_GRASS);
					freezeTrample = true;
				} else {
					Level.set(pos, Terrain.GRASS);
				}
			}
			
			int naturalismLevel = 0;
			
			if (ch != null) {
				SandalsOfNature.NaturalismBuff naturalism = ch.buff( SandalsOfNature.NaturalismBuff.class );
				if (naturalism != null) {
					if (!naturalism.isCursed()) {
						naturalismLevel = naturalism.natureLevel() + 1;
						naturalism.charge(1);
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
				if (Random.Float() < (1+points/6f)/(25 - (naturalismLevel * 4))) {
					level.drop(Generator.random(Generator.Category.SEED), pos).sprite.drop();
				}
				
				// Dew, scales from 1/6 to 1/4
				// NBA increases by 1/12 / 1/6 / 1/4
				// todo this probably isn't right.
				if (Random.Float(24 - naturalismLevel*2) <= 3*(1 + .25*(points/3f))) {
					level.drop(new Dewdrop(), pos).sprite.drop();
				}
			}

			//Camouflage
			if (ch instanceof Hero) {
				Hero hero = (Hero) ch;
				if (hero.belongings.armor() != null && hero.belongings.armor().hasGlyph(Camouflage.class, hero)) {
					Camouflage.activate(hero, hero.belongings.armor.buffedLvl());
				}
			} else if (ch instanceof DriedRose.GhostHero){
				DriedRose.GhostHero ghost = (DriedRose.GhostHero) ch;
				if (ghost.armor() != null && ghost.armor().hasGlyph(Camouflage.class, ghost)){
					Camouflage.activate(ghost, ghost.armor().buffedLvl());
				}
			} else if (ch instanceof ArmoredStatue){
				ArmoredStatue statue = (ArmoredStatue) ch;
				if (statue.armor() != null && statue.armor().hasGlyph(Camouflage.class, statue)){
					Camouflage.activate(statue, statue.armor().buffedLvl());
				}
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
