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

package com.zrp200.rkpd2.items.weapon;

import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.PowerfulDegrade;
import com.zrp200.rkpd2.actors.buffs.Preparation;
import com.zrp200.rkpd2.actors.buffs.RevealedArea;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.NaturesPower;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.effects.particles.LeafParticle;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.ScoutArmor;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.items.rings.RingOfSharpshooting;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfEnchantment;
import com.zrp200.rkpd2.items.weapon.enchantments.Blocking;
import com.zrp200.rkpd2.items.weapon.enchantments.Explosive;
import com.zrp200.rkpd2.items.weapon.enchantments.Lucky;
import com.zrp200.rkpd2.items.weapon.enchantments.Unstable;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.mechanics.Ballistica;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Blindweed;
import com.zrp200.rkpd2.plants.Firebloom;
import com.zrp200.rkpd2.plants.Icecap;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.plants.Sorrowmoss;
import com.zrp200.rkpd2.plants.Stormvine;
import com.zrp200.rkpd2.scenes.CellSelector;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.MissileSprite;
import com.zrp200.rkpd2.ui.QuickSlotButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SpiritBow extends Weapon implements BrawlerBuff.BrawlerWeapon {
	
	public static final String AC_SHOOT		= "SHOOT";
	
	{
		image = ItemSpriteSheet.SPIRIT_BOW;

		defaultAction = AC_SHOOT;
		usesTargeting = true;
		
		unique = true;
		bones = false;
	}

	/// enchanting logic ///
	public static final Set<Class<? extends Enchantment>> REMOVED_ENCHANTS = Collections.unmodifiableSet(new HashSet(Arrays.asList(
			Blocking.class, Lucky.class
	)));

	private final static float[] typeChances = { 50, 40, 15 }; // +50% rare enchants
	public static Enchantment randomUncommonEnchant(Class<?extends Enchantment>... toIgnore) { // an override for the static Weapon.randomUncommon.
		boolean addExplosive = true;
		for(Class cls : toIgnore) if(cls == Explosive.class) {
			addExplosive = false;
			break;
		}
		while(true) {
			Enchantment enchantment = Enchantment.randomUncommon(toIgnore);
			if(REMOVED_ENCHANTS.contains(enchantment.getClass())) {
				if(addExplosive && Random.Int(2) == 0) return new Explosive();
				else continue;
			}
			return enchantment;
		}
	}

	// an override for Weapon.Enchantment.random()
	public static Enchantment randomEnchantment(Class<? extends Enchantment>... toIgnore) {
		switch ( Random.chances(typeChances) ) {
			case 0: return Enchantment.randomCommon(toIgnore);
			case 1: return randomUncommonEnchant(toIgnore); // using the custom logic.
			default: return Enchantment.randomRare(toIgnore);
		}
	}

	@Override public Weapon enchant() {
		@SuppressWarnings("unchecked")
		Enchantment newEnchant = randomEnchantment(enchantment != null ? enchantment.getClass() : null);
		return enchant(newEnchant);
	}
	///

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if(hero == null || !hero.heroClass.isExact(HeroClass.HUNTRESS)) actions.remove(AC_EQUIP);
		actions.add(AC_SHOOT);
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		
		super.execute(hero, action);
		
		if (action.equals(AC_SHOOT)) {
			
			curUser = hero;
			curItem = this;
			GameScene.selectCell( shooter );
			
		}
	}

	private static Class[] harmfulPlants = new Class[]{
			Blindweed.class, Firebloom.class, Icecap.class, Sorrowmoss.class,  Stormvine.class
	};

	@Override
	public String info() {
		String info = desc();
		
		info += "\n\n" + Messages.get( SpiritBow.class, "stats",
				Math.round(augment.damageFactor(min())),
				Math.round(augment.damageFactor(max())),
				STRReq());
		
		if (STRReq() > Dungeon.hero.STR()) {
			info += " " + Messages.get(Weapon.class, "too_heavy");
		} else if (Dungeon.hero.STR() > STRReq()){
			info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
		}
		
		switch (augment) {
			case SPEED:
				info += "\n\n" + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += "\n\n" + Messages.get(Weapon.class, "stronger");
				break;
			case NONE:
		}

		if (enchantment != null && (cursedKnown || !enchantment.curse())){
			info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()));
			if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
			info += " " + enchantment.desc();
		} else if (enchantHardened){
			info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
		}
		
		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Weapon.class, "cursed");
		} else if (!isIdentified() && cursedKnown){
			info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
		}
		
		info += "\n\n" + Messages.get(MissileWeapon.class, "distance");
		
		return info;
	}
	
	@Override
	public int STRReq(int lvl) {
		return STRReq(1, lvl); //tier 1
	}
	
	@Override
	public int min(int lvl) {
		return Math.max(0,1 + level() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero));
	}
	
	@Override
	public int max(int lvl) {
		return Math.max(0, 6 + (int)(2*(internalLevel() + RingOfSharpshooting.levelDamageBonus(Dungeon.hero))));
	}

	@Override
	public int targetingPos(Hero user, int dst) {
		return knockArrow().targetingPos(user, dst);
	}

	public class SuperShot extends SpiritArrow{
		{
			hitSound = Assets.Sounds.HIT_STRONG;
		}

		@Override
		public int image() {
			return ItemSpriteSheet.SR_RANGED;
		}

		@Override
		public int damageRoll(Char owner) {
			int damage = SpiritBow.this.damageRoll(owner);
			int distance = Dungeon.level.distance(owner.pos, targetPos) - 1;
			float multiplier = Math.min(ScoutArmor.maxDamage(), ScoutArmor.startingBoost() * (float)Math.pow(ScoutArmor.distanceMultiplier(), distance));
			damage = Math.round(damage * multiplier);
			return damage;
		}

		@Override
		public float baseDelay(Char user) {
			return SpiritBow.this.baseDelay(user) * 2f;
		}

		@Override
		public void onThrow(int cell) {
			super.onThrow(cell);
			superShot = false;
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			if (Dungeon.hero.hasTalent(Talent.IVYLASH)){
				Buff.affect(defender, Roots.class, Dungeon.hero.pointsInTalent(Talent.IVYLASH) + baseDelay(attacker));
			}
			if (Dungeon.hero.hasTalent(Talent.SCOUTS_BARRIER)){
				Buff.affect(attacker, Barrier.class).incShield(2 + Dungeon.hero.pointsInTalent(Talent.SCOUTS_BARRIER)*2);
			}
			// TODO: make god slayer inferno an enchantment
//			if (Dungeon.hero.hasTalent(Talent.SCOURGING_THE_UNIVERSE)
//					&& Dungeon.hero.belongings.armor instanceof ScoutArmor
//					&& Random.Float() < (1 + Dungeon.hero.pointsInTalent(Talent.SCOURGING_THE_UNIVERSE))/
//					(10f + (Dungeon.hero.pointsInTalent(Talent.SCOURGING_THE_UNIVERSE)-1)*2)){
//				Buff.affect(defender, GodSlayerBurning.class).reignite(defender, Random.IntRange(2, 3));
//			}
			return super.proc(attacker, defender, damage);
		}
	}

	private int targetPos;

	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll(owner));
		
		if (owner instanceof Hero) {
			int exStr = ((Hero)owner).STR() - STRReq();
			if (exStr > 0) {
				damage += Random.IntRange( 0, exStr );
			}
		}

		return damage;
	}


	@Override
	public int level() {
		if (Dungeon.hero.buff(PowerfulDegrade.class) != null) return 0;
		return (int)internalLevel();
	}
	// this allows me to more dynamically adjust it by class.
	private double internalLevel() {
		double level = curseInfusionBonus ? 1 : 0;
		if(Dungeon.hero == null) return level;
		double rate = 30d/(Dungeon.hero.heroClass.isExact(HeroClass.HUNTRESS) ? 10 : 6);
		return level + Dungeon.hero.lvl / rate;
	}

	@Override
	public int buffedLvl() {
		if (Dungeon.hero.buff(PowerfulDegrade.class) != null) return 0;
		//level isn't affected by buffs/debuffs
		return level();
	}

	// huntress can enchant with scrolls of upgrade
	@Override
	public boolean isUpgradable() {
		return Dungeon.hero != null && Dungeon.hero.heroClass.isExact(HeroClass.HUNTRESS);
	}
	@Override
	public Item upgrade(boolean enchant) {
		new ScrollOfEnchantment().enchantWeapon(this);
		return this;
	}

	public SpiritArrow knockArrow(){
		if (superShot){
			return new SuperShot();
		}
		return new SpiritArrow();
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		SpiritBow.SpiritArrow arrow = knockArrow(); // need a unique arrow for every character.
		arrow.sniperSpecial = true; // :D

		int cell = QuickSlotButton.autoAim(enemy, arrow);

		int points = Dungeon.hero.pointsInTalent(Talent.SHARED_UPGRADES, Talent.RK_SNIPER);
		if(Dungeon.hero.canHaveTalent(Talent.SHARED_UPGRADES)) points++; // free +1.
		arrow.sniperSpecialBonusDamage = level()*points/10f;

		Buff.detach(Dungeon.hero, Preparation.class); // nope!

		arrow.cast(Dungeon.hero, cell);
		return 0;
	}

	public int shotCount; // used for sniper specials
	public boolean bounced = false;
	public class SpiritArrow extends MissileWeapon {

		{
			image = ItemSpriteSheet.SPIRIT_ARROW;

			hitSound = Assets.Sounds.HIT_ARROW;
		}

		public boolean sniperSpecial = false;
		public float sniperSpecialBonusDamage = 0f;
		public boolean doNotDelay = false;

		@Override
		public Emitter emitter() {
			if (Dungeon.hero.buff(NaturesPower.naturesPowerTracker.class) != null && !sniperSpecial){
				Emitter e = new Emitter();
				e.pos(5, 5);
				e.fillTarget = false;
				e.pour(LeafParticle.GENERAL, 0.01f);
				return e;
			} else {
				return super.emitter();
			}
		}

		@Override
		public int damageRoll(Char owner) {
			int damage = SpiritBow.this.damageRoll(owner);
			if (sniperSpecial){
				damage = Math.round(damage * (1f + sniperSpecialBonusDamage));
				switch (SpiritBow.this.augment) {
					case NONE:
						damage = Math.round(damage * 0.667f);
						break;
					case SPEED:
						damage = Math.round(damage * 0.5f);
						break;
					case DAMAGE:
						//as distance increases so does damage, capping at 3x:
						//1.20x|1.35x|1.52x|1.71x|1.92x|2.16x|2.43x|2.74x|3.00x
						int distance = Dungeon.level.distance(owner.pos, targetPos) - 1;
						float multiplier = Math.min(3f, 1.2f * (float) Math.pow(1.125f, distance));
						damage = Math.round(damage * multiplier);
						break;
				}
			}
			if (bounced) {
				switch (Dungeon.hero.pointsInTalent(Talent.ARCHERY_MARK)){
					case 1: default:
						return (int) (damage*0.2f);
					case 2:
						return (int) (damage*0.4f);
					case 3:
						return (int) (damage*0.6f);
				}
			}
			return damage;
		}

		@Override
		public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
			return SpiritBow.this.hasEnchant(type, owner);
		}

		@Override
		public int proc(Char attacker, Char defender, int damage) {
			if (attacker.buff(NaturesPower.naturesPowerTracker.class) != null && !sniperSpecial){

				Actor.add(new Actor() {
					{
						actPriority = VFX_PRIO;
					}

					@Override
					protected boolean act() {

						if (Random.Int( ((Hero)attacker).hasTalent(Talent.NATURES_WRATH) ? 10 : 12)
								< ((Hero)attacker).pointsInTalent(Talent.NATURES_WRATH, Talent.SILVA_RANGE)){
							Plant plant = (Plant) Reflection.newInstance(Random.element(harmfulPlants));
							plant.pos = defender.pos;
							plant.activate( defender.isAlive() ? defender : null );
						}

						if (!defender.isAlive()){
							NaturesPower.naturesPowerTracker tracker = attacker.buff(NaturesPower.naturesPowerTracker.class);
							if (tracker != null){
								if (Dungeon.hero.canHaveTalent(Talent.WILD_MOMENTUM))
									tracker.extend(((Hero) attacker).shiftedPoints(Talent.WILD_MOMENTUM));
								else
									tracker.extend(((Hero) attacker).pointsInTalent(Talent.SILVA_RANGE));
							}
						}

						Actor.remove(this);
						return true;
					}
				});

			}
			return SpiritBow.this.proc(attacker, defender, damage);
		}

		@Override
		public void onRangedAttack(Char enemy, int cell, boolean hit) { } // does nothing.

		@Override
		public float baseDelay(Char user) {
			if (bounced) return 0f;
			if(sniperSpecial) {
				switch (SpiritBow.this.augment) {
					case NONE:
					default:
						return 0f;
					case SPEED:
						return 1f;
					case DAMAGE:
						return 2f;
				}
			}
			else return SpiritBow.this.baseDelay(user);
		}
		@Override
		protected float speedMultiplier(Char owner) {
			float speed = SpiritBow.this.speedMultiplier(owner);
			if (owner.buff(NaturesPower.naturesPowerTracker.class) != null){
				// +33% speed to ~~+50%~~ +56% speed, depending on talent points
				speed += ((Dungeon.hero.canHaveTalent(Talent.GROWING_POWER) ? 6 : 8
						+ ((Hero)owner).pointsInTalent(Talent.GROWING_POWER)) /
						(Dungeon.hero.canHaveTalent(Talent.GROWING_POWER) ? 18f : 24f));
			}
			return speed;
		}
		
		@Override
		public float accuracyFactor(Char owner, Char target) {
			if (sniperSpecial && SpiritBow.this.augment == Augment.DAMAGE){
				return Float.POSITIVE_INFINITY;
			} else {
				return super.accuracyFactor(owner, target);
			}
		}
		
		@Override
		public int STRReq(int lvl) {
			return SpiritBow.this.STRReq();
		}

		@Override
		public void onThrow(int cell) {
			Char enemy = Actor.findChar( cell );
			boolean hitGround;
			if (enemy == null || enemy == curUser) {
				parent = null;
				hitGround = true;
			} else {
				hitGround = !curUser.shoot( enemy, this );
				if (Dungeon.hero.hasTalent(Talent.ARCHERY_MARK)){
					if (!bounced) {
						Mob[] mobs = Dungeon.level.mobs.toArray(new Mob[0]);
						int targetPos = Integer.MAX_VALUE - 1;
						Mob desiredMob = null;
						for (Mob m : mobs) {
							if (new Ballistica(cell, m.pos, Ballistica.PROJECTILE).collisionPos == m.pos
									&& Dungeon.level.trueDistance(cell, m.pos) <= Dungeon.level.trueDistance(cell, targetPos) && m != enemy
									&& m.alignment == Char.Alignment.ENEMY && !m.isInvulnerable(getClass())) {
								targetPos = m.pos;
								desiredMob = m;
							}
						}
						if (targetPos == Integer.MAX_VALUE - 1){
							Dungeon.hero.spendAndNext(0f);
							return;
						} else {
							bounced = true;
							Mob finalDesiredMob = desiredMob;
							CellEmitter.center(enemy.pos).parent.recycle(MissileSprite.class).
									reset(enemy.pos,
											desiredMob.sprite,
											this,
											new Callback() {
												@Override
												public void call() {
													curUser = Dungeon.hero;
													curUser.shoot(finalDesiredMob, knockArrow());
//												if ((archeryCount >= (Dungeon.hero.pointsInTalent(Talent.ARCHERY_MARK) != 1 ? 2 : 1))){
//													Dungeon.hero.spendAndNext(castDelay(Dungeon.hero, cell));
//													archeryCount = 0;
//												}
												}
											});
						}
					}
				}
			}
			if(hitGround) {
				Splash.at(cell, 0xCC99FFFF, 1);
				if((hasEnchant(Explosive.class,curUser)
						|| (hasEnchant(Unstable.class,curUser)
						&& Unstable.getRandomEnchant(SpiritBow.this) instanceof Explosive))
						&& new Explosive().tryProc(curUser, SpiritBow.this.buffedLvl()))
					new Bomb().explode(cell);
				curUser.ready();
			}
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play( Assets.Sounds.ATK_SPIRITBOW, 1, Random.Float(0.87f, 1.15f) );
		}

		int flurryCount = -1;
		Actor flurryActor = null;

		@Override
		public void cast(final Hero user, final int dst) {
			final int cell = throwPos( user, dst );
			SpiritBow.this.targetPos = cell;
			if (sniperSpecial && SpiritBow.this.augment == Augment.SPEED){
				if (flurryCount == -1) flurryCount = 3;
				
				final Char enemy = Actor.findChar( cell );
				
				if (enemy == null){
					if(--shotCount <= 0) {
						if(Talent.LethalMomentumTracker.apply(user)) {
							user.next();
						} else {
							user.spendAndNext(castDelay(user, dst));
						}
					}
					flurryCount = -1;

					if (flurryActor != null){
						flurryActor.next();
						flurryActor = null;
					}
					return;
				}

				QuickSlotButton.target(enemy);

				throwSound();

				user.sprite.zap(cell);
				user.sprite.parent.recycle(MissileSprite.class).
						reset(user.sprite,
								cell,
								this,
								() -> {
									if (enemy.isAlive()) {
										curUser = user;
										onThrow(cell);
									}

									flurryCount--;
									if (flurryCount > 0 && !doNotDelay){
										Actor.add(new Actor() {

											{
												actPriority = VFX_PRIO-1;
											}

											@Override
											protected boolean act() {
												flurryActor = this;
												int target = QuickSlotButton.autoAim(enemy, SpiritArrow.this);
												if (target == -1) target = cell;
												cast(user, target);
												Actor.remove(this);
												return false;
											}
										});
										curUser.next();
									} else {
										if (--shotCount <= 0) {
											if (Talent.LethalMomentumTracker.apply(user)) {
												user.next();
											} else {
												user.spendAndNext(castDelay(user, dst));
											}
										}
										flurryCount = -1;
									}
									if (flurryActor != null){
										flurryActor.next();
										flurryActor = null;
									}
						});
			} else {

				if ((user.hasTalent(Talent.SEER_SHOT, Talent.RK_WARDEN) || user.heroClass.isExact(HeroClass.HUNTRESS))
						&& user.buff(Talent.SeerShotCooldown.class) == null){
					int shotPos = throwPos(user, dst);
					if (Actor.findChar(shotPos) == null) {
						new RevealedArea(shotPos, Dungeon.depth).attachTo(user);
					}
				}
				if (!doNotDelay)
					forceSkipDelay = sniperSpecial && --shotCount > 0;
				super.cast(user, dst);
			}
		}
	}

	public static boolean superShot = false;

	public CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				if (Dungeon.hero.hasTalent(Talent.ARCHERY_MARK)){
					bounced = false;
				}
				knockArrow().cast(curUser, target);
			}
		}
		@Override
		public String prompt() {
			return Messages.get(SpiritBow.class, "prompt");
		}
	};
}
