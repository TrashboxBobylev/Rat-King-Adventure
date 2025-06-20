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

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Berserk;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.buffs.PowerfulDegrade;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.AscendedForm;
import com.zrp200.rkpd2.actors.hero.abilities.duelist.ElementalStrike;
import com.zrp200.rkpd2.actors.hero.spells.BodyForm;
import com.zrp200.rkpd2.actors.hero.spells.HolyWeapon;
import com.zrp200.rkpd2.actors.hero.spells.Smite;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.KindOfWeapon;
import com.zrp200.rkpd2.items.artifacts.CloakOfShadows;
import com.zrp200.rkpd2.items.bags.Bag;
import com.zrp200.rkpd2.items.rings.RingOfArcana;
import com.zrp200.rkpd2.items.rings.RingOfForce;
import com.zrp200.rkpd2.items.rings.RingOfFuror;
import com.zrp200.rkpd2.items.trinkets.ParchmentScrap;
import com.zrp200.rkpd2.items.trinkets.ShardOfOblivion;
import com.zrp200.rkpd2.items.wands.WandOfDisintegration;
import com.zrp200.rkpd2.items.weapon.curses.Annoying;
import com.zrp200.rkpd2.items.weapon.curses.Chaotic;
import com.zrp200.rkpd2.items.weapon.curses.Dazzling;
import com.zrp200.rkpd2.items.weapon.curses.Displacing;
import com.zrp200.rkpd2.items.weapon.curses.Explosive;
import com.zrp200.rkpd2.items.weapon.curses.Friendly;
import com.zrp200.rkpd2.items.weapon.curses.Polarized;
import com.zrp200.rkpd2.items.weapon.curses.Sacrificial;
import com.zrp200.rkpd2.items.weapon.curses.Wayward;
import com.zrp200.rkpd2.items.weapon.enchantments.Blazing;
import com.zrp200.rkpd2.items.weapon.enchantments.Blocking;
import com.zrp200.rkpd2.items.weapon.enchantments.Blooming;
import com.zrp200.rkpd2.items.weapon.enchantments.Chilling;
import com.zrp200.rkpd2.items.weapon.enchantments.Corrupting;
import com.zrp200.rkpd2.items.weapon.enchantments.Elastic;
import com.zrp200.rkpd2.items.weapon.enchantments.Grim;
import com.zrp200.rkpd2.items.weapon.enchantments.Kinetic;
import com.zrp200.rkpd2.items.weapon.enchantments.Lucky;
import com.zrp200.rkpd2.items.weapon.enchantments.Projecting;
import com.zrp200.rkpd2.items.weapon.enchantments.Shocking;
import com.zrp200.rkpd2.items.weapon.enchantments.Unstable;
import com.zrp200.rkpd2.items.weapon.enchantments.Vampiric;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.items.weapon.melee.RunicBlade;
import com.zrp200.rkpd2.items.weapon.melee.Scimitar;
import com.zrp200.rkpd2.journal.Catalog;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

import static com.zrp200.rkpd2.Dungeon.hero;

abstract public class Weapon extends KindOfWeapon {

	public float    ACC = 1f;	// Accuracy modifier
	public float	DLY	= 1f;	// Speed modifier
	public int      RCH = 1;    // Reach modifier (only applies to melee hits)
    public int tier;

	@Override
	public void hitSound(float pitch) {
		super.hitSound(pitch);
	}

	public enum Augment {
		SPEED   (0.7f, 2/3f),
		DAMAGE  (1.5f, 5/3f),
		NONE	(1.0f, 1f);

		private float damageFactor;
		private float delayFactor;

		Augment(float dmg, float dly){
			damageFactor = dmg;
			delayFactor = dly;
		}

		public int damageFactor(int dmg){
			int damage = Math.round(dmg * damageFactor);
			damage *= 1f + 0.08f*(Dungeon.hero.pointsInTalent(Talent.HEROIC_ENERGY));
			return damage;
		}

		public float delayFactor(float dly){
			return dly * delayFactor;
		}
	}
	
	public Augment augment = Augment.NONE;
	
	private static final int USES_TO_ID = 20;
	private float usesLeftToID = USES_TO_ID;
	private float availableUsesToID = USES_TO_ID/2f;
	
	public Enchantment enchantment;
	public boolean enchantHardened = false;
	public boolean curseInfusionBonus = false;
	public boolean masteryPotionBonus = false;
	
	@Override
	public int proc( Char attacker, Char defender, int damage) {

		boolean becameAlly = false;
		boolean wasAlly = defender.alignment == Char.Alignment.ALLY;
		if (attacker.buff(MagicImmune.class) == null) {
			Enchantment trinityEnchant = null;
			if (Dungeon.hero.buff(BodyForm.BodyFormBuff.class) != null && this instanceof MeleeWeapon){
				trinityEnchant = Dungeon.hero.buff(BodyForm.BodyFormBuff.class).enchant();
				if (enchantment != null && trinityEnchant != null && trinityEnchant.getClass() == enchantment.getClass()){
					trinityEnchant = null;
				}
			}

			HolyWeapon.HolyWepBuff holyWep = attacker.virtualBuff(HolyWeapon.HolyWepBuff.class);
			if (attacker instanceof Hero && isEquipped((Hero) attacker)
					&& holyWep != null){
				if (enchantment != null &&
						(holyWep instanceof HolyWeapon.HolyWepBuff.Empowered || ((Hero) attacker).subClass == HeroSubClass.PALADIN || hasCurseEnchant())){
					damage = enchantment.proc(this, attacker, defender, damage);
					if (defender.alignment == Char.Alignment.ALLY && !wasAlly){
						becameAlly = true;
					}
				}
				if (defender.isAlive() && !becameAlly && trinityEnchant != null){
					damage = trinityEnchant.proc(this, attacker, defender, damage);
				}
				if (defender.isAlive() && !becameAlly) {
					holyWep.proc(attacker, defender);
				}
			} else {
				if (holyWep == null) holyWep = Dungeon.hero.buff(HolyWeapon.HolyWepBuff.Empowered.class);
				if (enchantment != null
						&& (Random.Float() < Talent.SpiritBladesTracker.getProcModifier())) {
					damage = enchantment.proc(this, attacker, defender, damage);
					if (defender.alignment == Char.Alignment.ALLY && !wasAlly) {
						becameAlly = true;
					}
				}

				if (defender.isAlive() && !becameAlly && trinityEnchant != null){
					damage = trinityEnchant.proc(this, attacker, defender, damage);
				}

				if (defender.isAlive() && !becameAlly && holyWep != null) {
					holyWep.proc(attacker, defender);
				}
			}

			if (attacker instanceof Hero && isEquipped((Hero) attacker) &&
					attacker.virtualBuff(Smite.SmiteTracker.class) != null && !becameAlly){
				defender.damage(attacker.virtualBuff(Smite.SmiteTracker.class).bonusDmg((Hero) attacker, defender), Smite.INSTANCE);
			}
		}
		
		if (!levelKnown && attacker == Dungeon.hero) {
			float uses = Math.min( availableUsesToID, Talent.itemIDSpeedFactor(Dungeon.hero, this) );
			availableUsesToID -= uses;
			usesLeftToID -= uses;
			if (usesLeftToID <= 0) {
				if (ShardOfOblivion.passiveIDDisabled()){
					if (usesLeftToID > -1){
						GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), name());
					}
					setIDReady();
				} else {
					identify();
					GLog.p(Messages.get(Weapon.class, "identify"));
					Badges.validateItemLevelAquired(this);
				}
			}
		}

		return damage;
	}
	
	public void onHeroGainExp( float levelPercent, Hero hero ){
		levelPercent *= Talent.itemIDSpeedFactor(hero, this);
		if (!levelKnown && isEquipped(hero) && availableUsesToID <= USES_TO_ID/2f) {
			//gains enough uses to ID over 0.5 levels
			availableUsesToID = Math.min(USES_TO_ID/2f, availableUsesToID + levelPercent * USES_TO_ID);
		}
	}
	
	private static final String USES_LEFT_TO_ID = "uses_left_to_id";
	private static final String AVAILABLE_USES  = "available_uses";
	private static final String ENCHANTMENT	    = "enchantment";
	private static final String ENCHANT_HARDENED = "enchant_hardened";
	private static final String CURSE_INFUSION_BONUS = "curse_infusion_bonus";
	private static final String MASTERY_POTION_BONUS = "mastery_potion_bonus";
	private static final String AUGMENT	        = "augment";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( USES_LEFT_TO_ID, usesLeftToID );
		bundle.put( AVAILABLE_USES, availableUsesToID );
		bundle.put( ENCHANTMENT, enchantment );
		bundle.put( ENCHANT_HARDENED, enchantHardened );
		bundle.put( CURSE_INFUSION_BONUS, curseInfusionBonus );
		bundle.put( MASTERY_POTION_BONUS, masteryPotionBonus );
		bundle.put( AUGMENT, augment );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		usesLeftToID = bundle.getFloat( USES_LEFT_TO_ID );
		availableUsesToID = bundle.getFloat( AVAILABLE_USES );
		enchantment = (Enchantment)bundle.get( ENCHANTMENT );
		enchantHardened = bundle.getBoolean( ENCHANT_HARDENED );
		curseInfusionBonus = bundle.getBoolean( CURSE_INFUSION_BONUS );
		masteryPotionBonus = bundle.getBoolean( MASTERY_POTION_BONUS );

		augment = bundle.getEnum(AUGMENT, Augment.class);
	}
	
	@Override
	public void reset() {
		super.reset();
		usesLeftToID = USES_TO_ID;
		availableUsesToID = USES_TO_ID/2f;
	}

	@Override
	public boolean collect(Bag container) {
		if(super.collect(container)){
			if (Dungeon.hero != null && Dungeon.hero.isAlive() && isIdentified() && enchantment != null){
				Catalog.setSeen(enchantment.getClass());
				Statistics.itemTypesDiscovered.add(enchantment.getClass());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item identify(boolean byHero) {
		if (enchantment != null && byHero && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Catalog.setSeen(enchantment.getClass());
			Statistics.itemTypesDiscovered.add(enchantment.getClass());
		}
		return super.identify(byHero);
	}

	public void setIDReady(){
		usesLeftToID = -1;
	}

	public boolean readyToIdentify(){
		return !isIdentified() && usesLeftToID <= 0;
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		
		int encumbrance = 0;
		
		if( owner instanceof Hero ){
			encumbrance = STRReq() - ((Hero)owner).STR();
		}

		float ACC = this.ACC;

		if (owner.buff(Wayward.WaywardBuff.class) != null && enchantment instanceof Wayward){
			ACC /= 5;
		}

		return encumbrance > 0 ? (float)(ACC / Math.pow( 1.5, encumbrance )) : ACC;
	}
	
	@Override
	public float delayFactor( Char owner ) {
		return baseDelay(owner) * (1f/speedMultiplier(owner));
	}

	protected float baseDelay( Char owner ){
		float delay = augment.delayFactor(this.DLY);
		if (owner instanceof Hero) {
			int encumbrance = STRReq() - ((Hero)owner).STR();
			if (encumbrance > 0){
				delay *= Math.pow( 1.2, encumbrance );
			}
		}

		return delay;
	}

	protected float speedMultiplier(Char owner ){
		float multi = RingOfFuror.attackSpeedMultiplier(owner);

		if (owner.buff(Scimitar.SwordDance.class) != null){
			multi += 0.6f;
		}

		return multi;
	}

	@Override
	public int reachFactor(Char owner) {
		int reach = RCH;
		if (owner instanceof Hero && RingOfForce.fightingUnarmed((Hero) owner)){
			reach = 1; //brawlers stance benefits from enchantments, but not innate reach
			if (!RingOfForce.unarmedGetsWeaponEnchantment((Hero) owner)){
				return reach;
			}
		}
		if (owner instanceof Hero && owner.buff(AscendedForm.AscendBuff.class) != null){
			reach += 2;
		}
		if(hasEnchant(Projecting.class, owner)) reach +=Math.round(RingOfArcana.enchantPowerMultiplier(owner));
		if(owner.buff(ChampionEnemy.Projecting.class) != null) reach += 2;
		if(owner.buff(ChampionEnemy.Giant.class) != null) reach += 1;
		if(owner.buff(HighnessBuff.class) != null && owner.buff(HighnessBuff.class).state == HighnessBuff.State.ENERGIZED) reach += 2;
		if(owner instanceof Hero) {
			Hero hero = (Hero) owner;
			MagesStaff staff = hero.belongings.getItem(MagesStaff.class);
			if(hero.subClass.is(HeroSubClass.BATTLEMAGE) && staff != null && staff.wandClass() == WandOfDisintegration.class) {
				if(staff == this || Random.Int(3) < hero.pointsInTalent(Talent.SORCERY)) reach++;
			}
			if (((Hero) owner).pointsInTalent(Talent.BEAR_PAW) > 1 && owner.HP <= owner.HT / 4)
				reach++;
			if(RobotBuff.isRobot()) reach++;
		}
		return reach;
	}

	public int STRReq(){
		return STRReq(level());
	}

	public abstract int STRReq(int lvl);

	protected static int STRReq(int tier, int lvl){
		lvl = Math.max(0, lvl);

		//strength req decreases at +1,+3,+6,+10,etc.
		int req = (8 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;

		/* removed in v0.9.3
		if (Dungeon.hero.hasTalent(Talent.STRONGMAN)) req -= 1+2*(Dungeon.hero.pointsInTalent(Talent.STRONGMAN)-1); // 1/3/5
		if (Dungeon.hero.pointsInTalent(Talent.RK_GLADIATOR) >= 2) req--;
		*/

		return req;
	}

	@Override
	public int level() {
		int level = super.level();
		if (curseInfusionBonus) level += 1 + level/6;
		return level;
	}

	private static boolean evaluatingTwinUpgrades = false;
	@Override
	public int buffedLvl() {
		if (Dungeon.hero.buff(PowerfulDegrade.class) != null) return 0;
		int lvl = super.buffedLvl();
		if((isEquipped(Dungeon.hero) || Dungeon.hero != null && Dungeon.hero.belongings.contains(this))
				&& (Dungeon.hero.buff(CloakOfShadows.cloakStealth.class, false) != null && Dungeon.hero.heroClass.isExact(HeroClass.ROGUE))) lvl++;

		if (!evaluatingTwinUpgrades && isEquipped(Dungeon.hero) && Dungeon.hero.hasTalent(Talent.TWIN_UPGRADES)){
			evaluatingTwinUpgrades = true;
			for (KindOfWeapon weapon : Dungeon.hero.belongings.weapons()) {
				if (weapon == this || !(weapon instanceof Weapon)) continue;
				//weaker weapon needs to be 2/1/0 tiers lower, based on talent level
				if ((tier + (3 - Dungeon.hero.pointsInTalent(Talent.TWIN_UPGRADES))) <= ((Weapon) weapon).tier
						&& weapon.buffedLvl() > lvl) {
					lvl = weapon.buffedLvl();
				}
			}
			evaluatingTwinUpgrades = false;
		}
		return lvl;
	}

	@Override
	public Item upgrade() {
		return upgrade(false);
	}
	
	public Item upgrade(boolean enchant ) {

		if (enchant){
			if (enchantment == null){
				enchant(Enchantment.random());
			}
		} else if (enchantment != null) {
			//chance to lose harden buff is 10/20/40/80/100% when upgrading from +6/7/8/9/10
			if (enchantHardened){
				if (level() >= 6 && Random.Float(10) < Math.pow(2, level()-6)){
					enchantHardened = false;
				}

			//chance to remove curse is a static 33%
			} else if (hasCurseEnchant()) {
				if (Random.Int(3) == 0) enchant(null);

			//otherwise chance to lose enchant is 10/20/40/80/100% when upgrading from +4/5/6/7/8
			} else if (level() >= 4 && Random.Float(10) < Math.pow(2, level()-4)){
				enchant(null);
			}
		}
		
		cursed = false;

		return super.upgrade();
	}
	
	@Override
	public String name() {
		if (isEquipped(Dungeon.hero) && !hasCurseEnchant() && Dungeon.hero.buff(HolyWeapon.HolyWepBuff.class) != null
			&& (Dungeon.hero.subClass != HeroSubClass.PALADIN || enchantment == null)){
				return Messages.get(HolyWeapon.class, "ench_name", super.name());
			} else {
				return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.name(super.name()) : super.name();

		}
	}
	
	@Override
	public Item random() {
		//+0: 75% (3/4)
		//+1: 20% (4/20)
		//+2: 5%  (1/20)
		int n = 0;
		if (Random.Int(4) == 0) {
			n++;
			if (Random.Int(5) == 0) {
				n++;
			}
		}
		if (!Dungeon.isChallenged(Challenges.REDUCED_POWER))
		level(n);

		//we use a separate RNG here so that variance due to things like parchment scrap
		//does not affect levelgen
		Random.pushGenerator(Random.Long());

			//30% chance to be cursed
			//10% chance to be enchanted
			cursed = false; // not cursed by default.
		float effectRoll = Random.Float();
		if (effectRoll < 0.3f* ParchmentScrap.curseChanceMultiplier()) {
				enchant(Enchantment.randomCurse());
				cursed = true;
			} else if (effectRoll >= 1f - (0.1f * ParchmentScrap.enchantChanceMultiplier())){
			enchant();
		} else {
			enchant(null);
		}

		Random.popGenerator();

		return this;
	}
	
	public Weapon enchant( Enchantment ench ) {
		if (ench == null || !ench.curse()) curseInfusionBonus = false;
		enchantment = ench;
		updateQuickslot();
		if (ench != null && isIdentified() && Dungeon.hero != null
				&& Dungeon.hero.isAlive() && Dungeon.hero.belongings.contains(this)){
			Catalog.setSeen(ench.getClass());
			Statistics.itemTypesDiscovered.add(ench.getClass());
		}
		return this;
	}

	public Weapon enchant() {

		Class<? extends Enchantment> oldEnchantment = enchantment != null ? enchantment.getClass() : null;
		Enchantment ench = Enchantment.random( oldEnchantment );

		return enchant( ench );
	}

	public boolean hasEnchant(Class<?extends Enchantment> type, Char owner) {
		if (enchantment == null){
			return false;
		} else if (owner.buff(MagicImmune.class) != null) {
			return false;
		} else if (!enchantment.curse()
				&& owner instanceof Hero
				&& isEquipped((Hero) owner)
				&& owner.buff(HolyWeapon.HolyWepBuff.class) != null
				&& ((Hero) owner).subClass != HeroSubClass.PALADIN) {
			return false;
		} else if (owner.buff(BodyForm.BodyFormBuff.class) != null
				&& owner.buff(BodyForm.BodyFormBuff.class).enchant() != null
				&& owner.buff(BodyForm.BodyFormBuff.class).enchant().getClass().equals(type)){
			return true;
		} else {
			// idk why evan's so anti-oop
			return type.isInstance(enchantment);
		}
	}
	
	//these are not used to process specific enchant effects, so magic immune doesn't affect them
	public boolean hasGoodEnchant(){
		return enchantment != null && !enchantment.curse();
	}

	public boolean hasCurseEnchant(){
		return enchantment != null && enchantment.curse();
	}

	protected static ItemSprite.Glowing HOLY = new ItemSprite.Glowing( 0xFFFF00 );

	@Override
	public ItemSprite.Glowing glowing() {
		// fixme should probably cause thrown weapons to glow
		if (isEquipped(Dungeon.hero) && !hasCurseEnchant() && (enchantment == null ? hero.virtualBuff(HolyWeapon.HolyWepBuff.class) != null
				: hero.buff(HolyWeapon.HolyWepBuff.class) != null && hero.subClass != HeroSubClass.PALADIN)){
			return HOLY;
		} else {
			return enchantment != null && (cursedKnown || !enchantment.curse()) ? enchantment.glowing() : null;
		}
	}
	public static float procChanceMultiplier = 0;

	public static abstract class Enchantment implements Bundlable {

		public static final Class<?>[] common = new Class<?>[]{
				Blazing.class, Chilling.class, Kinetic.class, Shocking.class};

		public static final Class<?>[] uncommon = new Class<?>[]{
				Blocking.class, Blooming.class, Elastic.class,
				Lucky.class, Projecting.class, Unstable.class};

		public static final Class<?>[] rare = new Class<?>[]{
				Corrupting.class, Grim.class, Vampiric.class};

		public static final float[] typeChances = new float[]{
				50, //12.5% each
				40, //6.67% each
				10  //3.33% each
		};

		public static final Class<?>[] curses = new Class<?>[]{
				Annoying.class, Displacing.class, Dazzling.class, Explosive.class,
				Sacrificial.class, Wayward.class, Polarized.class, Friendly.class,
				Chaotic.class
		};

		public boolean heroicEnchanted;
			
		public abstract int proc( Weapon weapon, Char attacker, Char defender, int damage );

		public int proc( RingOfForce.Force weaponBuff, Char attacker, Char defender, int damage){
			return proc(new Weapon() {
				@Override
				public int STRReq(int lvl) {
					return attacker instanceof Hero ? ((Hero) attacker).STR() : 0;
				}

				@Override
				public int min(int lvl) {
					return 0;
				}

				@Override
				public int max(int lvl) {
					return 0;
				}

				@Override
				public int level() {
					return weaponBuff.level();
				}
			}, attacker, defender, damage);
		}

		public static float genericProcChanceMultiplier( Char attacker, boolean applyArcana ){
			float multi = 1;
			if (procChanceMultiplier != 0) multi *= procChanceMultiplier;
			if(applyArcana) multi *= RingOfArcana.enchantPowerMultiplier(attacker);
			boolean heroAttack = attacker instanceof Hero;
			Berserk rage = attacker.buff(Berserk.class);
			if (rage != null) {
				multi += rage.enchantFactor(multi);
			}
			// note I'm specifically preventing it from lowering the chance. I already handled that in Weapon#attackProc.
			multi += Math.max(0, Talent.SpiritBladesTracker.getProcModifier()-1);

			if (attacker.buff(RunicBlade.RunicSlashTracker.class) != null){
				multi += attacker.buff(RunicBlade.RunicSlashTracker.class).boost;
                //handled already
				//attacker.buff(RunicBlade.RunicSlashTracker.class).detach();
			}

			if (heroAttack && HighnessBuff.isEnergized() && ((Hero) attacker).pointsInTalent(Talent.SLASH_RUNNER) > 1){
				multi += 1f;
			}

			if (attacker.virtualBuff(Smite.SmiteTracker.class) != null){
				multi += attacker.virtualBuff(Smite.SmiteTracker.class).enchMulti();
			}

			if (attacker.buff(ElementalStrike.DirectedPowerTracker.class) != null){
				multi += attacker.buff(ElementalStrike.DirectedPowerTracker.class).enchBoost;
				attacker.buff(ElementalStrike.DirectedPowerTracker.class).detach();
			}

			if (attacker.buff(Talent.SpiritBladesTracker.class) != null
					&& ((Hero)attacker).pointsInTalent(Talent.SPIRIT_BLADES) == 4){
				multi += 0.1f;
			}
			if (heroAttack && attacker.buff(Talent.StrikingWaveTracker.class) != null
					&& ((Hero)attacker).pointsInTalent(Talent.STRIKING_WAVE) == 4){
				multi += 0.2f;
			}

			if (attacker.buff(RingOfForce.Force.class) != null && heroAttack && ((Hero) attacker).belongings.weapon() == null){
				multi *= 1.5f;
			}
			if (attacker.buff(Smite.OmniSmite.OmniSmiteTracker.class) != null) {
				multi *= Smite.OmniSmite.MULTI;
			}

			return multi;
		}
		public static float genericProcChanceMultiplier(Char attacker) {
			return genericProcChanceMultiplier(attacker, true);
		}

		protected float procChanceMultiplier( Char attacker) {
			float procChanceMultiplier = genericProcChanceMultiplier(attacker);
			if (heroicEnchanted){
				procChanceMultiplier *= 1.0f + 1.15f * Dungeon.hero.pointsInTalent(Talent.HEROIC_ENDURANCE);
			}
			return procChanceMultiplier;
		}

		public String name() {
			if (!curse())
				return name( Messages.get(this, "enchant"));
			else
				return name( Messages.get(Item.class, "curse"));
		}

		public String name( String weaponName ) {
			String name = Messages.get(this, "name", weaponName);
			if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.RLETTER)) {
				return ShatteredPixelDungeon.turnIntoRrrr(name);
			}
			return name;
		}

		public String desc() {
			return Messages.get(this, "desc");
		}

		public boolean curse() {
			return false;
		}

		protected static float procChance(Char attacker, int level, float numerator, float denominator) {
			return genericProcChanceMultiplier(attacker) * (numerator+level)/(denominator+level);
		}
		// just a faster way to get proc chances resolved while factoring in enchant modifiers.
		// results in (N+L)/(D+L) * modifier chance of returning true.
		public static boolean proc(Char attacker, int level, float numerator, float denominator) {
			return Random.Float() < procChance(attacker, level, numerator, denominator);
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}
		
		public abstract ItemSprite.Glowing glowing();
		
		@SuppressWarnings("unchecked")
		public static Enchantment random( Class<? extends Enchantment> ... toIgnore ) {
			switch(Random.chances(typeChances)){
				case 0: default:
					return randomCommon( toIgnore );
				case 1:
					return randomUncommon( toIgnore );
				case 2:
					return randomRare( toIgnore );
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Enchantment randomCommon( Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(common));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Enchantment randomUncommon( Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(uncommon));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}
		
		@SuppressWarnings("unchecked")
		public static Enchantment randomRare( Class<? extends Enchantment> ... toIgnore ) {
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(rare));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}

		@SuppressWarnings("unchecked")
		public static Enchantment randomCurse( Class<? extends Enchantment> ... toIgnore ){
			ArrayList<Class<?>> enchants = new ArrayList<>(Arrays.asList(curses));
			enchants.removeAll(Arrays.asList(toIgnore));
			if (enchants.isEmpty()) {
				return random();
			} else {
				return (Enchantment) Reflection.newInstance(Random.element(enchants));
			}
		}
		
	}
}
