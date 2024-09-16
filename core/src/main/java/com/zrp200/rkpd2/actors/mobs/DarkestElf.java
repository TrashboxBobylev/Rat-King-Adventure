/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 *  Shattered Pixel Dungeon
 *  Copyright (C) 2014-2022 Evan Debenham
 *
 * Summoning Pixel Dungeon
 * Copyright (C) 2019-2022 TrashboxBobylev
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
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Corruption;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Beam;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.PotionOfHealing;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.DarkestElfSprite;

public class DarkestElf extends AbyssalMob {

	{
		spriteClass = DarkestElfSprite.class;

		HP = HT = 145;
		defenseSkill = 19;

		EXP = 13;

		loot = new PotionOfHealing();
		lootChance = 0.2f; //see createloot

		properties.add(Property.UNDEAD);

		HUNTING = new Hunting();
	}

	public boolean summoning = false;
	private Emitter summoningEmitter = null;
	private int summoningPos = -1;

	private boolean firstSummon = true;

	private NecroSlime mySkeleton;
	private int storedSkeletonID = -1;

	@Override
	protected boolean act() {
		if (summoning && state != HUNTING){
			summoning = false;
			updateSpriteState();
		}
		return super.act();
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();

		if (summoning && summoningEmitter == null){
			summoningEmitter = CellEmitter.get( summoningPos );
			summoningEmitter.pour(Speck.factory(Speck.RATTLE), 0.2f);
			sprite.zap( summoningPos );
		} else if (!summoning && summoningEmitter != null){
			summoningEmitter.on = false;
			summoningEmitter = null;
		}
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0 + abyssLevel()*4, 5 + abyssLevel()*7);
	}

	@Override
	public void rollToDropLoot() {
		lootChance *= ((6f - Dungeon.LimitedDrops.NECRO_HP.count) / 6f);
		super.rollToDropLoot();
	}

	@Override
	public void spend(float time) {
		super.spend(time / (2f + abyssLevel()*0.5f));
	}

	@Override
	public Item createLoot(){
		Dungeon.LimitedDrops.NECRO_HP.count++;
		return super.createLoot();
	}

	@Override
	public void die(Object cause) {
		if (storedSkeletonID != -1){
			Actor ch = Actor.findById(storedSkeletonID);
			storedSkeletonID = -1;
			if (ch instanceof NecroSlime){
				mySkeleton = (NecroSlime) ch;
			}
		}

		if (mySkeleton != null && mySkeleton.isAlive()){
			mySkeleton.die(null);
		}

		if (summoningEmitter != null){
			summoningEmitter.on = false;
			summoningEmitter = null;
		}

		super.die(cause);
	}

	private static final String SUMMONING = "summoning";
	private static final String FIRST_SUMMON = "first_summon";
	private static final String SUMMONING_POS = "summoning_pos";
	private static final String MY_SKELETON = "my_skeleton";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put( SUMMONING, summoning );
		bundle.put( FIRST_SUMMON, firstSummon );
		if (summoning){
			bundle.put( SUMMONING_POS, summoningPos);
		}
		if (mySkeleton != null){
			bundle.put( MY_SKELETON, mySkeleton.id() );
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		summoning = bundle.getBoolean( SUMMONING );
		if (bundle.contains(FIRST_SUMMON)) firstSummon = bundle.getBoolean(FIRST_SUMMON);
		if (summoning){
			summoningPos = bundle.getInt( SUMMONING_POS );
		}
		if (bundle.contains( MY_SKELETON )){
			storedSkeletonID = bundle.getInt( MY_SKELETON );
		}
	}

	public void onZapComplete(){
		if (mySkeleton == null || mySkeleton.sprite == null || !mySkeleton.isAlive()){
			return;
		}

		//heal skeleton first
		if (mySkeleton.HP < mySkeleton.HT){

			if (sprite.visible || mySkeleton.sprite.visible) {
				sprite.parent.add(new Beam.HealthRay(sprite.center(), mySkeleton.sprite.center()));
			}

			mySkeleton.HP = Math.min(mySkeleton.HP + 20 + abyssLevel()*10, mySkeleton.HT);
			mySkeleton.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );

			//otherwise give it adrenaline
		} else if (mySkeleton.buff(Adrenaline.class) == null) {

			if (sprite.visible || mySkeleton.sprite.visible) {
				sprite.parent.add(new Beam.HealthRay(sprite.center(), mySkeleton.sprite.center()));
			}

			Buff.affect(mySkeleton, Adrenaline.class, 3f + abyssLevel()*3f);
		}

		next();
	}

	private class Hunting extends Mob.Hunting{

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			enemySeen = enemyInFOV;

			if (storedSkeletonID != -1){
				Actor ch = Actor.findById(storedSkeletonID);
				storedSkeletonID = -1;
				if (ch instanceof NecroSlime){
					mySkeleton = (NecroSlime) ch;
				}
			}

			if (summoning){

				//push anything on summoning spot away, to the furthest valid cell
				if (Actor.findChar(summoningPos) != null) {
					int pushPos = pos;
					for (int c : PathFinder.NEIGHBOURS8) {
						if (Actor.findChar(summoningPos + c) == null
								&& Dungeon.level.passable[summoningPos + c]
								&& Dungeon.level.trueDistance(pos, summoningPos + c) > Dungeon.level.trueDistance(pos, pushPos)) {
							pushPos = summoningPos + c;
						}
					}

					//push enemy, or wait a turn if there is no valid pushing position
					if (pushPos != pos) {
						Char ch = Actor.findChar(summoningPos);
						Actor.addDelayed( new Pushing( ch, ch.pos, pushPos ), -1 );

						ch.pos = pushPos;
						Dungeon.level.occupyCell(ch );

					} else {

						Char blocker = Actor.findChar(summoningPos);
						if (blocker.alignment != alignment){
							blocker.damage( Random.NormalIntRange(18, 39), this );
						}

						spend(TICK);
						return true;
					}
				}

				summoning = firstSummon = false;

				mySkeleton = new NecroSlime();
				mySkeleton.pos = summoningPos;
				GameScene.add( mySkeleton );
				Dungeon.level.occupyCell( mySkeleton );
				for (Buff b : buffs(ChampionEnemy.class)){
					Buff.affect( mySkeleton, b.getClass());
				}
				Sample.INSTANCE.play(Assets.Sounds.BONES);
				summoningEmitter.burst( Speck.factory( Speck.RATTLE ), 5 );
				sprite.idle();

				if (buff(Corruption.class) != null){
					Buff.affect(mySkeleton, Corruption.class);
				}

				spend(TICK);
				return true;
			}

			if (mySkeleton != null &&
					(!mySkeleton.isAlive()
							|| !Dungeon.level.mobs.contains(mySkeleton)
							|| mySkeleton.alignment != alignment)){
				mySkeleton = null;
			}

			//if enemy is seen, and enemy is within range, and we haven no skeleton, summon a skeleton!
			if (enemySeen && Dungeon.level.distance(pos, enemy.pos) <= 4 && mySkeleton == null && buff(Talent.AntiMagicBuff.class) == null){

				summoningPos = -1;
				for (int c : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(enemy.pos+c) == null
							&& Dungeon.level.passable[enemy.pos+c]
							&& fieldOfView[enemy.pos+c]
							&& Dungeon.level.trueDistance(pos, enemy.pos+c) < Dungeon.level.trueDistance(pos, summoningPos)){
						summoningPos = enemy.pos+c;
					}
				}

				if (summoningPos != -1){

					summoning = true;
					summoningEmitter = CellEmitter.get(summoningPos);
					summoningEmitter.pour(Speck.factory(Speck.RATTLE), 0.2f);

					sprite.zap( summoningPos );

					spend( firstSummon ? TICK : 2*TICK );
				} else {
					//wait for a turn
					spend(TICK);
				}

				return true;
				//otherwise, if enemy is seen, and we have a skeleton...
			} else if (enemySeen && mySkeleton != null && buff(Talent.AntiMagicBuff.class) == null){

				target = enemy.pos;
				spend(TICK);

				if (!fieldOfView[mySkeleton.pos]){

					//if the skeleton is not next to the enemy
					//teleport them to the closest spot next to the enemy that can be seen
					if (!Dungeon.level.adjacent(mySkeleton.pos, enemy.pos)){
						int telePos = -1;
						for (int c : PathFinder.NEIGHBOURS8){
							if (Actor.findChar(enemy.pos+c) == null
									&& Dungeon.level.passable[enemy.pos+c]
									&& fieldOfView[enemy.pos+c]
									&& Dungeon.level.trueDistance(pos, enemy.pos+c) < Dungeon.level.trueDistance(pos, telePos)){
								telePos = enemy.pos+c;
							}
						}

						if (telePos != -1){

							ScrollOfTeleportation.appear(mySkeleton, telePos);
							mySkeleton.teleportSpend();

							if (sprite != null && sprite.visible){
								sprite.zap(telePos);
								return false;
							} else {
								onZapComplete();
							}
						}
					}

					return true;

				} else {

					//zap skeleton
					if (mySkeleton.HP < mySkeleton.HT || mySkeleton.buff(Adrenaline.class) == null) {
						if (sprite != null && sprite.visible){
							sprite.zap(mySkeleton.pos);
							return false;
						} else {
							onZapComplete();
						}
					}

				}

				return true;

				//otherwise, default to regular hunting behaviour
			} else {
				return super.act(enemyInFOV, justAlerted);
			}
		}
	}

	public static class NecroSlime extends DarkSlime {

		{
			state = HUNTING;
		}



		@Override
		public float spawningWeight() {
			return 0;
		}

		private void teleportSpend(){
			spend(TICK);
		}

	}
}
