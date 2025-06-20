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

package com.zrp200.rkpd2.actors.mobs.npcs;

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AscensionChallenge;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.mobs.Golem;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Monk;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.quest.DwarfToken;
import com.zrp200.rkpd2.items.rings.Ring;
import com.zrp200.rkpd2.journal.Notes;
import com.zrp200.rkpd2.levels.CityLevel;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ImpSprite;
import com.zrp200.rkpd2.windows.WndImp;
import com.zrp200.rkpd2.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Imp extends NPC {

	{
		spriteClass = ImpSprite.class;

		properties.add(Property.IMMOVABLE);
	}
	
	private boolean seenBefore = false;

	@Override
	public Notes.Landmark landmark() {
		return Notes.Landmark.IMP;
	}

	@Override
	protected boolean act() {
		if (Dungeon.hero.buff(AscensionChallenge.class) != null){
			die(null);
			return true;
		}
		if (!Quest.given && Dungeon.level.visited[pos]) {
			if (!seenBefore && Dungeon.level.heroFOV[pos]) {
				yell(Messages.get(this, "hey", Messages.titleCase(Dungeon.hero.name())));
				seenBefore = true;
			}
		} else {
			seenBefore = false;
		}
		
		return super.act();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//do nothing
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	@Override
	public boolean interact(Char c) {
		
		sprite.turnTo( pos, Dungeon.hero.pos );

		if (c != Dungeon.hero){
			return true;
		}

		if (Quest.given) {
			
			DwarfToken tokens = Dungeon.hero.belongings.getItem( DwarfToken.class );
			if (tokens != null && (tokens.quantity() >= 3)) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndImp( Imp.this, tokens ) );
					}
				});
			} else {
				tell( Quest.alternative ?
						Messages.get(this, "monks_2", Messages.titleCase(Dungeon.hero.name()))
						: Messages.get(this, "golems_2", Messages.titleCase(Dungeon.hero.name())) );
			}
			
		} else {
			tell( Messages.get(this, "greeting")+"\n"+ Messages.get(this,
                    Quest.alternative ? "monks_1"
                            : "golems_1",
                    Messages.titleCase(Dungeon.hero.name())));
			Quest.given = true;
			Quest.completed = false;
		}

		return true;
	}
	
	private void tell( String text ) {
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndQuest( Imp.this, text ));
			}
		});
	}
	
	public void flee() {
		
		yell( Messages.get(this, "cya", Messages.titleCase(Dungeon.hero.name())) );
		
		destroy();
		sprite.die();
	}

	public static class Quest {
		
		private static boolean alternative;
		
		private static boolean spawned;
		private static boolean given;
		private static boolean completed;
		
		public static Ring reward;
		
		public static void reset() {
			spawned = false;
			given = false;
			completed = false;

			reward = null;
		}
		
		private static final String NODE		= "demon";
		
		private static final String ALTERNATIVE	= "alternative";
		private static final String SPAWNED		= "spawned";
		private static final String GIVEN		= "given";
		private static final String COMPLETED	= "completed";
		private static final String REWARD		= "reward";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				node.put( ALTERNATIVE, alternative );
				
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REWARD, reward );
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				alternative	= node.getBoolean( ALTERNATIVE );
				
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reward = (Ring)node.get( REWARD );
			}
		}
		
		public static void spawn( CityLevel level ) {
			if (!spawned && Dungeon.depth > 16 && Random.Int( 20 - Dungeon.depth) == 0) {
				Imp npc = new Imp();
				int tries = 30;
				do {
					npc.pos = level.randomRespawnCell( npc );
					tries--;
				} while (
						npc.pos == -1 ||
						//visibility issues on these tiles, try to avoid them
						(tries > 0 && level.map[ npc.pos ] == Terrain.EMPTY_SP) ||
						level.heaps.get( npc.pos ) != null ||
						level.traps.get( npc.pos) != null ||
						level.findMob( npc.pos ) != null ||
						//don't place the imp against solid terrain
						!level.passable[npc.pos + PathFinder.CIRCLE4[0]] || !level.passable[npc.pos + PathFinder.CIRCLE4[1]] ||
						!level.passable[npc.pos + PathFinder.CIRCLE4[2]] || !level.passable[npc.pos + PathFinder.CIRCLE4[3]]);
				level.mobs.add( npc );
				
				spawned = true;

				//imp always spawns on an empty tile, for better visibility
				Level.set( npc.pos, Terrain.EMPTY, level);

				//always assigns monks on floor 17, golems on floor 19, and 50/50 between either on 18
				switch (Dungeon.depth){
					case 17: default:
						alternative = true;
						break;
					case 18:
						alternative = Random.Int(2) == 0;
						break;
					case 19:
						alternative = false;
						break;
				}
				
				given = false;
				
				do {
					reward = (Ring)Generator.random( Generator.Category.RING );
				} while (reward.cursed);
				if (!Dungeon.isChallenged(Challenges.REDUCED_POWER))
					reward.upgrade( 2 );
				reward.cursed = true;
			}
		}
		
		public static void process( Mob mob ) {
			if (spawned && given && !completed && Dungeon.depth != 20) {
				if ((alternative && mob instanceof Monk) ||
					(!alternative && mob instanceof Golem)) {
					
					Dungeon.level.drop( new DwarfToken(), mob.pos ).sprite.drop();
				}
			}
		}
		
		public static void complete() {
			reward = null;
			completed = true;

			Statistics.questScores[3] = 4000;
			Notes.remove( Notes.Landmark.IMP );
		}
		
		public static boolean isCompleted() {
			return completed;
		}
	}
}
