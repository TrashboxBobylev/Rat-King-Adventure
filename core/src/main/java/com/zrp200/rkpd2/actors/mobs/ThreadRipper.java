/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.GodSlayerFire;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.PowerfulDegrade;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.TargetedCell;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.ThreadripperSprite;
import com.zrp200.rkpd2.utils.GLog;

public class ThreadRipper extends Mob {

	{
		spriteClass = ThreadripperSprite.class;

		HP = HT = (int) (8 * getModifier());
		defenseSkill = (int) (5 * getModifier());
		viewDistance = Light.DISTANCE;

		EXP = (int) (3 * getModifier()); //for corrupting
		maxLvl = -1;

		HUNTING = new Hunting();

		baseSpeed = 1f;
		loot = Generator.random();
		lootChance = 1f;

		properties.add(Property.DEMONIC);
		properties.add(Property.UNDEAD);
		if (Dungeon.isChallenged(Challenges.NO_HERBALISM)){
			immunities.add(GodSlayerFire.class);
		}
	}
	private static double getModifier() { return Math.max(1, Dungeon.scalingDepth() /5d); }

	@Override
	public float spawningWeight() {
		return 0;
	}
	protected int[]
			damageRange = {1,4},
			armorRange  = {1,3};

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( (int)(damageRange[0]*getModifier()), (int)(damageRange[1]*getModifier()) );
	}

	@Override
	public int attackSkill( Char target ) {
		return (int) (8*getModifier());
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(armorRange[0], armorRange[1]);
	}

	private static final String LAST_ENEMY_POS = "last_enemy_pos";
	private static final String LEAP_POS = "leap_pos";
	private static final String LEAP_CD = "leap_cd";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LAST_ENEMY_POS, lastEnemyPos);
		bundle.put(LEAP_POS, leapPos);
		bundle.put(LEAP_CD, leapCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lastEnemyPos = bundle.getInt(LAST_ENEMY_POS);
		leapPos = bundle.getInt(LEAP_POS);
		leapCooldown = bundle.getFloat(LEAP_CD);
	}

	private int lastEnemyPos = -1;

	@Override
	protected void onDamage(int dmg, Object src) {
		super.onDamage(dmg, src);
		int count = 0;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
			if (mob.alignment == alignment && Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)) {
				count += 1;
			}
		}

		if (count < getModifier()) {

			PathFinder.buildDistanceMap(Dungeon.hero.pos, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));

			Mob mob = Reflection.newInstance(ThreadRipper.class);
			mob.state = mob.WANDERING;
			mob.pos = Dungeon.level.randomRespawnCell( mob );
			if (Dungeon.hero.isAlive() && mob.pos != -1 && PathFinder.distance[mob.pos] >= 12) {
				GameScene.add( mob );
				if (Statistics.amuletObtained) {
					mob.beckon( Dungeon.hero.pos );
				}
				if (!mob.buffs(ChampionEnemy.class).isEmpty()){
					GLog.w(Messages.get(ChampionEnemy.class, "warn"));
				}
			}
		}
	}

	@Override
	protected boolean act() {
		AiState lastState = state;
		boolean result = super.act();
		if (paralysed <= 0) leapCooldown --;

		//if state changed from wandering to hunting, we haven't acted yet, don't update.
		if (!(lastState == WANDERING && state == HUNTING)) {
			if (enemy != null) {
				lastEnemyPos = enemy.pos;
			} else {
				lastEnemyPos = Dungeon.hero.pos;
			}
		}

		return result;
	}

	@Override
	public void spend(float time) {
		super.spend(time/ (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 2 : 1));
	}

	@Override
	public void move(int step) {
		super.move(step);
		if (Dungeon.level.flamable[step] && Dungeon.isChallenged(Challenges.NO_HERBALISM)){
			GameScene.add(Blob.seed(step, 2, GodSlayerFire.class));
		}
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		if (Random.Int( 3 ) == 0 && Dungeon.isChallenged(Challenges.NO_SCROLLS)) {
			Buff.affect( enemy, PowerfulDegrade.class, 7f);
		}
		if (Random.Int(2) == 0 && Dungeon.isChallenged(Challenges.NO_ARMOR)){
			Buff.affect(enemy, Vulnerable.class, 10f);
		}
		return super.attackProc(enemy, damage);
	}



	private int leapPos = -1;
	private float leapCooldown = 0;

	public class Hunting extends Mob.Hunting {

		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {

			if (leapPos != -1){

				leapCooldown = Random.NormalIntRange(2, 4);
				Ballistica b = new Ballistica(pos, leapPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);

				//check if leap pos is not obstructed by terrain
				if (rooted || b.collisionPos != leapPos){
					leapPos = -1;
					return true;
				}

				final Char leapVictim = Actor.findChar(leapPos);
				final int endPos;

				//ensure there is somewhere to land after leaping
				if (leapVictim != null){
					int bouncepos = -1;
					for (int i : PathFinder.NEIGHBOURS8){
						if ((bouncepos == -1 || Dungeon.level.trueDistance(pos, leapPos+i) < Dungeon.level.trueDistance(pos, bouncepos))
								&& Actor.findChar(leapPos+i) == null && Dungeon.level.passable[leapPos+i]){
							bouncepos = leapPos+i;
						}
					}
					if (bouncepos == -1) {
						leapPos = -1;
						return true;
					} else {
						endPos = bouncepos;
					}
				} else {
					endPos = leapPos;
				}

				//do leap
				sprite.visible = Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos] || Dungeon.level.heroFOV[endPos];
				sprite.jump(pos, leapPos, new Callback() {
					@Override
					public void call() {

						if (leapVictim != null && alignment != leapVictim.alignment){
							Buff.affect(leapVictim, Bleeding.class).set(0.75f*damageRoll());
							if (Dungeon.isChallenged(Challenges.NO_FOOD) && leapVictim == Dungeon.hero){
								Dungeon.hero.buff(Hunger.class).affectHunger(-100);
							}
							if (Dungeon.isChallenged(Challenges.NO_HEALING)){
								int healAmt = Math.round(damageRoll());
								healAmt = Math.min( healAmt, leapVictim.HT - leapVictim.HP );

								if (healAmt > 0 && isAlive()) {

									HP += healAmt;
									sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 1 );
									sprite.showStatus( CharSprite.POSITIVE, Integer.toString( healAmt ) );

								}
							}
							leapVictim.sprite.flash();
							Sample.INSTANCE.play(Assets.Sounds.HIT);
						}

						if (endPos != leapPos){
							Actor.addDelayed(new Pushing(ThreadRipper.this, leapPos, endPos), -1);
						}

						pos = endPos;
						leapPos = -1;
						sprite.idle();
						Dungeon.level.occupyCell(ThreadRipper.this);
						next();
					}
				});
				return false;
			}

			enemySeen = enemyInFOV;
			if (enemyInFOV && !isCharmedBy( enemy ) && canAttack( enemy )) {

				return doAttack( enemy );

			} else {

				if (enemyInFOV) {
					target = enemy.pos;
				} else if (enemy == null) {
					state = WANDERING;
					target = Dungeon.level.randomDestination( ThreadRipper.this );
					return true;
				}

				if (leapCooldown <= 0 && enemyInFOV && !rooted
						&& Dungeon.level.distance(pos, enemy.pos) >= 3 - (Dungeon.isChallenged(Challenges.DARKNESS) ? 4 : 0)) {

					int targetPos = enemy.pos;
					if (lastEnemyPos != enemy.pos){
						int closestIdx = 0;
						for (int i = 1; i < PathFinder.CIRCLE8.length; i++){
							if (Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[i])
									< Dungeon.level.trueDistance(lastEnemyPos, enemy.pos+PathFinder.CIRCLE8[closestIdx])){
								closestIdx = i;
							}
						}
						targetPos = enemy.pos + PathFinder.CIRCLE8[(closestIdx+4)%8];
					}

					Ballistica b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
					//try aiming directly at hero if aiming near them doesn't work
					if (b.collisionPos != targetPos && targetPos != enemy.pos){
						targetPos = enemy.pos;
						b = new Ballistica(pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
					}
					if (b.collisionPos == targetPos){
						//get ready to leap
						leapPos = targetPos;
						//don't want to overly punish players with slow move or attack speed
						spend(GameMath.gate(TICK, enemy.cooldown(), 3*TICK));
						if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[leapPos]){
							GLog.w(Messages.get(ThreadRipper.this, "leap"));
							sprite.parent.addToBack(new TargetedCell(leapPos, 0xFF0000));
							((ThreadripperSprite)sprite).leapPrep( leapPos );
							Dungeon.hero.interrupt();
						}
						return true;
					}
				}

				int oldPos = pos;
				if (target != -1 && getCloser( target )) {

					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );

				} else {
					spend( TICK );
					if (!enemyInFOV) {
						sprite.showLost();
						state = WANDERING;
						target = Dungeon.level.randomDestination( ThreadRipper.this );
					}
					return true;
				}
			}
		}

	}

}
