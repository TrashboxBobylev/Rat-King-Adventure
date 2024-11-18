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

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Amok;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Charm;
import com.zrp200.rkpd2.actors.buffs.Dread;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.Light;
import com.zrp200.rkpd2.actors.buffs.LockedFloor;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.Sleep;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.WarpedEnemy;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.mobs.npcs.Sheep;
import com.zrp200.rkpd2.effects.Beam;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.Pushing;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.TargetedCell;
import com.zrp200.rkpd2.effects.particles.PurpleParticle;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.potions.PotionOfExperience;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.mechanics.ShadowCaster;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.LarvaSprite;
import com.zrp200.rkpd2.sprites.YogSprite;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.BossHealthBar;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class YogDzewa extends Mob {

	{
		spriteClass = YogSprite.class;

		HP = HT = 1000;

		EXP = 50;

		//so that allies can attack it. States are never actually used.
		state = HUNTING;

		viewDistance = 12;

		properties.add(Property.BOSS);
		properties.add(Property.IMMOVABLE);
		properties.add(Property.DEMONIC);
	}

	private int phase = 0;

	private float abilityCooldown;
	private static final int MIN_ABILITY_CD = 10;
	private static final int MAX_ABILITY_CD = 15;

	private float summonCooldown;
	private static final int MIN_SUMMON_CD = 10;
	private static final int MAX_SUMMON_CD = 15;

	private static Class getPairedFist(Class fist){
		if (fist == YogFist.BurningFist.class) return YogFist.SoiledFist.class;
		if (fist == YogFist.SoiledFist.class) return YogFist.BurningFist.class;
		if (fist == YogFist.RottingFist.class) return YogFist.RustedFist.class;
		if (fist == YogFist.RustedFist.class) return YogFist.RottingFist.class;
		if (fist == YogFist.BrightFist.class) return YogFist.DarkFist.class;
		if (fist == YogFist.DarkFist.class) return YogFist.BrightFist.class;
		return null;
	}

	private ArrayList<Class> fistSummons = new ArrayList<>();
	private ArrayList<Class> challengeSummons = new ArrayList<>();
	{
		//offset seed slightly to avoid output patterns
		Random.pushGenerator(Dungeon.seedCurDepth()+1);
			fistSummons.add(Random.Int(2) == 0 ? YogFist.BurningFist.class : YogFist.SoiledFist.class);
			fistSummons.add(Random.Int(2) == 0 ? YogFist.RottingFist.class : YogFist.RustedFist.class);
			fistSummons.add(Random.Int(2) == 0 ? YogFist.BrightFist.class : YogFist.DarkFist.class);
			Random.shuffle(fistSummons);
			//randomly place challenge summons so that two fists of a pair can never spawn together
			if (Random.Int(2) == 0){
				challengeSummons.add(getPairedFist(fistSummons.get(1)));
				challengeSummons.add(getPairedFist(fistSummons.get(2)));
				challengeSummons.add(getPairedFist(fistSummons.get(0)));
			} else {
				challengeSummons.add(getPairedFist(fistSummons.get(2)));
				challengeSummons.add(getPairedFist(fistSummons.get(0)));
				challengeSummons.add(getPairedFist(fistSummons.get(1)));
			}
		Random.popGenerator();
	}

	private ArrayList<Class> regularSummons = new ArrayList<>();
	{
		if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
			for (int i = 0; i < 6; i++){
				if (i >= 4){
					regularSummons.add(YogRipper.class);
				} else if (i >= Statistics.spawnersAlive){
					regularSummons.add(Larva.class);
				} else {
					regularSummons.add( i % 2 == 0 ? YogEye.class : YogScorpio.class);
				}
			}
		} else {
			for (int i = 0; i < 4; i++){
				if (i >= Statistics.spawnersAlive){
					regularSummons.add(Larva.class);
				} else {
					regularSummons.add(YogRipper.class);
				}
			}
		}
		Random.shuffle(regularSummons);
	}

	private ArrayList<Integer> targetedCells = new ArrayList<>();

	@Override
	public int attackSkill(Char target) {
		return INFINITE_ACCURACY;
	}

	@Override
	protected boolean act() {
		//char logic
		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
		}
		Dungeon.level.updateFieldOfView( this, fieldOfView );

		throwItems();

		sprite.hideAlert();
		sprite.hideLost();

		//mob logic
		enemy = chooseEnemy();

		enemySeen = enemy != null && enemy.isAlive() && fieldOfView[enemy.pos] && enemy.invisible <= 0;
		//end of char/mob logic

		if (phase == 0){
			if (Dungeon.hero.viewDistance >= Dungeon.level.distance(pos, Dungeon.hero.pos)) {
				Dungeon.observe();
			}
			if (Dungeon.level.heroFOV[pos]) {
				notice();
			}
		}

		if (phase == 4 && findFist() == null){
			yell(Messages.get(this, "hope"));
			summonCooldown = -15; //summon a burst of minions!
			phase = 5;
			BossHealthBar.bleed(true);
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					Music.INSTANCE.fadeOut(0.5f, new Callback() {
						@Override
						public void call() {
							Music.INSTANCE.play(Assets.Music.HALLS_BOSS_FINALE, true);
						}
					});
				}
			});
		}

		if (phase == 0){
			spend(TICK);
			return true;
		} else {

			boolean terrainAffected = false;
			HashSet<Char> affected = new HashSet<>();
			//delay fire on a rooted hero
			if (!Dungeon.hero.rooted) {
				for (int i : targetedCells) {
					Ballistica b = new Ballistica(pos, i, Ballistica.WONT_STOP);
					//shoot beams
					sprite.parent.add(new Beam.DeathRay(sprite.center(), DungeonTilemap.raisedTileCenterToWorld(b.collisionPos)));
					for (int p : b.path) {
						Char ch = Actor.findChar(p);
						if (ch != null && (ch.alignment != alignment || ch instanceof Bee)) {
							affected.add(ch);
						}
						if (Dungeon.level.flamable[p]) {
							Dungeon.level.destroy(p);
							GameScene.updateMap(p);
							terrainAffected = true;
						}
					}
				}
				if (terrainAffected) {
					Dungeon.observe();
				}
				Invisibility.dispel(this);
				for (Char ch : affected) {

					if (ch == Dungeon.hero) {
						Statistics.bossScores[4] -= 500;
					}

					if (hit( this, ch, true )) {
						if (ch == Dungeon.hero) {
							Statistics.bossScores[4] -= 500;
						}
						int dmg;
						if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
							dmg = Random.NormalIntRange(30, 50);
						} else {
							dmg = Random.NormalIntRange(20, 30);
						}
						if (ch.buff(WarriorParry.BlockTrock.class) != null){
							ch.sprite.emitter().burst( Speck.factory( Speck.FORGE ), 15 );
							SpellSprite.show(ch, SpellSprite.BLOCK, 2f, 2f, 2f);
							Buff.affect(ch, Barrier.class).incShield(Math.round(dmg*1.25f));
							ch.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(Math.round(dmg*1.25f)), FloatingText.SHIELDING );
							ch.buff(WarriorParry.BlockTrock.class).triggered = true;
						} else {
							ch.damage(dmg, new Eye.DeathGaze());

							if (Dungeon.level.heroFOV[pos]) {
								ch.sprite.flash();
								CellEmitter.center(pos).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
							}
							if (!ch.isAlive() && ch == Dungeon.hero) {
								Badges.validateDeathFromEnemyMagic();
								Dungeon.fail(getClass());
								GLog.n(Messages.get(Char.class, "kill", name()));
							}
						}
					} else {
						ch.sprite.showStatus( CharSprite.NEUTRAL,  ch.defenseVerb() );
					}
				}
				targetedCells.clear();
			}

			if (abilityCooldown <= 0){

				int beams = 1 + (HT - HP)/400;
				if (Dungeon.isChallenged(Challenges.EVIL_MODE)){
					beams *= 2;
				}
				HashSet<Integer> affectedCells = new HashSet<>();
				PathFinder.buildDistanceMap( Dungeon.hero.pos, BArray.or(Dungeon.level.passable, Dungeon.level.solid, null),
						Dungeon.isChallenged(Challenges.EVIL_MODE) ? 2 : 1 );
				for (int i = 0; i < beams; i++){

					int targetPos = Dungeon.hero.pos;
					if (i != 0){
						do {
							do {
								targetPos = Random.Int(PathFinder.distance.length);
							} while (PathFinder.distance[targetPos] == Integer.MAX_VALUE);
						} while (
								Dungeon.level.trueDistance(pos, Dungeon.hero.pos)
								> Dungeon.level.trueDistance(pos, targetPos));
					}
					targetedCells.add(targetPos);
					Ballistica b = new Ballistica(pos, targetPos, Ballistica.WONT_STOP);
					affectedCells.addAll(b.path);
				}

				//remove one beam if multiple shots would cause every cell next to the hero to be targeted
				boolean allAdjTargeted = true;
				for (int i : PathFinder.NEIGHBOURS9){
					if (!affectedCells.contains(Dungeon.hero.pos + i) && Dungeon.level.passable[Dungeon.hero.pos + i]){
						allAdjTargeted = false;
						break;
					}
				}
				if (allAdjTargeted){
					targetedCells.remove(targetedCells.size()-1);
				}
				for (int i : targetedCells){
					Ballistica b = new Ballistica(pos, i, Ballistica.WONT_STOP);
					for (int p : b.path){
						sprite.parent.add(new TargetedCell(p, 0xFF0000));
						affectedCells.add(p);
					}
				}

				//don't want to overly punish players with slow move or attack speed
				spend(GameMath.gate(TICK, (int)Math.ceil(Dungeon.hero.cooldown()), 3*TICK));
				Dungeon.hero.interrupt();

				abilityCooldown += Random.NormalFloat(MIN_ABILITY_CD, MAX_ABILITY_CD);
				abilityCooldown -= (phase - 1);
				if (buff(WarpedEnemy.BossEffect.class) != null){
					abilityCooldown = 5;
				}

			} else {
				spend(TICK);
			}

			while (summonCooldown <= 0){

				Class<?extends Mob> cls = regularSummons.remove(0);
				Mob summon = Reflection.newInstance(cls);
				regularSummons.add(cls);

				int spawnPos = -1;
				for (int i : PathFinder.NEIGHBOURS8){
					if (Actor.findChar(pos+i) == null){
						if (spawnPos == -1 || Dungeon.level.trueDistance(Dungeon.hero.pos, spawnPos) > Dungeon.level.trueDistance(Dungeon.hero.pos, pos+i)){
							spawnPos = pos + i;
						}
					}
				}

				//if no other valid spawn spots exist, try to kill an adjacent sheep to spawn anyway
				if (spawnPos == -1){
					for (int i : PathFinder.NEIGHBOURS8){
						if (Actor.findChar(pos+i) instanceof Sheep){
							if (spawnPos == -1 || Dungeon.level.trueDistance(Dungeon.hero.pos, spawnPos) > Dungeon.level.trueDistance(Dungeon.hero.pos, pos+i)){
								spawnPos = pos + i;
							}
						}
					}
					if (spawnPos != -1){
						Actor.findChar(spawnPos).die(null);
					}
				}

				if (spawnPos != -1) {
					summon.pos = spawnPos;
					GameScene.add( summon );
					Actor.add( new Pushing( summon, pos, summon.pos ) );
					summon.beckon(Dungeon.hero.pos);
					Dungeon.level.occupyCell(summon);

					summonCooldown += Random.NormalFloat(MIN_SUMMON_CD, MAX_SUMMON_CD);
					summonCooldown -= (phase - 1);
					if (findFist() != null){
						summonCooldown += MIN_SUMMON_CD - (phase - 1);
					}
				} else {
					break;
				}
			}

		}

		if (summonCooldown > 0) summonCooldown--;
		if (abilityCooldown > 0) abilityCooldown--;

		//extra fast abilities and summons at the final 100 HP
		if (phase == 5 && abilityCooldown > 2){
			abilityCooldown = 2;
		}
		if (phase == 5 && summonCooldown > 3){
			summonCooldown = 3;
		}

		return true;
	}

	@Override
	public boolean isAlive() {
		return super.isAlive() || phase != 5;
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return phase == 0 || findFist() != null || super.isInvulnerable(effect);
	}

	@Override
	public void damage( int dmg, Object src ) {

		int preHP = HP;
		if (Dungeon.isChallenged(Challenges.EVIL_MODE)){
			dmg /= Math.min(3, phase);
		}
		super.damage( dmg, src );

		if (phase == 0 || findFist() != null) return;

		if (phase < 4) {
			HP = Math.max(HP, HT - 300 * phase);
		} else if (phase == 4) {
			HP = Math.max(HP, 100);
		}
		int dmgTaken = preHP - HP;

		if (dmgTaken > 0) {
			abilityCooldown -= dmgTaken / 10f;
			summonCooldown -= dmgTaken / 10f;
		}
		if (buff(WarpedEnemy.BossEffect.class) != null){
			boolean[] FOV = new boolean[Dungeon.level.length()];
			Point c = Dungeon.level.cellToPoint(pos);
			ShadowCaster.castShadow(c.x, c.y, FOV, Dungeon.level.losBlocking, 6);

			ArrayList<Char> affected = new ArrayList<>();

			for (int i = 0; i < FOV.length; i++) {
				if (FOV[i]) {
					if (Dungeon.level.heroFOV[i] && !Dungeon.level.solid[i]) {
						//TODO better vfx?
						CellEmitter.center( i ).burst( ShadowParticle.CURSE, 8 );
					}
					Char ch = Actor.findChar(i);
					if (ch != null){
						if (ch instanceof YogDzewa || ch instanceof YogFist || ch instanceof Larva ||
							ch instanceof YogRipper || ch instanceof YogEye || ch instanceof YogScorpio) {
							continue;
						}
						affected.add(ch);
					}
				}
			}

			for (Char ch : affected){
				//4x taken damage, which falls off at a rate of 17.5% per tile of distance
				int damage = dmgTaken*4;
				damage = Math.round(damage * (1f - .175f*Dungeon.level.distance(pos, ch.pos)));
				damage -= ch.drRoll();
				ch.damage(damage, this);
				if (ch == Dungeon.hero && !ch.isAlive()) {
					Dungeon.fail(YogDzewa.class);
				}
			}
		}

		if (phase < 4 && HP <= HT - 300*phase){

			phase++;

			updateVisibility(Dungeon.level);
			GLog.n(Messages.get(this, "darkness"));
			sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "invulnerable"));

			addFist((YogFist)Reflection.newInstance(fistSummons.remove(0)));

			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
				addFist((YogFist)Reflection.newInstance(challengeSummons.remove(0)));
			}

			CellEmitter.get(Dungeon.level.exit()-1).burst(ShadowParticle.UP, 25);
			CellEmitter.get(Dungeon.level.exit()).burst(ShadowParticle.UP, 100);
			CellEmitter.get(Dungeon.level.exit()+1).burst(ShadowParticle.UP, 25);

			if (abilityCooldown < 5) abilityCooldown = 5;
			if (summonCooldown < 5) summonCooldown = 5;

		}

		LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
		if (lock != null){
			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(dmgTaken/3f);
			else                                                    lock.addTime(dmgTaken/2f);
		}

	}

	public void addFist(YogFist fist){
		fist.pos = Dungeon.level.exit();

		CellEmitter.get(Dungeon.level.exit()-1).burst(ShadowParticle.UP, 25);
		CellEmitter.get(Dungeon.level.exit()).burst(ShadowParticle.UP, 100);
		CellEmitter.get(Dungeon.level.exit()+1).burst(ShadowParticle.UP, 25);

		if (abilityCooldown < 5) abilityCooldown = 5;
		if (summonCooldown < 5) summonCooldown = 5;

		int targetPos = Dungeon.level.exit() + Dungeon.level.width();

		if (!Dungeon.isChallenged(Challenges.STRONGER_BOSSES)
				&& (Actor.findChar(targetPos) == null || Actor.findChar(targetPos) instanceof Sheep)){
			fist.pos = targetPos;
		} else if (Actor.findChar(targetPos-1) == null || Actor.findChar(targetPos-1) instanceof Sheep){
			fist.pos = targetPos-1;
		} else if (Actor.findChar(targetPos+1) == null || Actor.findChar(targetPos+1) instanceof Sheep){
			fist.pos = targetPos+1;
		} else if (Actor.findChar(targetPos) == null || Actor.findChar(targetPos) instanceof Sheep){
			fist.pos = targetPos;
		}

		if (Actor.findChar(fist.pos) instanceof Sheep){
			Actor.findChar(fist.pos).die(null);
		}

		GameScene.add(fist, 4);
		Actor.add( new Pushing( fist, Dungeon.level.exit(), fist.pos ) );
		Dungeon.level.occupyCell(fist);
	}

	public void updateVisibility( Level level ){
		int viewDistance = 4;
		if (phase > 1 && isAlive()){
			viewDistance = 4 - (phase-1);
		}
		level.viewDistance = (int)GameMath.gate(1, viewDistance, level.viewDistance);
		if (Dungeon.hero != null) {
			if (Dungeon.hero.buff(Light.class) == null) {
				Dungeon.hero.viewDistance = level.viewDistance;
			}
			Dungeon.observe();
		}
	}

	private YogFist findFist(){
		for ( Char c : Actor.chars() ){
			if (c instanceof YogFist){
				return (YogFist) c;
			}
		}
		return null;
	}

	@Override
	public void beckon( int cell ) {
	}

	@Override
	public void clearEnemy() {
		//do nothing
	}

	@Override
	public void aggro(Char ch) {
		for (Mob mob : (Iterable<Mob>)Dungeon.level.mobs.clone()) {
			if (Dungeon.level.distance(pos, mob.pos) <= 4 &&
					(mob instanceof Larva || mob instanceof YogRipper || mob instanceof YogEye || mob instanceof YogScorpio)) {
				mob.aggro(ch);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void die( Object cause ) {

		for (Mob mob : (Iterable<Mob>)Dungeon.level.mobs.clone()) {
			if (mob instanceof Larva || mob instanceof YogRipper || mob instanceof YogEye || mob instanceof YogScorpio) {
				mob.die( cause );
			}
		}

		updateVisibility(Dungeon.level);

		GameScene.bossSlain();

		if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) && Statistics.spawnersAlive == 4){
			Badges.validateBossChallengeCompleted();
		} else {
			Statistics.qualifiedForBossChallengeBadge = false;
		}
		Statistics.bossScores[4] += 5000 + 1250*Statistics.spawnersAlive;

		Dungeon.level.unseal();
		super.die( cause );
		if (Dungeon.isChallenged(Challenges.NO_LEVELS))
			new PotionOfExperience().apply(Dungeon.hero);

		yell( Messages.get(this, "defeated") );
	}

	@Override
	public void notice() {
		if (!BossHealthBar.isAssigned()) {
			BossHealthBar.assignBoss(this);
			yell(Messages.get(this, "notice"));
			for (Char ch : Actor.chars()){
				if (ch instanceof DriedRose.GhostHero){
					((DriedRose.GhostHero) ch).sayBoss();
				}
			}
			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					Music.INSTANCE.play(Assets.Music.HALLS_BOSS, true);
				}
			});
			if (phase == 0) {
				phase = 1;
				summonCooldown = Random.NormalFloat(MIN_SUMMON_CD, MAX_SUMMON_CD);
				abilityCooldown = Random.NormalFloat(MIN_ABILITY_CD, MAX_ABILITY_CD);
			}
		}
	}

	@Override
	public String description() {
		String desc = super.description();

		if (Statistics.spawnersAlive > 0){
			desc += "\n\n" + Messages.get(this, "desc_spawners");
		}

		return desc;
	}

	{
		immunities.add( Dread.class );
		immunities.add( Terror.class );
		immunities.add( Amok.class );
		immunities.add( Charm.class );
		immunities.add( Sleep.class );
		immunities.add( Vertigo.class );
		immunities.add( Frost.class );
		immunities.add( Paralysis.class );
	}

	private static final String PHASE = "phase";

	private static final String ABILITY_CD = "ability_cd";
	private static final String SUMMON_CD = "summon_cd";

	private static final String FIST_SUMMONS = "fist_summons";
	private static final String REGULAR_SUMMONS = "regular_summons";
	private static final String CHALLENGE_SUMMONS = "challenges_summons";

	private static final String TARGETED_CELLS = "targeted_cells";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(PHASE, phase);

		bundle.put(ABILITY_CD, abilityCooldown);
		bundle.put(SUMMON_CD, summonCooldown);

		bundle.put(FIST_SUMMONS, fistSummons.toArray(new Class[0]));
		bundle.put(CHALLENGE_SUMMONS, challengeSummons.toArray(new Class[0]));
		bundle.put(REGULAR_SUMMONS, regularSummons.toArray(new Class[0]));

		int[] bundleArr = new int[targetedCells.size()];
		for (int i = 0; i < targetedCells.size(); i++){
			bundleArr[i] = targetedCells.get(i);
		}
		bundle.put(TARGETED_CELLS, bundleArr);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		phase = bundle.getInt(PHASE);
		if (phase != 0) {
			BossHealthBar.assignBoss(this);
			if (phase == 5) BossHealthBar.bleed(true);
		}

		abilityCooldown = bundle.getFloat(ABILITY_CD);
		summonCooldown = bundle.getFloat(SUMMON_CD);

		fistSummons.clear();
		Collections.addAll(fistSummons, bundle.getClassArray(FIST_SUMMONS));
		challengeSummons.clear();
		Collections.addAll(challengeSummons, bundle.getClassArray(CHALLENGE_SUMMONS));
		regularSummons.clear();
		Collections.addAll(regularSummons, bundle.getClassArray(REGULAR_SUMMONS));

		for (int i : bundle.getIntArray(TARGETED_CELLS)){
			targetedCells.add(i);
		}
	}

	public static class Larva extends Mob {

		{
			spriteClass = LarvaSprite.class;

			HP = HT = 20;
			if (Dungeon.isChallenged(Challenges.EVIL_MODE)){
				HP = HT = 60;
			}
			defenseSkill = 12;
			viewDistance = Light.DISTANCE;

			EXP = 5;
			maxLvl = -2;

			properties.add(Property.DEMONIC);
			properties.add(Property.BOSS_MINION);
		}

		@Override
		public int attackSkill( Char target ) {
			return 30;
		}

		@Override
		public float attackDelay() {
			if (Dungeon.isChallenged(Challenges.EVIL_MODE)){
				return super.attackDelay()*0.5f;
			}
			return super.attackDelay();
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 15, 25 );
		}

		@Override
		public int drRoll() {
			return super.drRoll() + Random.NormalIntRange(0, 4);
		}

	}

	//used so death to yog's ripper demons have their own rankings description
	public static class YogRipper extends RipperDemon {
		{
			maxLvl = -2;
			properties.add(Property.BOSS_MINION);
		}
	}
	public static class YogEye extends Eye {
		{
			maxLvl = -2;
			properties.add(Property.BOSS_MINION);
		}
	}
	public static class YogScorpio extends Scorpio {
		{
			maxLvl = -2;
			properties.add(Property.BOSS_MINION);
		}
	}
}
