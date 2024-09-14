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

package com.zrp200.rkpd2.actors.mobs;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Amok;
import com.zrp200.rkpd2.actors.buffs.AscensionChallenge;
import com.zrp200.rkpd2.actors.buffs.Barkskin;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Charm;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.buffs.Degrade;
import com.zrp200.rkpd2.actors.buffs.Dread;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.FrostBurn;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.MindVision;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.buffs.Preparation;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.buffs.Sleep;
import com.zrp200.rkpd2.actors.buffs.SoulMark;
import com.zrp200.rkpd2.actors.buffs.StuckBuff;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.buffs.WarpedEnemy;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.duelist.Feint;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.NaturesPower;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.SpiritHawk;
import com.zrp200.rkpd2.actors.mobs.npcs.DirectableAlly;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.Surprise;
import com.zrp200.rkpd2.effects.Wound;
import com.zrp200.rkpd2.effects.particles.ElmoParticle;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Gold;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.MasterThievesArmband;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.rings.Ring;
import com.zrp200.rkpd2.items.rings.RingOfWealth;
import com.zrp200.rkpd2.items.spells.CurseInfusion;
import com.zrp200.rkpd2.items.spells.ReclaimTrap;
import com.zrp200.rkpd2.items.spells.SummonElemental;
import com.zrp200.rkpd2.items.stones.StoneOfAggression;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.enchantments.Lucky;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.darts.Dart;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.features.Chasm;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Swiftthistle;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.utils.FunctionalStuff;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import static com.zrp200.rkpd2.Dungeon.hero;

public abstract class Mob extends Char {

	public float scaleFactor = 1f;

	{
		actPriority = MOB_PRIO;
		
		alignment = Alignment.ENEMY;
		if (Dungeon.isChallenged(Challenges.BURN)){
			resistances.add(Burning.class);
			resistances.add(FrostBurn.class);
		}
	}

	public AiState SLEEPING     = new Sleeping();
	public AiState HUNTING		= new Hunting();
	public AiState WANDERING	= new Wandering();
	public AiState FLEEING		= new Fleeing();
	public AiState PASSIVE		= new Passive();
	public AiState state = SLEEPING;
	
	public Class<? extends CharSprite> spriteClass;
	
	protected int target = -1;
	
	public int defenseSkill = 0;
	
	public int EXP = 1;
	public int maxLvl = 100000;

	public Char enemy;
	protected int enemyID = -1; //used for save/restore
	protected boolean enemySeen;
	protected boolean alerted = false;

	protected static final float TIME_TO_WAKE_UP = 1f;

	protected boolean firstAdded = true;
	protected void onAdd(){
		if (firstAdded) {
			//modify health for ascension challenge if applicable, only on first add
			float percent = HP / (float) HT;
			HT = Math.round(HT * AscensionChallenge.statModifier(this));
			HP = Math.round(HT * percent);
			firstAdded = false;
		}
	}

	private static final String STATE	= "state";
	private static final String SEEN	= "seen";
	private static final String TARGET	= "target";
	private static final String MAX_LVL	= "max_lvl";

	private static final String ENEMY_ID	= "enemy_id";

	private static final String SCALE   = "scale";

	@Override
	public void storeInBundle( Bundle bundle ) {
		
		super.storeInBundle( bundle );

		if (state == SLEEPING) {
			bundle.put( STATE, Sleeping.TAG );
		} else if (state == WANDERING) {
			bundle.put( STATE, Wandering.TAG );
		} else if (state == HUNTING) {
			bundle.put( STATE, Hunting.TAG );
		} else if (state == FLEEING) {
			bundle.put( STATE, Fleeing.TAG );
		} else if (state == PASSIVE) {
			bundle.put( STATE, Passive.TAG );
		}
		bundle.put( SEEN, enemySeen );
		bundle.put( TARGET, target );
		bundle.put( MAX_LVL, maxLvl );

		if (enemy != null) {
			bundle.put(ENEMY_ID, enemy.id() );
		}
		bundle.put( SCALE, scaleFactor);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		
		super.restoreFromBundle( bundle );

		String state = bundle.getString( STATE );
		if (state.equals( Sleeping.TAG )) {
			this.state = SLEEPING;
		} else if (state.equals( Wandering.TAG )) {
			this.state = WANDERING;
		} else if (state.equals( Hunting.TAG )) {
			this.state = HUNTING;
		} else if (state.equals( Fleeing.TAG )) {
			this.state = FLEEING;
		} else if (state.equals( Passive.TAG )) {
			this.state = PASSIVE;
		}

		enemySeen = bundle.getBoolean( SEEN );

		target = bundle.getInt( TARGET );

		if (bundle.contains(SCALE)) scaleFactor = bundle.getFloat( SCALE);

		if (bundle.contains(MAX_LVL)) maxLvl = bundle.getInt(MAX_LVL);

		if (bundle.contains(ENEMY_ID)) {
			enemyID = bundle.getInt(ENEMY_ID);
		}

		//no need to actually save this, must be false
		firstAdded = false;
	}

	//mobs need to remember their targets after every actor is added
	public void restoreEnemy(){
		if (enemyID != -1 && enemy == null) enemy = (Char)Actor.findById(enemyID);
	}
	
	public CharSprite sprite() {
		return Reflection.newInstance(spriteClass);
	}

	boolean updateAlert() {
		boolean justAlerted = alerted;
		alerted = false;
		if (justAlerted){
			sprite.showAlert();
		} else {
			sprite.hideAlert();
			sprite.hideLost();
		}
		return justAlerted;
	}

	@Override
	protected boolean act() {

		super.act();

		if (Dungeon.isChallenged(Challenges.RANDOM_HP) && scaleFactor == 1f &&
				!properties().contains(Property.BOSS) && !properties().contains(Property.MINIBOSS) &&
				!(this instanceof Swarm || this instanceof DarkSlime)){
			scaleFactor = Random.Float(0.5f, 1.75f);
			HP = HT = (int) (HT * scaleFactor);
			if (scaleFactor >= 1.25f){
				HP = HT = (int) (HT * 1.25f);
			}
			if (HT <= 1){
				HP = HT = 1;
			}
			sprite.linkVisuals(this);
		}

		if (hero.hasTalent(Talent.LASER_PRECISION)){
			PathFinder.buildDistanceMap( pos, BArray.not( Dungeon.level.solid, null ), 2 );
			Char ch;
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE && (ch = Actor.findChar(i)) != null && Random.Int(5) == 0) {
					HashSet<Buff> debuffs = FunctionalStuff.extract(buffs(), (buff) -> buff.type == Buff.buffType.NEGATIVE);
					if (!debuffs.isEmpty() && ch.alignment == alignment && ch != hero && ch != this){
						Buff debuff = Random.element(debuffs);
						if (ch.buff(debuff.getClass()) == null) {
							if (debuff instanceof FlavourBuff) {
								Buff.affect(ch, ((FlavourBuff) debuff).getClass(), debuff.cooldown());
							} else Buff.affect(ch, debuff.getClass());
							CellEmitter.get(i).burst(ElmoParticle.FACTORY, 12);
						}
					}
				}
			}
		}

		boolean justAlerted = updateAlert();

		if (paralysed > 0) {
			enemySeen = false;
			spend( TICK );
			return true;
		}

		if (buff(Terror.class) != null || buff(Dread.class) != null ){
			state = FLEEING;
		}
		
		enemy = chooseEnemy();
		
		boolean enemyInFOV = enemy != null && enemy.isAlive() && fieldOfView[enemy.pos] && enemy.invisible <= 0;

		//prevents action, but still updates enemy seen status
		if (buff(Feint.AfterImage.FeintConfusion.class) != null){
			enemySeen = enemyInFOV;
			spend( TICK );
			return true;
		}

		return state.act( enemyInFOV, justAlerted );
	}
	
	//FIXME this is sort of a band-aid correction for allies needing more intelligent behaviour
	protected boolean intelligentAlly = false;
	
	protected Char chooseEnemy() {

		Dread dread = buff( Dread.class );
		if (dread != null) {
			Char source = (Char)Actor.findById( dread.object );
			if (source != null) {
				return source;
			}
		}

		Terror terror = buff( Terror.class );
		if (terror != null) {
			Char source = (Char)Actor.findById( terror.object );
			if (source != null) {
				return source;
			}
		}
		
		//if we are an alert enemy, auto-hunt a target that is affected by aggression, even another enemy
		if ((alignment == Alignment.ENEMY || buff(Amok.class) != null ) && state != PASSIVE && state != SLEEPING) {
			if (enemy != null && enemy.buff(StoneOfAggression.Aggression.class) != null){
				state = HUNTING;
				return enemy;
			}
			for (Char ch : Actor.chars()) {
				if (ch != this && canSee(ch.pos) &&
						ch.buff(StoneOfAggression.Aggression.class) != null) {
					state = HUNTING;
					return ch;
				}
			}
		}

		//find a new enemy if..
		boolean newEnemy = false;
		//we have no enemy, or the current one is dead/missing
		if ( enemy == null || !enemy.isAlive() || !Actor.chars().contains(enemy) || state == WANDERING) {
			newEnemy = true;
		//We are amoked and current enemy is the hero
		} else if (buff( Amok.class ) != null && enemy == hero) {
			newEnemy = true;
		//We are charmed and current enemy is what charmed us
		} else if (buff(Charm.class) != null && buff(Charm.class).object == enemy.id()) {
			newEnemy = true;
		}

		//additionally, if we are an ally, find a new enemy if...
		if (!newEnemy && alignment == Alignment.ALLY){
			//current enemy is also an ally
			if (enemy.alignment == Alignment.ALLY){
				newEnemy = true;
			//current enemy is invulnerable
			} else if (enemy.isInvulnerable(getClass())){
				newEnemy = true;
			}
		}

		if ( newEnemy ) {

			HashSet<Char> enemies = new HashSet<>();
			Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);

			//if we are amoked...
			if ( buff(Amok.class) != null) {

				//try to find an enemy mob to attack first.
				for (Mob mob : mobs)
					if (mob.alignment == Alignment.ENEMY && mob != this
							&& canSee(mob.pos) && mob.invisible <= 0) {
						enemies.add(mob);
					}
				
				if (enemies.isEmpty()) {
					//try to find ally mobs to attack second.
					for (Mob mob : mobs)
						if (mob.alignment == Alignment.ALLY && mob != this
								&& canSee(mob.pos) && mob.invisible <= 0) {
							enemies.add(mob);
						}
					
					if (enemies.isEmpty()) {
						//try to find the hero third
						if (fieldOfView[hero.pos] && hero.invisible <= 0) {
							enemies.add(hero);
						}
					}
				}
				
			//if we are an ally...
			} else if ( alignment == Alignment.ALLY ) {
				//look for hostile mobs to attack
				for (Mob mob : mobs)
					if (mob.alignment == Alignment.ENEMY && canSee(mob.pos)
							&& mob.invisible <= 0 && !mob.isInvulnerable(getClass()))
						//do not target passive mobs
						//intelligent allies also don't target mobs which are wandering or asleep
						if (mob.state != mob.PASSIVE &&
								(!intelligentAlly || (mob.state != mob.SLEEPING && mob.state != mob.WANDERING))) {
							enemies.add(mob);
						}
				
			//if we are an enemy...
			} else if (alignment == Alignment.ENEMY) {
				//look for ally mobs to attack
				for (Mob mob : mobs)
					if (mob.alignment == Alignment.ALLY && canSee(mob.pos) && mob.invisible <= 0)
						enemies.add(mob);

				//and look for the hero
				if (canSee(hero.pos) && hero.invisible <= 0) {
					enemies.add(hero);
				}
				
			}

			//do not target anything that's charming us
			Charm charm = buff( Charm.class );
			if (charm != null){
				Char source = (Char)Actor.findById( charm.object );
				if (source != null && enemies.contains(source) && enemies.size() > 1){
					enemies.remove(source);
				}
			}

			//neutral characters in particular do not choose enemies.
			if (enemies.isEmpty()){
				return null;
			} else {
				//go after the closest potential enemy, preferring enemies that can be reached/attacked, and the hero if two are equidistant
				PathFinder.buildDistanceMap(pos, Dungeon.findPassable(this, Dungeon.level.passable, fieldOfView, true));
				Char closest = null;

				for (Char curr : enemies){
					if (closest == null){
						closest = curr;
					} else if (canAttack(closest) && !canAttack(curr)){
						continue;
					} else if ((canAttack(curr) && !canAttack(closest))
							|| (PathFinder.distance[curr.pos] < PathFinder.distance[closest.pos])){
						closest = curr;
					} else if ( curr == Dungeon.hero &&
							(PathFinder.distance[curr.pos] == PathFinder.distance[closest.pos]) || (canAttack(curr) && canAttack(closest))){
						closest = curr;
					}
				}
				//if we were going to target the hero, but an afterimage exists, target that instead
				if (closest == Dungeon.hero){
					for (Char ch : enemies){
						if (ch instanceof Feint.AfterImage){
							closest = ch;
							break;
						}
					}
				}

				return closest;
			}

		} else
			return enemy;
	}
	
	@Override
	public boolean add( Buff buff ) {
		if (super.add( buff )) {
			if (buff instanceof Amok || buff instanceof AllyBuff) {
				state = HUNTING;
			} else if (buff instanceof Terror || buff instanceof Dread) {
				state = FLEEING;
			} else if (buff instanceof Sleep) {
				state = SLEEPING;
				postpone(Sleep.SWS);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean remove( Buff buff ) {
		if (super.remove( buff )) {
			if ((buff instanceof Terror && buff(Dread.class) == null)
					|| (buff instanceof Dread && buff(Terror.class) == null)) {
				if (enemySeen) {
					sprite.showStatus(CharSprite.WARNING, Messages.get(this, "rage"));
					state = HUNTING;
				} else {
					state = WANDERING;
				}
			}
			return true;
		}
		return false;
	}
	
	public boolean canAttack( Char enemy ) {
		for (ChampionEnemy buff : buffs(ChampionEnemy.class)){
			if (buff.canAttackWithExtraReach( enemy )){
				return true;
			}
			if (buff.getClass() == ChampionEnemy.Paladin.class){
				return false;
			}
		}
		return super.canAttack(enemy);
	}

	protected boolean getCloser( int target ) {
		
		if (rooted || target == pos || buff(StuckBuff.class) != null) {
			return false;
		}

		if (buff(ChampionEnemy.Paladin.class) != null){
			return false;
		}

		int step = -1;

		if (buff(WarpedEnemy.class) != null){
			path = Dungeon.findPath( this, target,
					Dungeon.level.passable,
					fieldOfView, true );
			if (path != null)
				step = path.removeFirst();
			else
				return false;
		} else if (Dungeon.level.adjacent( pos, target )) {

			path = null;

			if (cellIsPathable(target)) {
				step = target;
			}

		} else {

			boolean newPath = false;
			//scrap the current path if it's empty, no longer connects to the current location
			//or if it's extremely inefficient and checking again may result in a much better path
			if (path == null || path.isEmpty()
					|| !Dungeon.level.adjacent(pos, path.getFirst())
					|| path.size() > 2*Dungeon.level.distance(pos, target))
				newPath = true;
			else if (path.getLast() != target) {
				//if the new target is adjacent to the end of the path, adjust for that
				//rather than scrapping the whole path.
				if (Dungeon.level.adjacent(target, path.getLast())) {
					int last = path.removeLast();

					if (path.isEmpty()) {

						//shorten for a closer one
						if (Dungeon.level.adjacent(target, pos)) {
							path.add(target);
						//extend the path for a further target
						} else {
							path.add(last);
							path.add(target);
						}

					} else {
						//if the new target is simply 1 earlier in the path shorten the path
						if (path.getLast() == target) {

						//if the new target is closer/same, need to modify end of path
						} else if (Dungeon.level.adjacent(target, path.getLast())) {
							path.add(target);

						//if the new target is further away, need to extend the path
						} else {
							path.add(last);
							path.add(target);
						}
					}

				} else {
					newPath = true;
				}

			}

			//checks if the next cell along the current path can be stepped into
			if (!newPath) {
				int nextCell = path.removeFirst();
				if (!cellIsPathable(nextCell)) {

					newPath = true;
					//If the next cell on the path can't be moved into, see if there is another cell that could replace it
					if (!path.isEmpty()) {
						for (int i : PathFinder.NEIGHBOURS8) {
							if (Dungeon.level.adjacent(pos, nextCell + i) && Dungeon.level.adjacent(nextCell + i, path.getFirst())) {
								if (cellIsPathable(nextCell+i)){
									path.addFirst(nextCell+i);
									newPath = false;
									break;
								}
							}
						}
					}
				} else {
					path.addFirst(nextCell);
				}
			}

			//generate a new path
			if (newPath) {
				//If we aren't hunting, always take a full path
				PathFinder.Path full = Dungeon.findPath(this, target, Dungeon.level.passable, fieldOfView, true);
				if (state != HUNTING){
					path = full;
				} else {
					//otherwise, check if other characters are forcing us to take a very slow route
					// and don't try to go around them yet in response, basically assume their blockage is temporary
					PathFinder.Path ignoreChars = Dungeon.findPath(this, target, Dungeon.level.passable, fieldOfView, false);
					if (ignoreChars != null && (full == null || full.size() > 2*ignoreChars.size())){
						//check if first cell of shorter path is valid. If it is, use new shorter path. Otherwise do nothing and wait.
						path = ignoreChars;
						if (!cellIsPathable(ignoreChars.getFirst())) {
							return false;
						}
					} else {
						path = full;
					}
				}
			}

			if (path != null) {
				step = path.removeFirst();
			} else {
				return false;
			}
		}
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}
	
	protected boolean getFurther( int target ) {
		if (rooted || target == pos|| buff(StuckBuff.class) != null) {
			return false;
		}
		
		int step = Dungeon.flee( this, target, Dungeon.level.passable, fieldOfView, true );
		if (step != -1) {
			move( step );
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();
		if (hero.buff(TimekeepersHourglass.timeFreeze.class) != null
				|| hero.buff(Swiftthistle.TimeBubble.class) != null)
			sprite.add( CharSprite.State.PARALYSED );
	}

	protected boolean doAttack( Char enemy ) {
		
		if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
			sprite.attack( enemy.pos );
			return false;
			
		} else {
			attack( enemy );
			Invisibility.dispel(this);
			spend( attackDelay() );
			return true;
		}
	}
	
	@Override
	public void onAttackComplete() {
		attack( enemy );
		Invisibility.dispel(this);
		spend( attackDelay() );
		super.onAttackComplete();
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		if ( !surprisedBy(enemy)
				&& paralysed == 0
				&& !(alignment == Alignment.ALLY && enemy == hero)) {
			return (int) (this.defenseSkill/((Dungeon.isChallenged(Challenges.RANDOM_HP) && scaleFactor != 1) ? (0.8f * scaleFactor) : 1));
		} else {
			return 0;
		}
	}

	@Override
	public int defenseProc( Char enemy, int damage ) {
		
		if (enemy instanceof Hero
				&& ((Hero) enemy).belongings.attackingWeapon() instanceof MissileWeapon){
			Statistics.thrownAttacks++;
			Badges.validateHuntressUnlock();
		}
		
		if (surprisedBy(enemy)) {
			Statistics.sneakAttacks++;
			Badges.validateRogueUnlock();
			//TODO this is somewhat messy, it would be nicer to not have to manually handle delays here
			// playing the strong hit sound might work best as another property of weapon?
			if (hero.belongings.attackingWeapon() instanceof SpiritBow.SpiritArrow
				|| hero.belongings.attackingWeapon() instanceof Dart){
				Sample.INSTANCE.playDelayed(Assets.Sounds.HIT_STRONG, 0.125f);
			} else {
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
			}
			if (enemy.buff(Preparation.class) != null) {
				Wound.hit(this);
			} else {
				Surprise.hit(this);
			}
		}

		//if attacked by something else than current target, and that thing is closer, switch targets
		if (this.enemy == null
				|| (enemy != this.enemy && (Dungeon.level.distance(pos, enemy.pos) < Dungeon.level.distance(pos, this.enemy.pos)))) {
			aggro(enemy);
			target = enemy.pos;
		}

		SoulMark soulMark = buff(SoulMark.class);
		if(soulMark != null) soulMark.proc(enemy,this,damage);
		/*{
				if (Dungeon.hero.HP < Dungeon.hero.HT) {
					int heal = (int)Math.ceil(restoration * 0.4f);
					Dungeon.hero.HP = Math.min(Dungeon.hero.HT, Dungeon.hero.HP + heal);
					Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
				}
		}
		*/

		if (buff(ChampionEnemy.Reflective.class) != null){
			enemy.damage((int) (damage*0.5f), this);
		}
		if (buff(WarpedEnemy.class) != null){
			for (int i : PathFinder.NEIGHBOURS8){
				int pos = this.pos + i;
				CellEmitter.center(pos).burst(Speck.factory(Speck.WARPCLOUD), 6);
				Char ch = Actor.findChar(pos);
				if (ch != null){
					ch.damage(damage / 3, new Warp());
					Buff.affect(ch, Degrade.class, 8);
				}
			}
		}

		return super.defenseProc(enemy, damage);
	}

	@Override
	public float speed() {
		return super.speed() * AscensionChallenge.enemySpeedModifier(this)/((Dungeon.isChallenged(Challenges.RANDOM_HP) && scaleFactor != 1) ? (0.8f * scaleFactor) : 1);
	}

	public final boolean surprisedBy( Char enemy ){
		return surprisedBy( enemy, true);
	}

	public boolean surprisedBy( Char enemy, boolean attacking ){
		return enemy == hero
				&& (enemy.invisible > 0 || !enemySeen || (fieldOfView != null && fieldOfView.length == Dungeon.level.length() && !fieldOfView[enemy.pos]))
				&& (!attacking || enemy.canSurpriseAttack());
	}

	//whether the hero should interact with the mob (true) or attack it (false)
	public boolean heroShouldInteract(){
		return alignment != Alignment.ENEMY && buff(Amok.class) == null;
	}

	public void aggro( Char ch ) {
		enemy = ch;
		if (state != PASSIVE){
			state = HUNTING;
		}
	}

	public void clearEnemy(){
		enemy = null;
		enemySeen = false;
		if (state == HUNTING) state = WANDERING;
	}

	public boolean isTargeting( Char ch){
		return enemy == ch;
	}

	//2.5x speed to 0.71x speed
	@Override
	public float attackDelay() {
		return super.attackDelay()*((Dungeon.isChallenged(Challenges.RANDOM_HP) && scaleFactor != 1) ? (0.8f * scaleFactor) : 1);
	}

	//70% damage to 245% damage
	@Override
	public int attackProc(Char enemy, int damage) {
		return super.attackProc(enemy, (int) (damage*((Dungeon.isChallenged(Challenges.RANDOM_HP) && scaleFactor != 1) ? (1.4f * scaleFactor) : 1)));
	}

	@Override
	protected void onDamage(int dmg, Object src) {
		if (!isInvulnerable(src.getClass())) {
			if (state == SLEEPING) {
				state = WANDERING;
			}
			if (state != HUNTING && !(src instanceof Corruption)) {
				alerted = true;
			}
		}
		super.onDamage(dmg, src);
	}
	
	
	@Override
	public void destroy() {
		
		super.destroy();
		
		Dungeon.level.mobs.remove( this );

		if (Dungeon.hero.buff(MindVision.class) != null){
			Dungeon.observe();
			GameScene.updateFog(pos, 2);
		}

		if (hero.isAlive()) {
			
			if (alignment == Alignment.ENEMY) {
				Statistics.enemiesSlain++;
				Badges.validateMonstersSlain();
				Statistics.qualifiedForNoKilling = false;

				AscensionChallenge.processEnemyKill(this);

				int exp = !isRewardSuppressed() ? EXP : 0;

				//during ascent, under-levelled enemies grant 10 xp each until level 30
				// after this enemy kills which reduce the amulet curse still grant 10 effective xp
				// for the purposes of on-exp effects, see AscensionChallenge.processEnemyKill
				if (Dungeon.hero.buff(AscensionChallenge.class) != null &&
						exp == 0 && maxLvl > 0 && EXP > 0 && Dungeon.hero.lvl < Hero.MAX_LEVEL){
					exp = Math.round(10 * spawningWeight());
				}
				if (exp > 0) {
					hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(exp), FloatingText.EXPERIENCE);
				}
				Dungeon.hero.earnExp(exp, getClass());

				if (Dungeon.hero.isSubclassedLoosely(HeroSubClass.MONK)){
					Buff.affect(Dungeon.hero, MonkEnergy.class).gainEnergy(this);
				}
				if (HighnessBuff.isEnergized() && Dungeon.hero.hasTalent(Talent.SLASH_RUNNER)){
					Buff.affect( Dungeon.hero, MeleeWeapon.Charger.class ).gainCharge(0.5f);
				}
				hero.earnExp(exp, getClass());
			}

			if (Random.Int(12) < hero.pointsInTalent(Talent.PRIMAL_AWAKENING)
				&& hero.buff(NaturesPower.naturesPowerTracker.class) != null){
				GnollTrickster gnoll = new GnollTrickster();
				gnoll.alignment = Alignment.ALLY;
				gnoll.state = gnoll.HUNTING;
				gnoll.HP = gnoll.HT = 50;
				gnoll.pos = pos;
				CellEmitter.center(pos).burst(LeafParticle.GENERAL, 25);
				Sample.INSTANCE.play(Assets.Sounds.BADGE);
				Dungeon.level.pressCell(gnoll.pos);
				GameScene.add(gnoll);
			}
		}
	}
	
	@Override
	public void die( Object cause ) {

		if (cause == Chasm.class){
			//50% chance to round up, 50% to round down
			if (EXP % 2 == 1) EXP += Random.Int(2);
			EXP /= 2;
		}

		if (cause instanceof Hero && ((Hero) cause).pointsInTalent(Talent.VOID_WRATH) > 2){
			HashSet<Buff> debuffs = FunctionalStuff.extract(buffs(), (buff) -> buff.type == Buff.buffType.NEGATIVE);
			if (!debuffs.isEmpty()){
				int heal = Math.min( 8, hero.HT-hero.HP);
				hero.HP += heal;
				Emitter e = hero.sprite.emitter();
				if (e != null && heal > 0) e.burst(Speck.factory(Speck.HEALING), 4);
			}
		}
		if (Dungeon.isChallenged(Challenges.NO_LEVELS)){
			EXP = 0;
		}

		if (alignment == Alignment.ENEMY){
			if (!(cause instanceof CurseInfusion)){
				rollToDropLoot();
			} else {
				EXP = 0;
			}
			if (cause == hero || cause instanceof Weapon || cause instanceof Weapon.Enchantment) {
				Talent.LethalMomentumTracker.process();
				Talent.LethalHasteCooldown.applyLethalHaste(Dungeon.hero, false);
			}
		}

		if (cause instanceof SpiritHawk.HawkAlly &&
			hero.hasTalent(Talent.BEAK_OF_POWER) &&
				Random.Int(3) < hero.pointsInTalent(Talent.BEAK_OF_POWER)){
			Buff.affect((Char) cause, Talent.LethalMomentumTracker.class, 1f);
		}

		if (hero.isAlive() && !Dungeon.level.heroFOV[pos]) {
			GLog.i( Messages.get(this, "died") );
		}

		boolean soulMarked = buff(SoulMark.class) != null;

		super.die( cause );

		if (RobotBuff.isRobot() && cause instanceof Hero && hero.pointsInTalent(Talent.MECHANICAL_POWER) > 1){
			Buff.affect(hero, Barkskin.class).set(hero.lvl, 4);
		}

		if (!(this instanceof Wraith)
				&& soulMarked
				&& Random.Float() < (hero.hasTalent(Talent.NECROMANCERS_MINIONS)
					? .15f* hero.pointsInTalent(Talent.NECROMANCERS_MINIONS)
					: 0.4f* hero.pointsInTalent(Talent.RK_WARLOCK))/3f){
			Wraith w = Wraith.spawnAt(pos, Wraith.class);
			if (w != null) {
				Buff.affect(w, Corruption.class);
				if (Dungeon.level.heroFOV[pos]) {
					CellEmitter.get(pos).burst(ShadowParticle.CURSE, 6);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
				}
			}
		}
	}

	public final boolean isRewardSuppressed() { return hero.lvl > maxLvl + 2; }

	public float lootChance(){
		float lootChance = this.lootChance;

		float dropBonus = RingOfWealth.dropChanceMultiplier( Dungeon.hero );

		Talent.BountyHunterTracker bhTracker = Dungeon.hero.buff(Talent.BountyHunterTracker.class);
		if (bhTracker != null){
			Preparation prep = Dungeon.hero.buff(Preparation.class);
			if (prep != null){
				// 2/4/8/16% per prep level, multiplied by talent points;
				float bhBonus = 0.02f * (float)Math.pow(2, prep.attackLevel()-1);
				bhBonus *= Dungeon.hero.byTalent(
						Talent.BOUNTY_HUNTER, 2,
						Talent.RK_ASSASSIN, 1);
				dropBonus += bhBonus;
			}
		}

		return lootChance * dropBonus;
	}
	public void rollToDropLoot(){
		if (isRewardSuppressed()) return;

		MasterThievesArmband.StolenTracker stolen = buff(MasterThievesArmband.StolenTracker.class);
		if (stolen == null || !stolen.itemWasStolen()) {
			if (Random.Float() < lootChance()) {
				Item loot = createLoot();
				if (loot != null) {
					Dungeon.level.drop(loot, pos).sprite.drop();
				}
			}
		}
		
		//ring of wealth logic
		if (Ring.getBuffedBonus(hero, RingOfWealth.Wealth.class) > 0) {
			int rolls = 1;
			if (properties.contains(Property.BOSS)) rolls = 15;
			else if (properties.contains(Property.MINIBOSS)) rolls = 5;
			ArrayList<Item> bonus = RingOfWealth.tryForBonusDrop(hero, rolls);
			if (bonus != null && !bonus.isEmpty()) {
				for (Item b : bonus) Dungeon.level.drop(b, pos).sprite.drop();
				RingOfWealth.showFlareForBonusDrop(sprite);
			}
		}
		
		//lucky enchant logic
		if (buff(Lucky.LuckProc.class) != null){
			Dungeon.level.drop(buff(Lucky.LuckProc.class).genLoot(), pos).sprite.drop();
			Lucky.showFlare(sprite);
		}

		//soul eater talent
		if (buff(SoulMark.class) != null &&
				Random.Float() < Math.max(
						.15f* hero.pointsInTalent(Talent.SOUL_EATER),
						.10f* hero.pointsInTalent(Talent.RK_WARLOCK))){
			Talent.onFoodEaten(hero, 0, null);
		}

		if (MonkEnergy.isFeelingEmpowered(Level.Feeling.LARGE)){
			Item money = new Gold().random();
			money.quantity(money.quantity()/10);
            Dungeon.level.drop(money, pos).sprite.drop();
        }

		if (MonkEnergy.isFeelingEmpowered(Level.Feeling.TRAPS) && Random.Float() < 0.125f){
			ReclaimTrap trap = new ReclaimTrap();
			trap.storedTrap = Random.element(Dungeon.level.traps.valueList()).getClass();
			Dungeon.level.drop(trap, pos).sprite.drop();
		}
	}
	
	protected Object loot = null;
	protected float lootChance = 0;

	public boolean canSee(int pos){
		return fieldOfView[pos];
	}

    @SuppressWarnings("unchecked")
	public Item createLoot() {
		Item item;
		if (loot instanceof Generator.Category) {

			item = Generator.randomUsingDefaults( (Generator.Category)loot );

		} else if (loot instanceof Class<?>) {

			item = Generator.random( (Class<? extends Item>)loot );

		} else {

			item = (Item)loot;

		}
		return item;
	}

	//how many mobs this one should count as when determining spawning totals
	public float spawningWeight(){
		return 1;
	}
	
	public boolean reset() {
		return false;
	}
	
	public void beckon( int cell ) {
		
		notice();
		
		if (state != HUNTING && state != FLEEING) {
			state = WANDERING;
		}
		target = cell;
	}
	
	public String description() {
		return Messages.get(this, "desc");
	}

	public String info(){
		String desc = description();

		for (Buff b : buffs(ChampionEnemy.class)){
			desc += "\n\n_" + Messages.titleCase(b.name()) + "_\n" + b.desc();
		}

		return desc;
	}
	
	public void notice() {
		sprite.showAlert();
	}
	
	public void yell( String str ) {
		GLog.newLine();
		GLog.n( "%s: \"%s\" ", Messages.titleCase(name()), str );
	}

	//returns true when a mob sees the hero, and is currently targeting them.
	public boolean focusingHero() {
		return enemySeen && (target == hero.pos);
	}

	public interface AiState {
		boolean act( boolean enemyInFOV, boolean justAlerted );
	}

	protected class Sleeping implements AiState {

		public static final String TAG	= "SLEEPING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {

			//debuffs cause mobs to wake as well
			for (Buff b : buffs()){
				if (b.type == Buff.buffType.NEGATIVE){
					awaken(enemyInFOV);
					if (state == SLEEPING){
						spend(TICK); //wait if we can't wake up for some reason
					}
					return true;
				}
			}

			if (enemyInFOV) {

				float enemyStealth = enemy.stealth();

				if (enemy instanceof Hero || enemy instanceof SpiritHawk.HawkAlly){
					int points = hero.pointsInTalent(false, Talent.SILENT_STEPS,Talent.PURSUIT);
					if (enemy instanceof SpiritHawk.HawkAlly) points = hero.pointsInTalent(Talent.BEAK_OF_POWER);
					if (MonkEnergy.isFeelingEmpowered(Level.Feeling.SECRETS)) points += 1;
					if (points > 0 && Dungeon.level.distance(pos, enemy.pos) >= 4 - points) {
						enemyStealth = Float.POSITIVE_INFINITY;
					}
				}

				if (Random.Float( distance( enemy ) + enemyStealth ) < 1) {
					awaken(enemyInFOV);
					if (state == SLEEPING){
						spend(TICK); //wait if we can't wake up for some reason
					}
					return true;
				}

			}

			enemySeen = false;
			spend( TICK );

			return true;
		}

		protected void awaken( boolean enemyInFOV ){
			if (enemyInFOV) {
				enemySeen = true;
				notice();
				state = HUNTING;
				target = enemy.pos;
			} else {
				notice();
				state = WANDERING;
				target = Dungeon.level.randomDestination( Mob.this );
			}

			if (alignment == Alignment.ENEMY && Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)) {
				for (Mob mob : Dungeon.level.mobs) {
					if (mob.paralysed <= 0
									&& Dungeon.level.distance(pos, mob.pos) <= 8 //TODO base on pathfinder distance instead?
							&& mob.state != mob.HUNTING) {
						mob.beckon(target);
					}
				}
			}
			spend(TIME_TO_WAKE_UP);
		}
	}

	protected class Wandering implements AiState {

		public static final String TAG	= "WANDERING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && (justAlerted || Random.Float( distance( enemy ) / 2f + enemy.stealth() ) < 1)) {

				return noticeEnemy();

			} else {

				return continueWandering();

			}
		}
		
		protected boolean noticeEnemy(){
			enemySeen = true;
			
			notice();
			alerted = true;
			state = HUNTING;
			target = enemy.pos;
			
			if (alignment == Alignment.ENEMY && Dungeon.isChallenged( Challenges.SWARM_INTELLIGENCE )) {
				for (Mob mob : Dungeon.level.mobs) {
					if (mob.paralysed <= 0
							&& Dungeon.level.distance(pos, mob.pos) <= 8 //TODO base on pathfinder distance instead?
							&& mob.state != mob.HUNTING) {
						mob.beckon( target );
					}
				}
			}
			
			return true;
		}
		
		protected boolean continueWandering(){
			enemySeen = false;
			
			int oldPos = pos;
			if (target != -1 && getCloser( target )) {
				if (Dungeon.level.water[pos] && buff(ChampionEnemy.Flowing.class) != null){
					spend(0.01f / speed());
				}
				else spend( 1 / speed() );
				return moveSprite( oldPos, pos );
			} else {
				target = randomDestination();
				spend( TICK );
			}
			
			return true;
		}

		protected int randomDestination(){
			return Dungeon.level.randomDestination( Mob.this );
		}

	}

	protected class Hunting implements AiState {

		public static final String TAG	= "HUNTING";

		//prevents rare infinite loop cases
		private boolean recursing = false;

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {

				target = enemy.pos;
				return doAttack( enemy );

			} else {

				if (enemyInFOV) {
					target = enemy.pos;
				} else if (enemy == null) {
					sprite.showLost();
					state = WANDERING;
					target = ((Mob.Wandering)WANDERING).randomDestination();
					spend( TICK );
					return true;
				}
				
				int oldPos = pos;
				if (target != -1 && getCloser( target )) {

					if (Dungeon.level.water[pos] && buff(ChampionEnemy.Flowing.class) != null){
						spend(0.01f / speed());
					}
					else spend( 1 / speed() );
					return moveSprite( oldPos,  pos );

				} else {

					//if moving towards an enemy isn't possible, try to switch targets to another enemy that is closer
					//unless we have already done that and still can't move toward them, then move on.
					if (!recursing) {
						Char oldEnemy = enemy;
						enemy = null;
						enemy = chooseEnemy();
						if (enemy != null && enemy != oldEnemy) {
							recursing = true;
							boolean result = act(enemyInFOV, justAlerted);
							recursing = false;
							return result;
						}
					}

					spend( TICK );
					if (!enemyInFOV) {
						sprite.showLost();
						state = WANDERING;
						target = ((Mob.Wandering)WANDERING).randomDestination();
					}
					return true;
				}
			}
		}
	}

	protected class Fleeing implements AiState {

		public static final String TAG	= "FLEEING";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			//triggers escape logic when 0-dist rolls a 6 or greater.
			if (enemy == null || !enemyInFOV && 1 + Random.Int(Dungeon.level.distance(pos, target)) >= 6){
				escaped();
				if (state != FLEEING){
					spend( TICK );
					return true;
				}
			
			//if enemy isn't in FOV, keep running from their previous position.
			} else if (enemyInFOV) {
				target = enemy.pos;
			}

			int oldPos = pos;
			if (target != -1 && getFurther( target )) {

				if (Dungeon.level.water[pos] && buff(ChampionEnemy.Flowing.class) != null){
					spend(0.01f / speed());
				}
				else spend( 1 / speed() );
				return moveSprite( oldPos, pos );

			} else {

				spend( TICK );
				nowhereToRun();

				return true;
			}
		}

		protected void escaped(){
			//does nothing by default, some enemies have special logic for this
		}

		//enemies will turn and fight if they have nowhere to run and aren't affect by terror
		protected void nowhereToRun() {
			if (buff( Terror.class ) == null
					&& buffs( AllyBuff.class ).isEmpty()
					&& buff( Dread.class ) == null) {
				if (enemySeen) {
					sprite.showStatus(CharSprite.WARNING, Messages.get(Mob.class, "rage"));
					state = HUNTING;
				} else {
					state = WANDERING;
				}
			}
		}
	}

	protected class Passive implements AiState {

		public static final String TAG	= "PASSIVE";

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			spend( TICK );
			return true;
		}
	}
	
	
	private static ArrayList<Mob> heldAllies = new ArrayList<>();

	public static void holdAllies( Level level ){
		holdAllies(level, hero.pos);
	}

	public static void holdAllies( Level level, int holdFromPos ){
		heldAllies.clear();
		for (Mob mob : level.mobs.toArray( new Mob[0] )) {
			//preserve directable allies no matter where they are
			if (mob instanceof DirectableAlly) {
				((DirectableAlly) mob).clearDefensingPos();
				level.mobs.remove( mob );
				heldAllies.add(mob);
				
			//preserve intelligent allies if they are near the hero
			} else if (mob.alignment == Alignment.ALLY
					&& (mob.intelligentAlly || mob.buff(SummonElemental.InvisAlly.class) != null)
					&& Dungeon.level.distance(holdFromPos, mob.pos) <= 5){
				level.mobs.remove( mob );
				heldAllies.add(mob);
			}
		}
	}

	public static void restoreAllies( Level level, int pos ){
		restoreAllies(level, pos, -1);
	}

	public static void restoreAllies( Level level, int pos, int gravitatePos ){
		if (!heldAllies.isEmpty()){
			
			ArrayList<Integer> candidatePositions = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8) {
				if (!Dungeon.level.solid[i+pos] && !Dungeon.level.avoid[i+pos] && level.findMob(i+pos) == null){
					candidatePositions.add(i+pos);
				}
			}

			//gravitate pos sets a preferred location for allies to be closer to
			if (gravitatePos == -1) {
				Collections.shuffle(candidatePositions);
			} else {
				Collections.sort(candidatePositions, new Comparator<Integer>() {
					@Override
					public int compare(Integer t1, Integer t2) {
						return Dungeon.level.distance(gravitatePos, t1) -
								Dungeon.level.distance(gravitatePos, t2);
					}
				});
			}

			for (Mob ally : heldAllies) {
				level.mobs.add(ally);
				ally.state = ally.WANDERING;
				
				if (!candidatePositions.isEmpty()){
					ally.pos = candidatePositions.remove(0);
				} else {
					ally.pos = pos;
				}
				if (ally.sprite != null) ally.sprite.place(ally.pos);

				if (ally.fieldOfView == null || ally.fieldOfView.length != level.length()){
					ally.fieldOfView = new boolean[level.length()];
				}
				if (ally instanceof Ech)
					((Ech) ally).update();
				Dungeon.level.updateFieldOfView( ally, ally.fieldOfView );

			}
		}
		heldAllies.clear();
	}
	
	public static void clearHeldAllies(){
		heldAllies.clear();
	}
}

