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

package com.zrp200.rkpd2.items.wands;

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.ConfusionGas;
import com.zrp200.rkpd2.actors.blobs.Fire;
import com.zrp200.rkpd2.actors.blobs.ParalyticGas;
import com.zrp200.rkpd2.actors.blobs.Regrowth;
import com.zrp200.rkpd2.actors.blobs.ToxicGas;
import com.zrp200.rkpd2.actors.buffs.BlobImmunity;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.Frost;
import com.zrp200.rkpd2.actors.buffs.Hex;
import com.zrp200.rkpd2.actors.buffs.Recharging;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.mobs.GoldenMimic;
import com.zrp200.rkpd2.actors.mobs.Mimic;
import com.zrp200.rkpd2.actors.mobs.npcs.Sheep;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.MagicMissile;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.particles.ShadowParticle;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.AlchemistsToolkit;
import com.zrp200.rkpd2.items.artifacts.Artifact;
import com.zrp200.rkpd2.items.artifacts.ChaliceOfBlood;
import com.zrp200.rkpd2.items.artifacts.EtherealChains;
import com.zrp200.rkpd2.items.artifacts.HornOfPlenty;
import com.zrp200.rkpd2.items.artifacts.MasterThievesArmband;
import com.zrp200.rkpd2.items.artifacts.SandalsOfNature;
import com.zrp200.rkpd2.items.artifacts.SoulOfYendor;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.artifacts.UnstableSpellbook;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.scrolls.ScrollOfMirrorImage;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfSirensSong;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.traps.CursingTrap;
import com.zrp200.rkpd2.levels.traps.ShockingTrap;
import com.zrp200.rkpd2.levels.traps.SummoningTrap;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Languages;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.InterlevelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.TargetHealthIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndOptions;

import java.io.IOException;
import java.util.ArrayList;

//helper class to contain all the cursed wand zapping logic, so the main wand class doesn't get huge.
public class CursedWand {

	public static int eldritchLevel = 0;

	private static float COMMON_CHANCE = 0.6f;
	private static float UNCOMMON_CHANCE = 0.3f;
	private static float RARE_CHANCE = 0.09f;
	private static float VERY_RARE_CHANCE = 0.01f;

	public static void cursedZap(final Item origin, final Char user, final Ballistica bolt, final Callback afterZap){

		cursedFX(user, bolt, new Callback() {
			@Override
			public void call() {
				if (cursedEffect(origin, user, bolt.collisionPos)){
					if (afterZap != null) afterZap.call();
				}
			}
		});
	}

	public static void tryForWandProc( Char target, Item origin ){
		if (target != null && origin instanceof Wand){
			((Wand) origin).wandProc(target, 1);
		}
	}

	public static boolean cursedEffect(final Item origin, final Char user, final Char target){
		return cursedEffect(origin, user, target.pos);
	}

	public static boolean cursedEffect(final Item origin, final Char user, final int targetPos){
		if (!createOmniArtifact(targetPos, origin)) {

			float[] chances = {COMMON_CHANCE, UNCOMMON_CHANCE, RARE_CHANCE, VERY_RARE_CHANCE};

			if (eldritchLevel > 1) {
				chances[0] = 0.52f;
				chances[1] = 0.36f;
				chances[2] = 0.108f;
				chances[3] = 0.012f;
			}

			switch (Random.chances(chances)) {
				case 0:
				default:
					return commonEffect(origin, user, targetPos);
				case 1:
					return uncommonEffect(origin, user, targetPos);
				case 2:
					return rareEffect(origin, user, targetPos);
				case 3:
					return veryRareEffect(origin, user, targetPos);
			}
		}
		return true;
	}

	private static boolean commonEffect(final Item origin, final Char user, final int targetPos){
		switch(Random.Int(4)){

			//anti-entropy
			case 0: default:
				Char target = Actor.findChar(targetPos);
				if (user == null) return cursedEffect(origin, null, targetPos);
				if (Random.Int(2) == 0) {
					if (target != null) Buff.affect(target, Burning.class).reignite(target,
							Burning.DURATION + eldritchLevel > 1 ? 5 : 0);
					float duration = Frost.DURATION;
					if (eldritchLevel > 0) duration /= 2;
					if (eldritchLevel < 3)
						Buff.affect(user, Frost.class, duration);
				} else {
					float duration = Burning.DURATION;
					if (eldritchLevel > 0) duration /= 2;
					if (eldritchLevel < 3)
						Buff.affect(user, Burning.class).reignite(user, duration);
					if (target != null) Buff.affect(target, Frost.class,
							Frost.DURATION + eldritchLevel > 1 ? 5 : 0);
				}
				tryForWandProc(target, origin);
				return true;

			//spawns some regrowth
			case 1:
				GameScene.add( Blob.seed(targetPos, 30 + eldritchLevel > 1 ? 10 : 0, Regrowth.class));
				tryForWandProc(Actor.findChar(targetPos), origin);
				return true;

			//random teleportation
			case 2:
				if(eldritchLevel < 3 && Random.Int(2 + eldritchLevel > 0 ? 2 : 0) == 0) {
					if (user != null && !user.properties().contains(Char.Property.IMMOVABLE)) {
						ScrollOfTeleportation.teleportChar(user);
					} else {
						return cursedEffect(origin, user, targetPos);
					}
				} else {
					Char ch = Actor.findChar( targetPos );
					if (ch != null && !ch.properties().contains(Char.Property.IMMOVABLE)) {
						ScrollOfTeleportation.teleportChar(ch);
						tryForWandProc(ch, origin);
					} else {
						return cursedEffect(origin, user, targetPos);
					}
				}
				return true;

			//random gas at location
			case 3:
				Sample.INSTANCE.play( Assets.Sounds.GAS );
				tryForWandProc(Actor.findChar(targetPos), origin);
				if (eldritchLevel > 0){
					Buff.affect(user, BlobImmunity.class, 5f);
				}
				switch (Random.Int(3)) {
					case 0: default:
						GameScene.add( Blob.seed( targetPos, 800 + eldritchLevel > 1 ? 100 : 0, ConfusionGas.class ) );
						return true;
					case 1:
						GameScene.add( Blob.seed( targetPos, 500 + eldritchLevel > 1 ? 80 : 0, ToxicGas.class ) );
						return true;
					case 2:
						GameScene.add( Blob.seed( targetPos, 200 + eldritchLevel > 1 ? 50 : 0, ParalyticGas.class ) );
						return true;
				}
		}

	}

	private static boolean uncommonEffect(final Item origin, final Char user, final int targetPos){
		switch(Random.Int(4)){

			//Random plant
			case 0: default:
				int pos = targetPos;

				if (Dungeon.level.map[pos] != Terrain.ALCHEMY
						&& !Dungeon.level.pit[pos]
						&& Dungeon.level.traps.get(pos) == null
						&& !Dungeon.isChallenged(Challenges.NO_HERBALISM)) {
					Dungeon.level.plant((Plant.Seed) Generator.randomUsingDefaults(Generator.Category.SEED), pos);
					tryForWandProc(Actor.findChar(pos), origin);
				} else {
					return cursedEffect(origin, user, targetPos);
				}

				return true;

			//Health transfer
			case 1:
				final Char target = Actor.findChar( targetPos );
				if (user != null && target != null) {
					int damage = Dungeon.scalingDepth() * 2;
					Char toHeal, toDamage;

					if (eldritchLevel >= 3 || Random.Int(2 + eldritchLevel) == 0){
						toHeal = user;
						toDamage = target;
					} else {
						toHeal = target;
						toDamage = user;
					}
					toHeal.HP = Math.min(toHeal.HT, toHeal.HP + damage);
					toHeal.sprite.emitter().burst(Speck.factory(Speck.HEALING), 3);
					toHeal.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(damage), FloatingText.HEALING );

					toDamage.damage(damage, new CursedWand());
					toDamage.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10);

					if (toDamage == Dungeon.hero){
						Sample.INSTANCE.play(Assets.Sounds.CURSED);
						if (!toDamage.isAlive()) {
							if (user == Dungeon.hero && origin != null) {
								Badges.validateDeathFromFriendlyMagic();
								Dungeon.fail( origin );
								GLog.n( Messages.get( CursedWand.class, "ondeath", origin.name() ) );
							} else {
								Badges.validateDeathFromEnemyMagic();
								Dungeon.fail( toHeal );
							}
						}
					} else {
						Sample.INSTANCE.play(Assets.Sounds.BURNING);
					}
					tryForWandProc(target, origin);
				} else {
					return cursedEffect(origin, user, targetPos);
				}
				return true;

			//Bomb explosion
			case 2:
				new Bomb.ConjuredBomb().explode(targetPos);
				tryForWandProc(Actor.findChar(targetPos), origin);
				return true;

			//shock and recharge
			case 3:
				if (user == null) return cursedEffect(origin, null, targetPos);
				if (eldritchLevel < 3) new ShockingTrap().set( user.pos ).activate();
				Buff.prolong(user, Recharging.class, Recharging.DURATION + eldritchLevel > 1 ? 10 : 0);
				ScrollOfRecharging.charge(user);
				SpellSprite.show(user, SpellSprite.CHARGE);
				return true;
		}

	}

	private static boolean rareEffect(final Item origin, final Char user, final int targetPos){
		switch(Random.Int(4)){

			//sheep transformation
			case 0: default:

				Char ch = Actor.findChar( targetPos );
				if (ch != null && !(ch instanceof Hero)
						&& !ch.properties().contains(Char.Property.BOSS)
						&& !ch.properties().contains(Char.Property.MINIBOSS)){
					Sheep sheep = new Sheep();
					sheep.lifespan = 10;
					sheep.pos = ch.pos;
					ch.destroy();
					ch.sprite.killAndErase();
					Dungeon.level.mobs.remove(ch);
					TargetHealthIndicator.instance.target(null);
					GameScene.add(sheep);
					CellEmitter.get(sheep.pos).burst(Speck.factory(Speck.WOOL), 4);
					Sample.INSTANCE.play(Assets.Sounds.PUFF);
					Sample.INSTANCE.play(Assets.Sounds.SHEEP);
				} else {
					return cursedEffect(origin, user, targetPos);
				}
				return true;

			//curses!
			case 1:
				if (eldritchLevel >= 3){
					ch = Actor.findChar( targetPos );
					if (ch != null){
						Buff.affect(ch, Hex.class, Hex.DURATION);
					}
					return true;
				}
				if (user instanceof Hero && eldritchLevel < 1) {
					CursingTrap.curse( (Hero) user );
				} else {
					return cursedEffect(origin, user, targetPos);
				}
				return true;

			//inter-level teleportation
			case 2:
				if (user == null) return cursedEffect(origin, null, targetPos);
				if (eldritchLevel < 3 && Dungeon.depth > 1 && Dungeon.interfloorTeleportAllowed() && user == Dungeon.hero) {

					//each depth has 1 more weight than the previous depth.
					float[] depths = new float[Dungeon.depth -1];
					for (int i = 1; i < Dungeon.depth; i++) depths[i-1] = i;
					int depth = 1+Random.chances(depths);

					Level.beforeTransition();
					InterlevelScene.mode = InterlevelScene.Mode.RETURN;
					InterlevelScene.returnDepth = depth;
					InterlevelScene.returnBranch = 0;
					InterlevelScene.returnPos = -1;
					Game.switchScene(InterlevelScene.class);

				} else {
					ScrollOfTeleportation.teleportChar(user);

				}
				return true;

			//summon monsters
			case 3:
				if (eldritchLevel >= 3 && user == Dungeon.hero){
					ScrollOfMirrorImage.spawnImages(Dungeon.hero, 2);
				} else {
					new SummoningTrap().set(targetPos).activate();
				}
				return true;
		}
	}

	public static boolean disableCursedWandError = false;

	private static boolean veryRareEffect(final Item origin, final Char user, final int targetPos){
		switch(Random.Int(4)){

			//great forest fire!
			case 0: default:
				if (user == null) return cursedEffect(origin, null, targetPos);
				for (int i = 0; i < Dungeon.level.length(); i++){
					int amount = 15;
					if (eldritchLevel > 0) amount /= 3;
					GameScene.add( Blob.seed(i, amount, Regrowth.class));
				}
				if (eldritchLevel <= 3) {
					do {
						int amount = 10;
						if (eldritchLevel > 0) amount /= 3;
						GameScene.add(Blob.seed(Dungeon.level.randomDestination(null), amount, Fire.class));
					} while (Random.Int(5) != 0);
				}
				new Flare(8, 32).color(0xFFFF66, true).show(user.sprite, 2f);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				GLog.p(Messages.get(CursedWand.class, "grass"));
				GLog.w(Messages.get(CursedWand.class, "fire"));
				return true;

			//golden mimic
			case 1:

				Char ch = Actor.findChar(targetPos);
				int spawnCell = targetPos;
				if (ch != null){
					ArrayList<Integer> candidates = new ArrayList<Integer>();
					for (int n : PathFinder.NEIGHBOURS8) {
						int cell = targetPos + n;
						if (Dungeon.level.passable[cell] && Actor.findChar( cell ) == null) {
							candidates.add( cell );
						}
					}
					if (!candidates.isEmpty()){
						spawnCell = Random.element(candidates);
					} else {
						return cursedEffect(origin, user, targetPos);
					}
				}

				Mimic mimic = Mimic.spawnAt(spawnCell, GoldenMimic.class, false);
				mimic.stopHiding();
				mimic.alignment = Char.Alignment.ENEMY;
				//play vfx/sfx manually as mimic isn't in the scene yet
				Sample.INSTANCE.play(Assets.Sounds.MIMIC, 1, 0.85f);
				CellEmitter.get(mimic.pos).burst(Speck.factory(Speck.STAR), 10);
				mimic.items.clear();

				if (eldritchLevel >= 3){
					Buff.affect(mimic, ScrollOfSirensSong.Enthralled.class);
				} else {
					Item reward;
					do {
						reward = Generator.randomUsingDefaults(Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR,
								Generator.Category.RING, Generator.Category.WAND));
						if (!Dungeon.isChallenged(Challenges.REDUCED_POWER)) break;
					} while (reward.level() < 1);
					mimic.items.add(reward);
				}
				GameScene.add(mimic);
				return true;

			//crashes the game, yes, really.
			case 2:
				
				try {
					Dungeon.saveAll();
					if(disableCursedWandError || Messages.lang() != Languages.ENGLISH){
						//Don't bother doing this joke to none-english speakers, I doubt it would translate.
						return cursedEffect(origin, user, targetPos);
					} else {
						ShatteredPixelDungeon.runOnRenderThread(
								new Callback() {
									@Override
									public void call() {
										GameScene.show(
												new WndOptions(Icons.get(Icons.WARNING),
														"CURSED WAND ERROR",
														"this application will now self-destruct",
														"abort",
														"retry",
														"fail") {

													@Override
													protected void onSelect(int index) {
														Game.instance.finish();
													}

													@Override
													public void onBackPressed() {
														//do nothing
													}
												}
										);
									}
								}
						);
						return false;
					}
				} catch(IOException e){
					ShatteredPixelDungeon.reportException(e);
					//maybe don't kill the game if the save failed.
					return cursedEffect(origin, user, targetPos);
				}

			//random transmogrification
			case 3:
				if (eldritchLevel >= 3){
					GameScene.show(new ScrollOfMetamorphosis.WndMetamorphChoose());
					return true;
				}

				//skips this effect if there is no item to transmogrify
				if (origin == null || origin.unique || user != Dungeon.hero || !Dungeon.hero.belongings.contains(origin) || origin.isEquipped(Dungeon.hero) || eldritchLevel > 1){
					return cursedEffect(origin, user, targetPos);
				}
				origin.detach(Dungeon.hero.belongings.backpack);
				Item result;
				do {
					result = Generator.randomUsingDefaults(Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR,
							Generator.Category.RING, Generator.Category.ARTIFACT));
				} while (result.cursed);
				if (result.isUpgradable() && !Dungeon.isChallenged(Challenges.REDUCED_POWER)) result.upgrade();
				result.cursed = result.cursedKnown = true;
				if (origin instanceof Wand){
					GLog.w( Messages.get(CursedWand.class, "transmogrify_wand") );
				} else {
					GLog.w( Messages.get(CursedWand.class, "transmogrify_other") );
				}
				Dungeon.level.drop(result, user.pos).sprite.drop();
				return true;
		}
	}

	private static boolean createOmniArtifact(int pos, Item origin){

		for (int i : PathFinder.NEIGHBOURS4){
			Heap heap = Dungeon.level.heaps.get(pos + i);
			if (heap == null && !Dungeon.level.solid[pos + i]){
				pos = pos + i;
				break;
			}
		}

		ArrayList<Class<? extends Artifact>> artifacts = new ArrayList<>();
		ArrayList<Integer> artifactLevels = new ArrayList<>();
		artifacts.add(EtherealChains.class);
		artifacts.add(AlchemistsToolkit.class);
		artifacts.add(HornOfPlenty.class);
		artifacts.add(ChaliceOfBlood.class);
		artifacts.add(MasterThievesArmband.class);
		artifacts.add(SandalsOfNature.class);
		artifacts.add(TimekeepersHourglass.class);
		artifacts.add(UnstableSpellbook.class);
		boolean circleOfItems = true;
		for (int i : PathFinder.NEIGHBOURS8){
			Heap heap = Dungeon.level.heaps.get(pos + i);
			if (heap == null) circleOfItems = false;
		}
		if (circleOfItems){
			for (int i : PathFinder.NEIGHBOURS8){
				Heap heap = Dungeon.level.heaps.get(pos + i);
				if (heap != null) scanForArtifact(heap, artifacts, artifactLevels);
			}
		} else {
			GLog.i(Messages.get(CursedWand.class, "nothing"));
			return false;
		}
		if (artifacts.isEmpty()){
			int averageLevel = 0;
			for (int i : artifactLevels){
				averageLevel += i;
			}
			averageLevel /= 8;
			Dungeon.level.drop(new SoulOfYendor().upgrade(averageLevel), pos).sprite.drop();
			GameScene.flash(0xFFFFFF);
			Sample.INSTANCE.play(Assets.Sounds.BOSS);
			origin.detach(Dungeon.hero.belongings.backpack);
		} else {
			GLog.i(Messages.get(CursedWand.class, "nothing"));
			return false;
		}
		return true;
	}

	private static void scanForArtifact(Heap heap, ArrayList<Class<? extends Artifact>> requiredArtifacts,
										ArrayList<Integer> artifactLevels){
		for (Item item: heap.items){
			for (Class<? extends Artifact> artifactClass : requiredArtifacts){
				if (artifactClass.isAssignableFrom(item.getClass())){
					heap.items.remove(item);
					artifactLevels.add(item.level());
					if (heap.items.isEmpty()) {
						heap.destroy();
					}
					requiredArtifacts.remove(artifactClass);
					break;
				}
			}
		}
	}

	private static void cursedFX(final Char user, final Ballistica bolt, final Callback callback){
		MagicMissile.boltFromChar( user.sprite.parent,
				MagicMissile.RAINBOW,
				user.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}

}
