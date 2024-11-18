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

package com.zrp200.rkpd2.actors.hero;

import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Bones;
import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.GamesInProgress;
import com.zrp200.rkpd2.SPDSettings;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.Statistics;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.blobs.SacrificialFire;
import com.zrp200.rkpd2.actors.buffs.AdrenalineSurge;
import com.zrp200.rkpd2.actors.buffs.AmnesiaBuff;
import com.zrp200.rkpd2.actors.buffs.AnkhInvulnerability;
import com.zrp200.rkpd2.actors.buffs.ArtifactRecharge;
import com.zrp200.rkpd2.actors.buffs.AscensionChallenge;
import com.zrp200.rkpd2.actors.buffs.Awareness;
import com.zrp200.rkpd2.actors.buffs.Barkskin;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Berserk;
import com.zrp200.rkpd2.actors.buffs.Bless;
import com.zrp200.rkpd2.actors.buffs.Blindness;
import com.zrp200.rkpd2.actors.buffs.BrawlerBuff;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Burning;
import com.zrp200.rkpd2.actors.buffs.ChampionEnemy;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.DomainOfHell;
import com.zrp200.rkpd2.actors.buffs.Drowsy;
import com.zrp200.rkpd2.actors.buffs.Foresight;
import com.zrp200.rkpd2.actors.buffs.GodSlayerBurning;
import com.zrp200.rkpd2.actors.buffs.HPDebuff;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.HoldFast;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.Levitation;
import com.zrp200.rkpd2.actors.buffs.LostInventory;
import com.zrp200.rkpd2.actors.buffs.MindVision;
import com.zrp200.rkpd2.actors.buffs.Momentum;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.buffs.NoDeath;
import com.zrp200.rkpd2.actors.buffs.Paralysis;
import com.zrp200.rkpd2.actors.buffs.PhysicalEmpower;
import com.zrp200.rkpd2.actors.buffs.RKChampionBuff;
import com.zrp200.rkpd2.actors.buffs.Recharging;
import com.zrp200.rkpd2.actors.buffs.Regeneration;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.buffs.Slow;
import com.zrp200.rkpd2.actors.buffs.SnipersMark;
import com.zrp200.rkpd2.actors.buffs.SpiritBuff;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.buffs.Warp;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.duelist.Challenge;
import com.zrp200.rkpd2.actors.hero.abilities.duelist.ElementalStrike;
import com.zrp200.rkpd2.actors.hero.abilities.huntress.NaturesPower;
import com.zrp200.rkpd2.actors.hero.abilities.rat_king.OmniAbility;
import com.zrp200.rkpd2.actors.hero.abilities.warrior.Endure;
import com.zrp200.rkpd2.actors.mobs.AbyssalSpawner;
import com.zrp200.rkpd2.actors.mobs.Ech;
import com.zrp200.rkpd2.actors.mobs.Elemental;
import com.zrp200.rkpd2.actors.mobs.Mimic;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Monk;
import com.zrp200.rkpd2.actors.mobs.Phantom;
import com.zrp200.rkpd2.actors.mobs.Snake;
import com.zrp200.rkpd2.actors.mobs.npcs.Blacksmith;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.CheckedCell;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.ShieldHalo;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.Splash;
import com.zrp200.rkpd2.effects.particles.ExoParticle;
import com.zrp200.rkpd2.effects.particles.GodfireParticle;
import com.zrp200.rkpd2.items.Ankh;
import com.zrp200.rkpd2.items.BrokenSeal;
import com.zrp200.rkpd2.items.Dewdrop;
import com.zrp200.rkpd2.items.DuelistGrass;
import com.zrp200.rkpd2.items.EquipableItem;
import com.zrp200.rkpd2.items.Heap;
import com.zrp200.rkpd2.items.Heap.Type;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.KindOfWeapon;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.armor.glyphs.AntiMagic;
import com.zrp200.rkpd2.items.armor.glyphs.Brimstone;
import com.zrp200.rkpd2.items.armor.glyphs.Viscosity;
import com.zrp200.rkpd2.items.artifacts.AlchemistsToolkit;
import com.zrp200.rkpd2.items.artifacts.Artifact;
import com.zrp200.rkpd2.items.artifacts.CapeOfThorns;
import com.zrp200.rkpd2.items.artifacts.CloakOfShadows;
import com.zrp200.rkpd2.items.artifacts.DriedRose;
import com.zrp200.rkpd2.items.artifacts.EtherealChains;
import com.zrp200.rkpd2.items.artifacts.HornOfPlenty;
import com.zrp200.rkpd2.items.artifacts.MasterThievesArmband;
import com.zrp200.rkpd2.items.artifacts.TalismanOfForesight;
import com.zrp200.rkpd2.items.artifacts.TimekeepersHourglass;
import com.zrp200.rkpd2.items.bags.MagicalHolster;
import com.zrp200.rkpd2.items.journal.Guidebook;
import com.zrp200.rkpd2.items.keys.CrystalKey;
import com.zrp200.rkpd2.items.keys.GoldenKey;
import com.zrp200.rkpd2.items.keys.IronKey;
import com.zrp200.rkpd2.items.keys.Key;
import com.zrp200.rkpd2.items.keys.SkeletonKey;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.items.potions.PotionOfExperience;
import com.zrp200.rkpd2.items.potions.PotionOfHealing;
import com.zrp200.rkpd2.items.potions.elixirs.ElixirOfMight;
import com.zrp200.rkpd2.items.potions.elixirs.KromerPotion;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfDivineInspiration;
import com.zrp200.rkpd2.items.quest.DarkGold;
import com.zrp200.rkpd2.items.quest.Pickaxe;
import com.zrp200.rkpd2.items.rings.RingOfAccuracy;
import com.zrp200.rkpd2.items.rings.RingOfElements;
import com.zrp200.rkpd2.items.rings.RingOfEvasion;
import com.zrp200.rkpd2.items.rings.RingOfForce;
import com.zrp200.rkpd2.items.rings.RingOfFuror;
import com.zrp200.rkpd2.items.rings.RingOfHaste;
import com.zrp200.rkpd2.items.rings.RingOfMight;
import com.zrp200.rkpd2.items.rings.RingOfTenacity;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfMagicMapping;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfChallenge;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.wands.WandOfDisintegration;
import com.zrp200.rkpd2.items.wands.WandOfLightning;
import com.zrp200.rkpd2.items.wands.WandOfLivingEarth;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.enchantments.Kinetic;
import com.zrp200.rkpd2.items.weapon.melee.Flail;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.items.weapon.melee.NuclearHatchet;
import com.zrp200.rkpd2.items.weapon.melee.Quarterstaff;
import com.zrp200.rkpd2.items.weapon.melee.RoundShield;
import com.zrp200.rkpd2.items.weapon.melee.Sai;
import com.zrp200.rkpd2.items.weapon.melee.Scimitar;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.journal.Document;
import com.zrp200.rkpd2.journal.Notes;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.MiningLevel;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.Chasm;
import com.zrp200.rkpd2.levels.features.LevelTransition;
import com.zrp200.rkpd2.levels.traps.Trap;
import com.zrp200.rkpd2.mechanics.ShadowCaster;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.AlchemyScene;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.scenes.PixelScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.sprites.HeroSprite;
import com.zrp200.rkpd2.ui.AttackIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.QuickSlotButton;
import com.zrp200.rkpd2.ui.StatusPane;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;
import com.zrp200.rkpd2.windows.WndHero;
import com.zrp200.rkpd2.windows.WndMessage;
import com.zrp200.rkpd2.windows.WndResurrect;
import com.zrp200.rkpd2.windows.WndTradeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

public class Hero extends Char {

	{
		actPriority = HERO_PRIO;
		
		alignment = Alignment.ALLY;

		resistances.add(GodSlayerBurning.class);
	}
	
	public static final int MAX_LEVEL = Integer.MAX_VALUE;

	public static final int STARTING_STR = 10;
	
	private static final float TIME_TO_REST		    = 1f;
	private static final float TIME_TO_SEARCH	    = 2f;
	private static final float HUNGER_FOR_SEARCH	= 6f;
	
	public HeroClass heroClass = HeroClass.ROGUE;
	public HeroClass heroClass2 = null;
	public HeroSubClass subClass = HeroSubClass.NONE;
	public HeroSubClass subClass2 = HeroSubClass.NONE;

	public boolean isSubclassedLoosely(HeroSubClass sub){
		return isSubclassedLoosely(this, sub);
	}

	public static boolean isSubclassedLoosely(Hero hero, HeroSubClass sub){
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.BALANCE) & hero.subClass != HeroSubClass.NONE){
			return true;
		} else {
			if (hero.subClass2 == HeroSubClass.NONE) {
				return hero.matchSubclass(hero.subClass, sub);
			} else {
				return hero.matchSubclass(hero.subClass, sub) || hero.matchSubclass(hero.subClass2, sub);
			}
		}
	}

	public boolean isSubclassed(HeroSubClass sub){
		return isSubclassed(this, sub);
	}

	public static boolean isSubclassed(Hero hero, HeroSubClass sub){
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.BALANCE) & hero.subClass != HeroSubClass.NONE){
			return true;
		} else {
			if (hero.subClass2 == HeroSubClass.NONE) {
				return hero.subClass == sub;
			} else {
				return hero.subClass == sub || hero.subClass2 == sub;
			}
		}
	}

	public boolean matchSubclass(HeroSubClass sub1, HeroSubClass sub2){
		if (sub1 == HeroSubClass.KING){
			switch (sub2){
				default: return false;
				case KING:
					return true;
				case BERSERKER:
					return hasTalent(Talent.RK_BERSERKER);
				case GLADIATOR:
					return hasTalent(Talent.RK_GLADIATOR);
				case BATTLEMAGE:
					return hasTalent(Talent.RK_BATTLEMAGE);
				case WARLOCK:
					return hasTalent(Talent.RK_WARLOCK);
				case ASSASSIN:
					return hasTalent(Talent.RK_ASSASSIN);
				case FREERUNNER:
					return hasTalent(Talent.RK_FREERUNNER);
				case SNIPER:
					return hasTalent(Talent.RK_SNIPER);
				case WARDEN:
					return hasTalent(Talent.RK_WARDEN);
			}
		} else {
			return sub1 == sub2;
		}
	}

	public boolean isClassedLoosely(HeroClass sub){
		return isClassedLoosely(this, sub);
	}

	public static boolean isClassedLoosely(Hero hero, HeroClass sub){
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.BALANCE)){
			return true;
		} else {
			if (hero.heroClass2 == null) {
				return hero.matchClass(hero.heroClass, sub);
			} else {
				return hero.matchClass(hero.heroClass, sub) || hero.matchClass(hero.heroClass2, sub);
			}
		}
	}

	public boolean isClassed(HeroClass sub){
		return isClassed(this, sub);
	}

	public boolean isClassed(Hero hero, HeroClass sub){
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.BALANCE)){
			return true;
		} else {
			if (hero.heroClass2 == null) {
				return hero.heroClass == sub;
			} else {
				return hero.heroClass == sub || hero.heroClass2 == sub;
			}
		}
	}

	public boolean matchClass(HeroClass class1, HeroClass class2){
		if (class1 == HeroClass.RAT_KING){
			return class2 != HeroClass.DUELIST;
		} else {
			return class1 == class2;
		}
	}

	public ArmorAbility armorAbility = null;
	public ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
	public LinkedHashMap<Talent, Talent> metamorphedTalents = new LinkedHashMap<>();

	private int attackSkill = 10;
	private int defenseSkill = 5;

	public boolean ready = false;
	public boolean damageInterrupt = true;
	public HeroAction curAction = null;
	public HeroAction lastAction = null;
	public int lastMovPos = -1;

	private Char enemy;
	
	public boolean resting = false;
	
	public Belongings belongings;
	
	public int STR;
	
	public float awareness;
	
	public int lvl = 1;
	public int exp = 0;
	
	public int HTBoost = 0;
	
	private ArrayList<Mob> visibleEnemies;

	//This list is maintained so that some logic checks can be skipped
	// for enemies we know we aren't seeing normally, resulting in better performance
	public ArrayList<Mob> mindVisionEnemies = new ArrayList<>();

	public double getViewDistanceModifier() {
		int points = pointsInTalent(Talent.FARSIGHT, Talent.RK_SNIPER);
		if(canHaveTalent(Talent.FARSIGHT)) points++;
		return 1 + 0.25*points;
	}

	public Hero() {
		super();

		HP = HT = 20;
		if (Dungeon.isChallenged(Challenges.JUST_KILL_ME)){
			HP = HT = 10;
		}
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.BALANCE)){
			HP = HT = 5;
		}
		STR = STARTING_STR;
		
		belongings = new Belongings( this );

		visibleEnemies = new ArrayList<>();
	}
	
	public void updateHT( boolean boostHP ){
		int curHT = HT;
		
		HT = 20 + 5*(lvl-1) + HTBoost;
		if (Dungeon.isChallenged(Challenges.JUST_KILL_ME)){
			HT = Math.round(10 + 2.5f*(lvl-1) + HTBoost/2f);
		}
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.BALANCE)){
			HT = 5 + 3*(lvl-1) + HTBoost;
		}
		float multiplier = RingOfMight.HTMultiplier(this);
		HT = Math.round(multiplier * HT);
		
		if (buff(ElixirOfMight.HTBoost.class) != null){
			HT += buff(ElixirOfMight.HTBoost.class).boost();
		}
		if (hasTalent(Talent.RK_PALADIN)){
			HT += RKChampionBuff.rkPaladinUniqueAllies() * (3*pointsInTalent(Talent.RK_PALADIN)-1);
		}
		
		if (boostHP){
			HP += Math.max(HT - curHT, 0);
		}
		if (buff(HPDebuff.class) != null){
			HT = Math.max(1, HT - (int) buff(HPDebuff.class).count());
		}
		HP = Math.min(HP, HT);
	}

	public int STR() {
		int strBonus = 0;

		strBonus += RingOfMight.strengthBonus( this );
		
		AdrenalineSurge buff = buff(AdrenalineSurge.class);
		if (buff != null){
			strBonus += buff.boost();
		}

		// TODO buff for warrior
		if (hasTalent(Talent.STRONGMAN,Talent.RK_BERSERKER)){ // note that you need to have points in this.
			float boost = Math.max(
					0.06f + 0.10f*pointsInTalent(Talent.STRONGMAN), // +16%/+26%/+36%
					0.03f + 0.05f*pointsInTalent(Talent.RK_BERSERKER)
			);
			//let's see
			//15 strength
			//at +1 it will be 17 strength
			//at +2 it will be 18 strength
			//at +3 it will be 20 strength
			//so +2/+3/+5
			//seems legit
			strBonus += (int)Math.floor(STR * boost);
		}

		return STR + strBonus;
	}

	// this affects what items get boosted. if I want a talent to grant boosts I should go here.
	public int getBonus(Item item) {
		return heroClass.getBonus(item) + subClass.getBonus(item) + subClass2.getBonus(item);
	}

	private static final String CLASS       = "class";
	private static final String CLASS2      = "class2";
	private static final String SUBCLASS    = "subClass";
	private static final String SUBCLASS2    = "subClass2";
	private static final String ABILITY     = "armorAbility";

	private static final String ATTACK		= "attackSkill";
	private static final String DEFENSE		= "defenseSkill";
	private static final String STRENGTH	= "STR";
	private static final String LEVEL		= "lvl";
	private static final String EXPERIENCE	= "exp";
	private static final String HTBOOST     = "htboost";
	private static final String LASTMOVE = "last_move";

	@Override
	public void storeInBundle( Bundle bundle ) {

		super.storeInBundle( bundle );

		bundle.put( CLASS, heroClass );
		bundle.put( CLASS2, heroClass2);
		bundle.put( SUBCLASS, subClass );
		bundle.put( SUBCLASS2, subClass2 );
		bundle.put( ABILITY, armorAbility );
		Talent.storeTalentsInBundle( bundle, this );
		
		bundle.put( ATTACK, attackSkill );
		bundle.put( DEFENSE, defenseSkill );
		
		bundle.put( STRENGTH, STR );
		
		bundle.put( LEVEL, lvl );
		bundle.put( EXPERIENCE, exp );
		
		bundle.put( HTBOOST, HTBoost );
		bundle.put( LASTMOVE, lastMovPos);

		belongings.storeInBundle( bundle );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {

		lvl = bundle.getInt( LEVEL );
		exp = bundle.getInt( EXPERIENCE );

		HTBoost = bundle.getInt(HTBOOST);

		super.restoreFromBundle( bundle );

		heroClass = bundle.getEnum( CLASS, HeroClass.class );
		subClass = bundle.getEnum( SUBCLASS, HeroSubClass.class );
		if (bundle.contains(SUBCLASS2)){
			subClass2 = bundle.getEnum( SUBCLASS2, HeroSubClass.class );
		}
		if (bundle.contains(CLASS2)){
			heroClass2 = bundle.getEnum( CLASS2, HeroClass.class );
		}
		armorAbility = (ArmorAbility)bundle.get( ABILITY );
		Talent.restoreTalentsFromBundle( bundle, this );
		lastMovPos = bundle.getInt(LASTMOVE);

		attackSkill = bundle.getInt( ATTACK );
		defenseSkill = bundle.getInt( DEFENSE );
		
		STR = bundle.getInt( STRENGTH );

		belongings.restoreFromBundle( bundle );
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		info.level = bundle.getInt( LEVEL );
		info.str = bundle.getInt( STRENGTH );
		info.exp = bundle.getInt( EXPERIENCE );
		info.hp = bundle.getInt( Char.TAG_HP );
		info.ht = bundle.getInt( Char.TAG_HT );
		info.shld = bundle.getInt( Char.TAG_SHLD );
		info.heroClass = bundle.getEnum( CLASS, HeroClass.class );
		info.subClass = bundle.getEnum( SUBCLASS, HeroSubClass.class );
		Belongings.preview( info, bundle );
	}

	public boolean canHaveTalent(Talent talent) {
		for(LinkedHashMap<Talent,Integer> tier : talents) if(tier.containsKey(talent)) return true;
		return OmniAbility.findTalent(talent) != null;
	}
	public boolean hasTalent( Talent talent ){
		return pointsInTalent(talent) > 0;
	}
	public boolean hasTalent( Talent... talents) {
		for(Talent talent : talents ) {
			if(hasTalent(talent)) return true;
		}
		return false;
	}

	public int pointsInTalent( Talent talent ){
		for (LinkedHashMap<Talent, Integer> tier : talents){
			for (Talent f : tier.keySet()){
				if (f == talent) return tier.get(f);
			}
		}
		Integer omniPoints = OmniAbility.findTalent(talent);
		return omniPoints != null ? omniPoints : 0;
	}
	// stacks was the legacy behavior.
	public final int pointsInTalent(Talent... talents) {
		return pointsInTalent(true, talents);
	}
	public int pointsInTalent(boolean stacks, Talent... talents) {
		int sum = 0;
		for(Talent talent : talents) {
			int points = pointsInTalent(talent);
			sum = stacks ? sum + points : Math.max(sum, points);
		}
		return sum;
	}

	public final float byTalent(Talent t1, float f1, Talent t2, float f2) {
		return byTalent(false, t1, f1, t2, f2);
	}
	public final float byTalent(boolean stacks, Talent t1, float f1, Talent t2, float f2) {
		return byTalent(stacks, false, t1, f1, t2, f2);
	}
	public float byTalent(boolean stacks, boolean shifted, Talent t1, float f1, Talent t2, float f2 ) {
		float r1 = f1 * (shifted ? shiftedPoints(t1) : pointsInTalent(t1)),
			  r2 = f2 * (shifted ? shiftedPoints(t2) : pointsInTalent(t2));
		return stacks ? r1 + r2 : Math.max(r1, r2);
	}

	// I'm... not sure if this is a good idea, but it does make it look a bit better in some situations.
	public void byTalent(Talent.TalentCallback callback, Talent... talents) {
		byTalent(callback, false, talents);
	}
	public void byTalent(Talent.TalentCallback callback, boolean runIf0, Talent... talents) {
		for(Talent talent : talents) if( hasTalent(talent) || runIf0 && canHaveTalent(talent) ) {
			callback.call(talent, pointsInTalent(talent));
		}
	}

	/** shifts [shifted] so that +0 becomes +1 */
	public int shiftedPoints( Talent shifted ) {
		int points = pointsInTalent(shifted);
		if(canHaveTalent(shifted)) points++;
		return points;
	}
	/** shifts [shifted] so that +0 becomes +1, or returns the points in standard */
	public int shiftedPoints( Talent shifted, Talent standard ) {
		return Math.max(shiftedPoints(shifted), pointsInTalent(standard));
	}
	/** gives a free +1 when the heroclass matches [cls] */
	public int pointsInTalentWithInnate( HeroClass cls, Talent... talents) {
		int points = pointsInTalent(talents);
		if(heroClass == cls) points++;
		return points;

	}

	public void upgradeTalent( Talent talent ){
		for (LinkedHashMap<Talent, Integer> tier : talents){
			for (Talent f : tier.keySet()){
				if (f == talent) tier.put(talent, tier.get(talent)+1);
			}
		}
		Talent.onTalentUpgraded(this, talent);
	}

	public int talentPointsSpent(int tier){
		int total = 0;
		for (int i : talents.get(tier-1).values()){
			total += i;
		}
		return total;
	}

	public boolean hasTier(int tier) {
		return !(tier >= Talent.tierLevelThresholds.length);
//		if (lvl < (Talent.tierLevelThresholds[tier] - 1)
//				|| (tier == 3 && subClass == HeroSubClass.NONE)
//				|| (tier == 4 && armorAbility == null));
	}
	public int talentPointsAvailable(int tier){
		if (!hasTier(tier)) {
			return 0;
		} else {
			return Math.min(1 + lvl - Talent.tierLevelThresholds[tier], Talent.getMaxPoints(tier))
					- talentPointsSpent(tier) + bonusTalentPoints(tier);
		}
	}

	public int bonusTalentPoints(int tier){
		if (!hasTier(tier)) {
			return 0;
		} else{
			int point = 0;
			if (buff(PotionOfDivineInspiration.DivineInspirationTracker.class) != null
					&& buff(PotionOfDivineInspiration.DivineInspirationTracker.class).isBoosted(tier)) {
				point += 2;
			}
			if (Dungeon.hero.pointsInTalent(Talent.HEROIC_RATINESS) >= tier) {
				point += 1;
			}
			return point;
		}
	}
	
	public String className() {
		return subClass == null || subClass == HeroSubClass.NONE ? heroClass.title() : subClass.title();
	}

	@Override
	public String name(){
		return className();
	}

	@Override
	public void hitSound(float pitch) {
		if (!RingOfForce.fightingUnarmed(this)) {
			belongings.attackingWeapon().hitSound(pitch);
		} else if (RingOfForce.getBuffedBonus(this, RingOfForce.Force.class) > 0) {
			//pitch deepens by 2.5% (additive) per point of strength, down to 75%
			super.hitSound( pitch * GameMath.gate( 0.75f, 1.25f - 0.025f*STR(), 1f) );
		} else {
			super.hitSound(pitch * 1.1f);
		}
	}

	@Override
	public boolean blockSound(float pitch) {
		if ( (belongings.weapon() != null && belongings.weapon().defenseFactor(this) >= 4) ||
			buff(WarriorParry.BlockTrock.class) != null){
			Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY, 1, pitch);
			return true;
		}
		return super.blockSound(pitch);
	}

	public void live() {
		for (Buff b : buffs()){
			if (!b.revivePersists) b.detach();
		}
		Buff.affect( this, Regeneration.class );
		Buff.affect( this, Hunger.class );
		if (Dungeon.isChallenged(Challenges.FORGET_PATH)){
			Buff.affect(this, AmnesiaBuff.class);
		}
		if (Dungeon.isChallenged(Challenges.BURN)){
			Buff.affect(this, DomainOfHell.class);
		}
		if (Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.ECH)){
			Buff.affect(this, Ech.EchDied.class).depth = -1;
		}
	}
	
	public int tier() {
		Armor armor = belongings.armor();
		if (isSubclassed(HeroSubClass.DECEPTICON)){
			if (RobotBuff.isVehicle())
				return 8 - (heroClass == HeroClass.RAT_KING ? 6 : 0);
			return 7 - (heroClass == HeroClass.RAT_KING ? 6 : 0);
		}
		if (belongings.armor() instanceof ClassArmor){
			return 6;
		} else if (armor != null){
			return armor.tier;
		} else {
			return 0;
		}
	}

	public boolean shoot( Char enemy, MissileWeapon wep ) {

		this.enemy = enemy;
		boolean wasEnemy = enemy.alignment == Alignment.ENEMY
				|| (enemy instanceof Mimic && enemy.alignment == Alignment.NEUTRAL);

		//temporarily set the hero's weapon to the missile weapon being used
		//TODO improve this!
		belongings.thrownWeapon = wep;
		int cell = enemy.pos;
		// this is kinda awkward but I don't know any other way to simulate this properly.
		MeleeWeapon.MeleeAbility ability =
				MeleeWeapon.activeAbility instanceof MeleeWeapon.MeleeAbility ? (MeleeWeapon.MeleeAbility) MeleeWeapon.activeAbility
						: null;
		boolean hit = ability != null ? attack( enemy, ability.dmgMulti(enemy), 0, ability.accMulti() )
				: attack( enemy );
		if (hit && ability != null) ability.onHit(this, enemy);
		wep.onRangedAttack(enemy, cell, hit);
		if (ability != null) ability.afterHit(enemy, hit);
		Invisibility.dispel();
		belongings.thrownWeapon = null;

		if (hit && wasEnemy && subClass.is(HeroSubClass.GLADIATOR)){
			Buff.affect( this, Combo.class ).hit( enemy );
		}

		if (hit && heroClass.is(HeroClass.DUELIST) && wasEnemy){
			Buff.affect( this, Sai.ComboStrikeTracker.class).addHit();
		}

		return hit;
	}
	
	@Override
	public int attackSkill( Char target ) {
		KindOfWeapon wep = belongings.attackingWeapon();
		
		float accuracy = 1;
		accuracy *= RingOfAccuracy.accuracyMultiplier( this );
		if(subClass.isExact(HeroSubClass.SNIPER)) accuracy *= 4/3d; // sniper innate boost

		if(buff(Talent.WarriorLethalMomentumTracker.Chain.class) != null) accuracy *= 2;

		if (buff(Scimitar.SwordDance.class) != null){
			accuracy *= 1.25f;
		}

		if (buff(MonkEnergy.MonkAbility.Focus.FocusBuff.class) != null &&
			MonkEnergy.isFeelingEmpowered(Level.Feeling.TRAPS)){
			accuracy *= 1.4f;
		}

		if (hasTalent(Talent.HEROIC_ADAPTABILITY)){
			float boost = 0f;
			for (int i : PathFinder.NEIGHBOURS8){
				if (Actor.findChar(target.pos + i) != null || !target.cellIsPathable(target.pos + i)){
					boost += 0.03f + 0.035f * pointsInTalent(Talent.HEROIC_ADAPTABILITY);
				}
			}
			accuracy *= 1f + boost;
		}

		if(buff(HighnessBuff.class) != null && buff(HighnessBuff.class).state == HighnessBuff.State.ENERGIZED){
			accuracy *= 1.5f;
		}

		if (hasTalent(Talent.RK_BLESSED)){
			accuracy *= 1f + 0.0075f*pointsInTalent(Talent.RK_BLESSED)*shielding();
		}

		if (!RingOfForce.fightingUnarmed(this)) {
			return (int)(attackSkill * accuracy * wep.accuracyFactor( this, target ));
		} else {
			return (int)(attackSkill * accuracy);
		}
	}
	
	@Override
	public int defenseSkill( Char enemy ) {

		if (buff(Combo.ParryTracker.class) != null){
			if (canAttack(enemy) && !isCharmedBy(enemy)){
				Buff.affect(this, Combo.RiposteTracker.class).enemy = enemy;
			}
			return INFINITE_EVASION;
		}

		if (buff(RoundShield.GuardTracker.class) != null){
			return INFINITE_EVASION;
		}

		float evasion = defenseSkill;
		
		evasion *= RingOfEvasion.evasionMultiplier( this );

		if (buff(Talent.RestoredAgilityTracker.class) != null){
			if (pointsInTalent(Talent.LIQUID_AGILITY) == 1){
				evasion *= 4f;
			} else if (pointsInTalent(Talent.LIQUID_AGILITY) == 2){
				return INFINITE_EVASION;
			}
		}

		if (buff(Quarterstaff.DefensiveStance.class) != null){
			evasion *= 3;
		}

		if (paralysed > 0) {
			evasion /= 2;
		}
		if (RobotBuff.isVehicle()){
			evasion *= 1.33f;
		}

		if (belongings.armor() != null) {
			evasion = belongings.armor().evasionFactor(this, evasion);
		}

		if (pointsInTalent(Talent.PROTEIN_INFUSION) > 0){
			int hunger = buff(Hunger.class).hunger();
			evasion *= 1f + 0.25f*pointsInTalent(Talent.PROTEIN_INFUSION)*((Hunger.STARVING - hunger)/Hunger.STARVING);
		}

		if (hasTalent(Talent.HEROIC_ADAPTABILITY)){
			float boost = 0f;
			for (int i : PathFinder.NEIGHBOURS8){
				if (Actor.findChar(pos + i) == null && cellIsPathable(pos + i)){
					boost += 0.02f + 0.03f * pointsInTalent(Talent.HEROIC_ADAPTABILITY);
				}
			}
			evasion *= 1f + boost;
		}

		if(buff(HighnessBuff.class) != null && buff(HighnessBuff.class).state == HighnessBuff.State.ENERGIZED){
			evasion *= 1.5f;
		}

		if (hasTalent(Talent.RK_BLESSED)){
			evasion *= 1f + 0.005f*pointsInTalent(Talent.RK_BLESSED)*shielding();
		}

		return Math.round(evasion);
	}

	@Override
	public String defenseVerb() {
		Combo.ParryTracker parry = buff(Combo.ParryTracker.class);
		if (parry != null){
			parry.parried = true;
			Combo combo = buff(Combo.class);
			if (combo != null && combo.getComboCount() < 9 && !hasTalent(Talent.ENHANCED_COMBO) || pointsInTalent(Talent.ENHANCED_COMBO,Talent.RK_GLADIATOR) < 2){
				parry.detach();
			}
			return Messages.get(Monk.class, "parried");
		}

		if (buff(RoundShield.GuardTracker.class) != null){
			buff(RoundShield.GuardTracker.class).detach();
			Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(0.96f, 1.05f));
			return Messages.get(RoundShield.GuardTracker.class, "guarded");
		}

		if (buff(MonkEnergy.MonkAbility.Focus.FocusActivation.class) != null){
			buff(MonkEnergy.MonkAbility.Focus.FocusActivation.class).detach();
			if (sprite != null && sprite.visible) {
				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(0.96f, 1.05f));
			}
			if (MonkEnergy.isFeelingEmpowered(Level.Feeling.GRASS)){
				Item grass = new DuelistGrass();
				if (grass.doPickUp(this, pos)) {
					spend(-1.0f); //casting the spell already takes a turn
					GLog.i( Messages.capitalize(Messages.get(this, "you_now_have", grass.name())) );
				} else {
					GLog.w(Messages.get(grass, "cant_grab"));
					Dungeon.level.drop(grass, pos).sprite.drop();
				}
			}
			return Messages.get(Monk.class, "parried");
		}

		return super.defenseVerb();
	}

	@Override
	public int drRoll() {
		int dr = super.drRoll();

		if (belongings.armor() != null) {
			int armDr = Random.NormalIntRange( belongings.armor().DRMin(), belongings.armor().DRMax());
			if (STR() < belongings.armor().STRReq()){
				armDr -= 2*(belongings.armor().STRReq() - STR());
			}
			if (armDr > 0) dr += armDr;
		}
		if (belongings.weapon() != null && !RingOfForce.fightingUnarmed(this))  {
			int wepDr = Random.NormalIntRange( 0 , belongings.weapon().defenseFactor( this ) );
			if (STR() < ((Weapon)belongings.weapon()).STRReq()){
				wepDr -= 2*(((Weapon)belongings.weapon()).STRReq() - STR());
			}
			if (wepDr > 0) dr += wepDr;
		}

		if (buff(HoldFast.class) != null){
			dr += buff(HoldFast.class).armorBonus();
		}

		if (hasTalent(Talent.RK_GIANT)){
			dr = Math.round(dr * (1f + (1f / 3f) * pointsInTalent(Talent.RK_GIANT) * ((float) (HT - HP) / HT)));
		}

		return dr;
	}
	
	@Override
	public int damageRoll() {
		if (RobotBuff.isVehicle()){
			return 0;
		}

		KindOfWeapon wep = belongings.attackingWeapon();
		int dmg;

		if (!RingOfForce.fightingUnarmed(this)) {
			dmg = wep.damageRoll( this );

			if (!(wep instanceof MissileWeapon)) dmg += RingOfForce.armedDamageBonus(this);
		} else {
			dmg = RingOfForce.damageRoll(this);
			if (RingOfForce.unarmedGetsWeaponAugment(this)){
				dmg = ((Weapon)belongings.attackingWeapon()).augment.damageFactor(dmg);
			}
		}

		PhysicalEmpower emp = buff(PhysicalEmpower.class);
		if (emp != null){
			dmg += emp.dmgBoost;
			emp.left--;
			if (emp.left <= 0) {
				emp.detach();
			}
			Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 0.75f, 1.2f);
		}

		if (/*heroClass != HeroClass.DUELIST
				&& */hasTalent(Talent.WEAPON_RECHARGING)
				&& (buff(Recharging.class) != null || buff(ArtifactRecharge.class) != null)){
			float boost = !heroClass.is(HeroClass.DUELIST) ? .05f : .025f;
			dmg = Math.round(dmg * (1 + boost) + (boost*pointsInTalent(Talent.WEAPON_RECHARGING)));
		}

		if (dmg < 0) dmg = 0;
		if (buff(Talent.BigRushTracker.class) != null){
			if (heroClass.is(HeroClass.WARRIOR)) {
				BrokenSeal.WarriorShield shield = buff(BrokenSeal.WarriorShield.class);
				if (shield != null && shield.maxShield() > 0) {
					dmg += Random.IntRange(0, Math.round(pointsInTalent(Talent.BIG_RUSH) * 0.75f * shield.maxShield()));
				}
			} else {
				dmg += Random.IntRange(0, Math.round(HT * (0.04f + Math.round(pointsInTalent(Talent.BIG_RUSH) * 0.035f))));
			}
		}
		if (buff(ChampionEnemy.Projecting.class) != null && wep instanceof MeleeWeapon){
			dmg *= 1f + (pointsInTalent(Talent.RK_PROJECT))/9f;
		}

		return dmg;
	}
	
	@Override
	public float speed() {

		float speed = super.speed();

		speed *= RingOfHaste.speedMultiplier(this);
		
		if (belongings.armor() != null) {
			speed = belongings.armor().speedFactor(this, speed);
		}
		
		Momentum momentum = buff(Momentum.class);
		if (momentum != null){
			((HeroSprite)sprite).sprint( momentum.freerunning() ? 1.5f : 1f );
			speed *= momentum.speedMultiplier();
		} else {
			((HeroSprite)sprite).sprint( 1f );
		}
		if (pointsInTalent(Talent.PROTEIN_INFUSION) > 0){
			int hunger = buff(Hunger.class).hunger();
			speed *= 1f + 0.4f*pointsInTalent(Talent.PROTEIN_INFUSION)*((Hunger.STARVING - hunger)/Hunger.STARVING);
		}
		if (RobotBuff.isVehicle()){
			((HeroSprite)sprite).sprint( 2f );
			speed*=2;
		}

		NaturesPower.naturesPowerTracker natStrength = buff(NaturesPower.naturesPowerTracker.class);
		if (natStrength != null){
			speed *= (2f + /*0.25f*/byTalent(Talent.GROWING_POWER, 0.33f,
												Talent.SILVA_RANGE, 0.25f));
		}

		speed = AscensionChallenge.modifyHeroSpeed(speed);

		return speed;
		
	}

	@Override
	public boolean canSurpriseAttack(){
		KindOfWeapon w = belongings.attackingWeapon();
		if (!(w instanceof Weapon))             return true;
		if (RingOfForce.fightingUnarmed(this))  return true;
		if (STR() < ((Weapon)w).STRReq())       return false;
		if (w instanceof Flail)                 return false;

		return super.canSurpriseAttack();
	}

	public boolean canAttack(Char enemy){
		if(super.canAttack(enemy)) return true;

		KindOfWeapon wep = Dungeon.hero.belongings.attackingWeapon();
		if (RobotBuff.isVehicle()){
			return KindOfWeapon.canReach(this, enemy.pos, 8);
		}

		if (wep != null){
			return wep.canReach(this, enemy.pos);
		}
		else {
			MagesStaff staff = belongings.getItem(MagesStaff.class);
			return distance(enemy) == 2 && staff != null && staff.wandClass() == WandOfDisintegration.class && Random.Int(3) < pointsInTalent(Talent.SORCERY);
		}
	}
	
	public float attackDelay() {
		if (Talent.LethalMomentumTracker.apply(this)){
			return 0;
		}

		float delay = super.attackDelay();

		if (!RingOfForce.fightingUnarmed(this)) {

			delay *= belongings.attackingWeapon().delayFactor( this );
			
		} else {
			//Normally putting furor speed on unarmed attacks would be unnecessary
			//But there's going to be that one guy who gets a furor+force ring combo
			//This is for that one guy, you shall get your fists of fury!
			float speed = RingOfFuror.attackSpeedMultiplier(this);

			//ditto for furor + sword dance!
			if (buff(Scimitar.SwordDance.class) != null){
				speed += 0.6f;
			}

			//and augments + brawler's stance! My goodness, so many options now compared to 2014!
			if (RingOfForce.unarmedGetsWeaponAugment(this)){
				delay = ((Weapon)belongings.weapon).augment.delayFactor(delay);
			}

			delay /= speed;
		}
		if(hasTalent(Talent.ONE_MAN_ARMY)) {
			int enemies = 0;
			for(Char ch : Dungeon.level.mobs) if(ch.alignment == Alignment.ENEMY && (canAttack(ch) || ch.canAttack(this))) enemies++;
			// every additional enemy, not the first guy.
			delay /= 1+0.11*Math.max(0,enemies-1)*pointsInTalent(Talent.ONE_MAN_ARMY);
		}
		return delay;
	}

	@Override
	public void spend( float time ) {
		super.spend(time);
	}

	@Override
	public void spendConstant(float time) {
		justMoved = false;
		super.spendConstant(time);
	}

	public void spendAndNextConstant(float time ) {
		busy();
		spendConstant( time );
		next();
	}

	public void spendAndNext( float time ) {
		busy();
		spend( time );
		next();
	}
	
	@Override
	public boolean act() {
		
		//calls to dungeon.observe will also update hero's local FOV.
		fieldOfView = Dungeon.level.heroFOV;

		if (buff(Endure.EndureTracker.class) != null){
			buff(Endure.EndureTracker.class).endEnduring();
		}

		if (!ready) {
			//do a full observe (including fog update) if not resting.
			if (!resting || buff(MindVision.class) != null || buff(Awareness.class) != null) {
				Dungeon.observe();
			} else {
				//otherwise just directly re-calculate FOV
				Dungeon.level.updateFieldOfView(this, fieldOfView);
			}
		}
		
		checkVisibleMobs();
		BuffIndicator.refreshHero();
		BuffIndicator.refreshBoss();

		if (paralysed > 0) {
			
			curAction = null;
			
			spendAndNext( TICK );
			return false;
		}
		
		boolean actResult;
		if (curAction == null) {
			
			if (resting) {
				spendConstant( TIME_TO_REST );
				next();
			} else {
				ready();
			}

			//if we just loaded into a level and have a search buff, make sure to process them
			if(Actor.now() == 0){
				if (buff(Foresight.class) != null){
					search(false);
				} else if (buff(TalismanOfForesight.Foresight.class) != null){
					buff(TalismanOfForesight.Foresight.class).checkAwareness();
				}
			}

			actResult = false;
			
		} else {
			
			resting = false;
			
			ready = false;
			
			if (curAction instanceof HeroAction.Move) {
				actResult = actMove( (HeroAction.Move)curAction );
				
			} else if (curAction instanceof HeroAction.Interact) {
				actResult = actInteract( (HeroAction.Interact)curAction );
				
			} else if (curAction instanceof HeroAction.Buy) {
				actResult = actBuy( (HeroAction.Buy)curAction );
				
			}else if (curAction instanceof HeroAction.PickUp) {
				actResult = actPickUp( (HeroAction.PickUp)curAction );
				
			} else if (curAction instanceof HeroAction.OpenChest) {
				actResult = actOpenChest( (HeroAction.OpenChest)curAction );
				
			} else if (curAction instanceof HeroAction.Unlock) {
				actResult = actUnlock((HeroAction.Unlock) curAction);
				
			} else if (curAction instanceof HeroAction.Mine) {
				actResult = actMine( (HeroAction.Mine)curAction );

			}else if (curAction instanceof HeroAction.LvlTransition) {
				actResult = actTransition( (HeroAction.LvlTransition)curAction );
				
			} else if (curAction instanceof HeroAction.Attack) {
				actResult = actAttack( (HeroAction.Attack)curAction );
				
			} else if (curAction instanceof HeroAction.Alchemy) {
				actResult = actAlchemy( (HeroAction.Alchemy)curAction );
				
			} else {
				actResult = false;
			}
		}
		
		if(shiftedPoints(Talent.BARKSKIN,Talent.RK_WARDEN) > 0 && Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
			Barkskin.conditionallyAppend(this, Barkskin.getGrassDuration(this), 1 );
		}
		if (buff(DomainOfHell.class) == null && Dungeon.isChallenged(Challenges.BURN))
			Buff.affect(this, DomainOfHell.class);

		if (belongings.weapon instanceof NuclearHatchet){
			Buff.affect(this, NuclearHatchet.Effect.class).set(1.1f);
		}
		if (subClass.is(HeroSubClass.BRAWLER) && buff(BrawlerBuff.class) == null){
			Buff.affect(this, BrawlerBuff.class);
		}
		if (subClass.is(HeroSubClass.SPIRITUALIST) && buff(SpiritBuff.class) == null){
			Buff.affect(this, SpiritBuff.class);
		}
		if (subClass.is(HeroSubClass.DECEPTICON) && buff(RobotBuff.class) == null){
			Buff.affect(this, RobotBuff.class);
			((HeroSprite)sprite).updateArmor();
		}
		if (subClass.is(HeroSubClass.RK_CHAMPION) && buff(RKChampionBuff.class) == null){
			Buff.affect(this, RKChampionBuff.class);
		}
		if (subClass.is(HeroSubClass.HIGHNESS) && buff(HighnessBuff.class) == null){
			Buff.affect(this, HighnessBuff.class);
		}

		return actResult;
	}
	
	public void busy() {
		ready = false;
	}
	
	public void ready() {
		if (sprite.looping()) sprite.idle();
		curAction = null;
		damageInterrupt = true;
		waitOrPickup = false;
		ready = true;
		canSelfTrample = true;

		AttackIndicator.updateState();

		GameScene.ready();
	}
	
	public void interrupt() {
		if (isAlive() && curAction != null &&
			((curAction instanceof HeroAction.Move && curAction.dst != pos) ||
			(curAction instanceof HeroAction.LvlTransition))) {
			lastAction = curAction;
		}
		curAction = null;
		GameScene.resetKeyHold();
		if (resting && hasTalent(Talent.ADVENTUROUS_SNOOZING) && !heroClass.is(HeroClass.DUELIST) && buff(Talent.AdventurousSnoozingAntiAbuse.class) == null){
			HP = Math.min(HT, HP + 1 + 2 * pointsInTalent(Talent.ADVENTUROUS_SNOOZING));

			sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(1 + 2 * pointsInTalent(Talent.ADVENTUROUS_SNOOZING)), FloatingText.HEALING);
		}
		resting = false;
	}
	
	public void resume() {
		curAction = lastAction;
		lastAction = null;
		damageInterrupt = false;
		next();
	}

	private boolean canSelfTrample = false;
	public boolean canSelfTrample(){
		return canSelfTrample && !rooted && !flying &&
				//standing in high grass
				(Dungeon.level.map[pos] == Terrain.HIGH_GRASS ||
				//standing in furrowed grass and not huntress
				(!heroClass.is(HeroClass.HUNTRESS) && Dungeon.level.map[pos] == Terrain.FURROWED_GRASS) ||
				//standing on a plant
				Dungeon.level.plants.get(pos) != null);
	}

	private boolean actMove( HeroAction.Move action ) {

		if (getCloser( action.dst )) {
			canSelfTrample = false;
			return true;

		//Hero moves in place if there is grass to trample
		} else if (pos == action.dst && canSelfTrample()){
			canSelfTrample = false;
			Dungeon.level.pressCell(pos);
			spendAndNext( 1 / speed() );
			return false;
		} else {
			// Rat King room logic might as well go here.
			if(Dungeon.level.map[action.dst] == Terrain.SIGN) {
				if(Dungeon.depth == 5) Game.runOnRenderThread(()->GameScene.show( new WndMessage( "Home Sweet Home!")));
			}
			ready();
			return false;
		}
	}
	
	private boolean actInteract( HeroAction.Interact action ) {
		
		Char ch = action.ch;

		if (ch.isAlive() && ch.canInteract(this)) {
			
			ready();
			sprite.turnTo( pos, ch.pos );
			return ch.interact(this);
			
		} else {
			
			if (fieldOfView[ch.pos] && getCloser( ch.pos )) {

				return true;

			} else {
				ready();
				return false;
			}
			
		}
	}
	
	private boolean actBuy( HeroAction.Buy action ) {
		int dst = action.dst;
		if (pos == dst) {

			ready();
			
			Heap heap = Dungeon.level.heaps.get( dst );
			if (heap != null && heap.type == Type.FOR_SALE && heap.size() == 1) {
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndTradeItem( heap ) );
					}
				});
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actAlchemy( HeroAction.Alchemy action ) {
		int dst = action.dst;
		if (Dungeon.level.distance(dst, pos) <= 1) {

			ready();
			if (Dungeon.isChallenged(Challenges.NO_ALCHEMY)){
				GLog.w( Messages.get(AlchemistsToolkit.class, "omni_cursed"));
				return false;
			}

			AlchemistsToolkit.kitEnergy kit = buff(AlchemistsToolkit.kitEnergy.class);
			if (kit != null && kit.isCursed()){
				GLog.w( Messages.get(AlchemistsToolkit.class, "cursed"));
				return false;
			}

			AlchemyScene.clearToolkit();
			ShatteredPixelDungeon.switchScene(AlchemyScene.class);
			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	//used to keep track if the wait/pickup action was used
	// so that the hero spends a turn even if the fail to pick up an item
	public boolean waitOrPickup = false;

	private boolean actPickUp( HeroAction.PickUp action ) {
		int dst = action.dst;
		if (pos == dst) {

			Heap heap = Dungeon.level.heaps.get( pos );
			if (heap != null) {
				Item item = heap.peek();
				if (item.doPickUp( this )) {
					heap.pickUp();

					if (item instanceof Dewdrop
							|| item instanceof TimekeepersHourglass.sandBag
							|| item instanceof DriedRose.Petal
							|| item instanceof Key
							|| item instanceof Guidebook
							|| item instanceof AbyssalSpawner.Clump) {
						//Do Nothing
					} else if (item instanceof DarkGold) {
						DarkGold existing = belongings.getItem(DarkGold.class);
						if (existing != null){
							if (existing.quantity() >= Blacksmith.Quest.MAX_GOLD) {
								GLog.p(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
							} else {
								GLog.i(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
							}
						}
					} else {

						//TODO make all unique items important? or just POS / SOU?
						boolean important = item.unique && item.isIdentified() &&
								(item instanceof Scroll || item instanceof Potion);
						if (important) {
							GLog.p( Messages.capitalize(Messages.get(this, "you_now_have", item.name())) );
						} else {
							GLog.i( Messages.capitalize(Messages.get(this, "you_now_have", item.name())) );
						}
					}

					curAction = null;
				} else {

					if (waitOrPickup) {
						spendAndNextConstant(TIME_TO_REST);
					}

					//allow the hero to move between levels even if they can't collect the item
					if (Dungeon.level.getTransition(pos) != null){
						throwItems();
					} else {
						heap.sprite.drop();
					}

					if (item instanceof Dewdrop
							|| item instanceof TimekeepersHourglass.sandBag
							|| item instanceof DriedRose.Petal
							|| item instanceof Key) {
						//Do Nothing
					} else {
						GLog.newLine();
						GLog.n(Messages.capitalize(Messages.get(this, "you_cant_have", item.name())));
					}

					ready();
				}
			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actOpenChest( HeroAction.OpenChest action ) {
		int dst = action.dst;
		if (Dungeon.level.adjacent( pos, dst ) || pos == dst) {
			path = null;

			Heap heap = Dungeon.level.heaps.get( dst );
			if (heap != null && (heap.type != Type.HEAP && heap.type != Type.FOR_SALE)) {
				
				if ((heap.type == Type.LOCKED_CHEST && Notes.keyCount(new GoldenKey(Dungeon.depth)) < 1)
					|| (heap.type == Type.CRYSTAL_CHEST && Notes.keyCount(new CrystalKey(Dungeon.depth)) < 1)){

						GLog.w( Messages.get(this, "locked_chest") );
						ready();
						return false;

				}
				
				switch (heap.type) {
				case TOMB:
					Sample.INSTANCE.play( Assets.Sounds.TOMB );
					PixelScene.shake( 1, 0.5f );
					break;
				case SKELETON:
				case REMAINS:
					break;
				default:
					Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
				}
				
				sprite.operate( dst );
				
			} else {
				ready();
			}

			return false;

		} else if (getCloser( dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actUnlock( HeroAction.Unlock action ) {
		int doorCell = action.dst;
		if (Dungeon.level.adjacent( pos, doorCell )) {
			path = null;

			boolean hasKey = false;
			int door = Dungeon.level.map[doorCell];
			
			if (door == Terrain.LOCKED_DOOR
					&& Notes.keyCount(new IronKey(Dungeon.depth)) > 0) {
				
				hasKey = true;

			} else if (door == Terrain.CRYSTAL_DOOR
					&& Notes.keyCount(new CrystalKey(Dungeon.depth)) > 0) {

				hasKey = true;

			} else if (door == Terrain.LOCKED_EXIT
					&& Notes.keyCount(new SkeletonKey(Dungeon.depth)) > 0) {

				hasKey = true;
				
			}
			
			if (hasKey) {
				
				sprite.operate( doorCell );
				
				Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
				
			} else {
				GLog.w( Messages.get(this, "locked_door") );
				ready();
			}

			return false;

		} else if (getCloser( doorCell )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	public boolean actMine(HeroAction.Mine action){
		if (Dungeon.level.adjacent(pos, action.dst)){
			path = null;
			if ((Dungeon.level.map[action.dst] == Terrain.WALL
					|| Dungeon.level.map[action.dst] == Terrain.WALL_DECO
					|| Dungeon.level.map[action.dst] == Terrain.MINE_CRYSTAL
					|| Dungeon.level.map[action.dst] == Terrain.MINE_BOULDER)
				&& Dungeon.level.insideMap(action.dst)){
				sprite.attack(action.dst, new Callback() {
					@Override
					public void call() {

						boolean crystalAdjacent = false;
						for (int i : PathFinder.NEIGHBOURS8) {
							if (Dungeon.level.map[action.dst + i] == Terrain.MINE_CRYSTAL){
								crystalAdjacent = true;
								break;
							}
						}

						//1 hunger spent total
						if (Dungeon.level.map[action.dst] == Terrain.WALL_DECO){
							DarkGold gold = new DarkGold();
							if (gold.doPickUp( Dungeon.hero )) {
								DarkGold existing = Dungeon.hero.belongings.getItem(DarkGold.class);
								if (existing != null && existing.quantity()%5 == 0){
									if (existing.quantity() >= 40) {
										GLog.p(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
									} else {
										GLog.i(Messages.get(DarkGold.class, "you_now_have", existing.quantity()));
									}
								}
								spend(-Actor.TICK); //picking up the gold doesn't spend a turn here
							} else {
								Dungeon.level.drop( gold, pos ).sprite.drop();
							}
							PixelScene.shake(0.5f, 0.5f);
							CellEmitter.center( action.dst ).burst( Speck.factory( Speck.STAR ), 7 );
							Sample.INSTANCE.play( Assets.Sounds.EVOKE );
							Level.set( action.dst, Terrain.EMPTY_DECO );

							//mining gold doesn't break crystals
							crystalAdjacent = false;

						//4 hunger spent total
						} else if (Dungeon.level.map[action.dst] == Terrain.WALL){
							buff(Hunger.class).affectHunger(-3);
							PixelScene.shake(0.5f, 0.5f);
							CellEmitter.get( action.dst ).burst( Speck.factory( Speck.ROCK ), 2 );
							Sample.INSTANCE.play( Assets.Sounds.MINE );
							Level.set( action.dst, Terrain.EMPTY_DECO );

						//1 hunger spent total
						} else if (Dungeon.level.map[action.dst] == Terrain.MINE_CRYSTAL){
							Splash.at(action.dst, 0xFFFFFF, 5);
							Sample.INSTANCE.play( Assets.Sounds.SHATTER );
							Level.set( action.dst, Terrain.EMPTY );

						//1 hunger spent total
						} else if (Dungeon.level.map[action.dst] == Terrain.MINE_BOULDER){
							Splash.at(action.dst, 0x555555, 5);
							Sample.INSTANCE.play( Assets.Sounds.MINE, 0.6f );
							Level.set( action.dst, Terrain.EMPTY_DECO );
						}

						for (int i : PathFinder.NEIGHBOURS9) {
							Dungeon.level.discoverable[action.dst + i] = true;
						}
						for (int i : PathFinder.NEIGHBOURS9) {
							GameScene.updateMap( action.dst+i );
						}

						if (crystalAdjacent){
							sprite.parent.add(new Delayer(0.2f){
								@Override
								protected void onComplete() {
									boolean broke = false;
									for (int i : PathFinder.NEIGHBOURS8) {
										if (Dungeon.level.map[action.dst+i] == Terrain.MINE_CRYSTAL){
											Splash.at(action.dst+i, 0xFFFFFF, 5);
											Level.set( action.dst+i, Terrain.EMPTY );
											broke = true;
										}
									}
									if (broke){
										Sample.INSTANCE.play( Assets.Sounds.SHATTER );
									}

									for (int i : PathFinder.NEIGHBOURS9) {
										GameScene.updateMap( action.dst+i );
									}
									spendAndNext(TICK);
									ready();
								}
							});
						} else {
							spendAndNext(TICK);
							ready();
						}

						Dungeon.observe();
					}
				});
			} else {
				ready();
			}
			return false;
		} else if (getCloser( action.dst )) {

			return true;

		} else {
			ready();
			return false;
		}
	}

	private boolean actTransition(HeroAction.LvlTransition action ) {
		int stairs = action.dst;
		LevelTransition transition = Dungeon.level.getTransition(stairs);

		if (rooted) {
			PixelScene.shake(1, 1f);
			ready();
			return false;

		} else if (!Dungeon.level.locked && transition != null && transition.inside(pos)) {

			if (Dungeon.level.activateTransition(this, transition)){
				curAction = null;
			} else {
				ready();
			}

			return false;

		} else if (getCloser( stairs )) {

			return true;

		} else {
			ready();
			return false;
		}
	}
	
	private boolean actAttack( HeroAction.Attack action ) {

		enemy = action.target;

		if (enemy.isAlive() && canAttack( enemy ) && !isCharmedBy( enemy ) && enemy.invisible == 0) {

			if (!heroClass.is(HeroClass.DUELIST)
					&& hasTalent(Talent.AGGRESSIVE_BARRIER)
					&& buff(Talent.AggressiveBarrierCooldown.class) == null
					&& (HP / (float)HT) < 0.20f*(1+pointsInTalent(Talent.AGGRESSIVE_BARRIER))){
				Buff.affect(this, Barrier.class).setShield(5);
				sprite.showStatusWithIcon(CharSprite.POSITIVE, "5", FloatingText.SHIELDING);
				Buff.affect(this, Talent.AggressiveBarrierCooldown.class, 50f);

			}
			sprite.attack( enemy.pos );

			return false;

		} else {

			if (fieldOfView[enemy.pos] && getCloser( enemy.pos )) {

				return true;

			} else {
				ready();
				return false;
			}

		}
	}

	public Char enemy(){
		return enemy;
	}
	
	public void rest( boolean fullRest ) {
		spendAndNextConstant( TIME_TO_REST );
		if (hasTalent(Talent.HOLD_FAST,Talent.RK_BERSERKER)){
			Buff.affect(this, HoldFast.class).pos = pos;
		}
		if (hasTalent(Talent.PATIENT_STRIKE)){
			Buff.affect(Dungeon.hero, Talent.PatientStrikeTracker.class).pos = Dungeon.hero.pos;
		}
		if (!fullRest) {
			if (sprite != null) {
				sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "wait"));
			}
		} else {
			if (hasTalent(Talent.ADVENTUROUS_SNOOZING) && !heroClass.is(HeroClass.DUELIST)){
				Buff.affect(this, Talent.AdventurousSnoozingAntiAbuse.class, 2f);
			}
		}
		resting = fullRest;
	}
	
	@Override
	public int attackProc( final Char enemy, int damage ) {
		Talent.AssassinLethalMomentumTracker.process(enemy);

		damage = super.attackProc( enemy, damage );

		KindOfWeapon wep;
		if (RingOfForce.fightingUnarmed(this) && !RingOfForce.unarmedGetsWeaponEnchantment(this)){
			wep = null;
		} else {
			wep = belongings.attackingWeapon();
		}

		float mult = Talent.SpiritBladesTracker.getProcModifier();

		if (RobotBuff.isVehicle() && Dungeon.hero.belongings.thrownWeapon == null){
			damage = Random.NormalIntRange(Math.max(0, STR()-12), Math.max(0, STR()-7));
			int points = Dungeon.hero.pointsInTalent(Talent.ENERGON_FUSION);
			if (points > 0){
				for (Buff b : Dungeon.hero.buffs()){
					if (b instanceof Artifact.ArtifactBuff) ((Artifact.ArtifactBuff) b).charge(Dungeon.hero, points/6f);
					if (b instanceof Wand.Charger) ((Wand.Charger) b).charge(Dungeon.hero, points/6f);
				}
			}
			if (Dungeon.hero.hasTalent(Talent.ERADICATING_CHARGE)){
				Buff.affect(this, Barrier.class).incShield(Dungeon.hero.pointsInTalent(Talent.ERADICATING_CHARGE));
				if (Random.Int(10) < Dungeon.hero.pointsInTalent(Talent.ERADICATING_CHARGE)){
					Buff.affect(enemy, Slow.class, 3f);
				}
			}
		}

		// subclass logic here
        if (subClass.is(HeroSubClass.BATTLEMAGE)) {
            MagesStaff staff = belongings.getItem(MagesStaff.class);
            if (staff != null && (staff == wep || hasTalent(Talent.SORCERY))&& (mult == 1 || Random.Float() < mult)){
					if(staff == wep || Random.Int(5) < pointsInTalent(Talent.SORCERY)) {
                    staff.procBM();
                }
                if (staff == wep || Random.Int(3) < pointsInTalent(Talent.SORCERY))
                    if (buff(Talent.EmpoweredStrikeTracker.class) != null) {
                        buff(Talent.EmpoweredStrikeTracker.class).detach();
                        damage = Math.round(damage * (1f + byTalent(
                                Talent.EMPOWERED_STRIKE, 1/4f,
                                Talent.RK_BATTLEMAGE, 1/6f)
									));
                    }
                staff.procWand(enemy, damage);
            }
        }
		if (wep != null) damage = wep.proc( this, enemy, damage );
		if (hasTalent(Talent.HEROIC_ENDURANCE)){
			Armor armor = belongings.armor();
			if (armor != null){
				if (armor.glyph != null && Random.Int(3) < pointsInTalent(Talent.HEROIC_ENDURANCE)){
					if (armor.glyph.beneficial)
						damage = armor.glyph.proc(armor, enemy, this, damage);
					else
						damage = armor.glyph.proc(armor, this, enemy, damage);
				}
			}
		}
		RingOfForce.Force forceBuff = buff(RingOfForce.Force.class);
		if (forceBuff != null && !(wep instanceof MissileWeapon)) {
			if (wep != null){
				damage += RingOfForce.armedDamageBonus(this);
			}
		}

		Talent.SpiritBladesTracker tracker = buff(Talent.SpiritBladesTracker.class);
		if ( tracker != null && Random.Float() < 3*pointsInTalent(Talent.SPIRIT_BLADES, Talent.SEA_OF_BLADES) * mult ){
			SpiritBow bow = belongings.getItem(SpiritBow.class);
			if (bow != null) damage = bow.proc( this, enemy, damage );
			tracker.detach();
		}

		damage = Talent.onAttackProc( this, enemy, damage );

		if (buff(ChampionEnemy.Blessed.class) != null && hasTalent(Talent.RK_BLESSED) && wep instanceof MeleeWeapon){
			PathFinder.buildDistanceMap(enemy.pos, BArray.not(Dungeon.level.solid, null), pointsInTalent(Talent.RK_BLESSED));
			ShieldHalo shield;
			GameScene.effect(shield = new ShieldHalo(enemy.sprite));
			shield.hardlight(0xFFFF00);
			shield.putOut();
			for (Char ch : Actor.chars()) {
				if (ch != enemy && ch.alignment == Char.Alignment.ENEMY
						&& PathFinder.distance[ch.pos] < Integer.MAX_VALUE) {
					int aoeHit = Math.round(damage * (1.20f + 0.05f*pointsInTalent(Talent.RK_BLESSED)));
					aoeHit -= ch.drRoll();
					if (ch.buff(Vulnerable.class) != null) aoeHit *= 1.33f;
					ch.damage(aoeHit, this);
					ch.sprite.bloodBurstA(sprite.center(), aoeHit);
					ch.sprite.flash();
					new Flare( 7, 20 ).color( 0xFFFF00, true ).show( ch.sprite, 1f );
				}
			}
		}
		if (buff(ChampionEnemy.Giant.class) != null && hasTalent(Talent.RK_GIANT) ){
			int conservedDamage = 0;
			if (buff(Kinetic.ConservedDamage.class) != null) {
				conservedDamage = buff(Kinetic.ConservedDamage.class).damageBonus();
				buff(Kinetic.ConservedDamage.class).detach();
			}

			damage += conservedDamage;
		}

        if (subClass.is(HeroSubClass.SNIPER)) {
            if (wep instanceof MissileWeapon && !(wep instanceof SpiritBow.SpiritArrow) && enemy != this) {
                Actor.add(new Actor() {

                    {
                        actPriority = VFX_PRIO;
                    }

                    @Override
                    protected boolean act() {
                        if (enemy.isAlive() || hasTalent(Talent.MULTISHOT)) {
                            int level = hasTalent(Talent.RK_SNIPER) || canHaveTalent(Talent.SHARED_UPGRADES) ? wep.buffedLvl() : 0;
                            SnipersMark .add(enemy, level);
                            // handles dead as well.
                        }
                        Actor.remove(this);
                        return true;
                    }
                });
            }
        }

        if (buff(KromerPotion.Effect.class) != null){
			if (enemy != null) {
				int dmg = Random.Int(0, damage*2);
				Char toHeal, toDamage;

                if (Random.Int(3) == 0) {
                    toHeal = enemy;
                    toDamage = this;
                } else {
                    toHeal = this;
                    toDamage = enemy;
                }
                toHeal.HP = Math.min(toHeal.HT, toHeal.HP + dmg);
				toHeal.sprite.emitter().burst(Speck.factory(Speck.HEALING), 3);

				if (toDamage == Dungeon.hero) {
					Sample.INSTANCE.play(Assets.Sounds.MIMIC, 1f, 2f);
					Warp.inflict(dmg, 0.5f);
				} else {
					Sample.INSTANCE.play(Assets.Sounds.DEGRADE, 1f, 2.5f);
					toDamage.damage(dmg, toHeal);
					toDamage.sprite.emitter().start(ExoParticle.FACTORY, 0.05f, 10);
				}
			}

		}

		if(buff(HighnessBuff.class) != null && buff(HighnessBuff.class).state == HighnessBuff.State.ENERGIZED){
			damage *= 1.33f;
		}
		
		return damage;
	}
	
	@Override
	public int defenseProc( Char enemy, int damage ) {
		
		if (damage > 0 && subClass.is(HeroSubClass.BERSERKER)){
			Berserk berserk = Buff.affect(this, Berserk.class);
			berserk.damage(damage);
		}
		if (buff(RobotBuff.ResistanceTracker.class) != null && pointsInTalent(Talent.VOID_WRATH) > 1){
			Buff.affect(enemy, Burning.class).reignite(enemy);
		}

		if (belongings.armor() != null) {
			damage = belongings.armor().proc( enemy, this, damage );
		}

		WandOfLivingEarth.RockArmor rockArmor = buff(WandOfLivingEarth.RockArmor.class);
		if (rockArmor != null) {
			damage = rockArmor.absorb(damage);
		}

		if (Dungeon.isChallenged(Challenges.FORGET_PATH) && Random.Int(5) == 0){
			Buff.affect(this, Blindness.class, 5f);
		}

		return super.defenseProc( enemy, damage );
	}
	
	@Override
	public void damage( int dmg, Object src ) {
		if (buff(TimekeepersHourglass.Stasis.class) != null)
			return;

		//regular damage interrupt, triggers on any damage except specific mild DOT effects
		// unless the player recently hit 'continue moving', in which case this is ignored
		if (!(src instanceof Hunger || src instanceof Viscosity.DeferedDamage) && damageInterrupt) {
			interrupt();
		}

		if (this.buff(Drowsy.class) != null){
			Buff.detach(this, Drowsy.class);
			GLog.w( Messages.get(this, "pain_resist") );
		}

		Endure.EndureTracker endure = buff(Endure.EndureTracker.class);
		if (!(src instanceof Char)){
			//reduce damage here if it isn't coming from a character (if it is we already reduced it)
			if (endure != null){
				dmg = Math.round(endure.adjustDamageTaken(dmg));
			}
			//the same also applies to challenge scroll damage reduction
			if (buff(ScrollOfChallenge.ChallengeArena.class) != null){
				dmg *= 0.67f;
			}
			//and to monk meditate damage reduction
			if (buff(MonkEnergy.MonkAbility.Meditate.MeditateResistance.class) != null){
				dmg *= 0.2f;
			}
			if (buff(DuelistGrass.GrassitateResistance.class) != null){
				dmg *= 0.6f;
			}
		}

		CapeOfThorns.Thorns thorns = buff( CapeOfThorns.Thorns.class );
		if (thorns != null) {
			dmg = thorns.proc(dmg, (src instanceof Char ? (Char)src : null),  this);
		}
		// berserker gets rage from all sources. all hail viscosity!
		// TODO change for 0.9.2?
		if (!(src instanceof Char)) {
			if (subClass.isExact(HeroSubClass.BERSERKER) && hasTalent(Talent.INDISCRIMINATE_RAGE)) {
				Buff.affect(this, Berserk.class).damage(Math.round(dmg * 0.2f * pointsInTalent(Talent.INDISCRIMINATE_RAGE)));
			}
		}

		if ((src instanceof Electricity || src instanceof Elemental.ShockElemental || src instanceof WandOfLightning) && pointsInTalent(Talent.FARADAY_CAGE) > 1){
			dmg /= 2;
		}

		dmg = (int)Math.ceil(dmg * RingOfTenacity.damageMultiplier( this ));

		//TODO improve this when I have proper damage source logic
		if (belongings.armor() != null && belongings.armor().hasGlyph(AntiMagic.class, this)
				&& AntiMagic.RESISTS.contains(src.getClass())){
			dmg -= AntiMagic.drRoll(this, belongings.armor(). glyphEffectLevel(this));
			dmg = Math.max(dmg, 0);
		}

		if (buff(Talent.WarriorFoodImmunity.class) != null){
			int points = pointsInTalent(Talent.IRON_STOMACH,Talent.ROYAL_MEAL);
			if (points == 1)    dmg = Math.round(dmg*0.25f);
			if (points == 2)  	dmg = Math.round(dmg*0.00f);
		}

		if (buff(RobotBuff.ResistanceTracker.class) != null){
			dmg /= 4;
		}

		if(buff(HighnessBuff.class) != null){
			HighnessBuff buff = buff(HighnessBuff.class);
			if (buff.state == HighnessBuff.State.ENERGIZED){
				dmg *= 0.67f;
			} else if (buff.state == HighnessBuff.State.RECOVERING && pointsInTalent(Talent.WOUND_IGNORANCE) > 2 &&
				RingOfElements.RESISTS.contains(src.getClass())){
				dmg *= 0.5f;
			}
		}

		if (Dungeon.isChallenged(Challenges.NO_HP) && dmg > 0){
			sprite.emitter().burst(GodfireParticle.FACTORY, 14);
			Sample.INSTANCE.play(Assets.Sounds.BLAST, 1f, 1.5f);
			Buff.count(this, HPDebuff.class, dmg / 2f);
		}

		int preHP = HP + shielding();
		if (src instanceof Hunger) preHP -= shielding();
		super.damage( dmg, src );
		int postHP = HP + shielding();
		if (src instanceof Hunger) postHP -= shielding();
		int effectiveDamage = preHP - postHP;

		if (effectiveDamage <= 0) return;

		if (buff(Challenge.DuelParticipant.class) != null){
			buff(Challenge.DuelParticipant.class).addDamage(effectiveDamage);
		}

		if (HighnessBuff.isEnergized() && pointsInTalent(Talent.WOUND_IGNORANCE) > 1){
			int shieldToGive = effectiveDamage / 13;
			if (shieldToGive > 0) {
				Buff.affect(this, Barrier.class).setShield(shieldToGive);
				Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
			}
		}

		//flash red when hit for serious damage.
		float percentDMG = effectiveDamage / (float)preHP; //percent of current HP that was taken
		float percentHP = 1 - ((HT - postHP) / (float)HT); //percent health after damage was taken
		// The flash intensity increases primarily based on damage taken and secondarily on missing HP.
		float flashIntensity = 0.25f * (percentDMG * percentDMG) / percentHP;
		//if the intensity is very low don't flash at all
		if (flashIntensity >= 0.05f){
			flashIntensity = Math.min(1/3f, flashIntensity); //cap intensity at 1/3
			GameScene.flash( (int)(0xFF*flashIntensity) << 16 );
			if (isAlive()) {
				if (flashIntensity >= 1/6f) {
					Sample.INSTANCE.play(Assets.Sounds.HEALTH_CRITICAL, 1/3f + flashIntensity * 2f);
				} else {
					Sample.INSTANCE.play(Assets.Sounds.HEALTH_WARN, 1/3f + flashIntensity * 4f);
				}
				//hero gets interrupted on taking serious damage, regardless of any other factor
				interrupt();
				damageInterrupt = true;
			}
		}
	}
	
	public void checkVisibleMobs() {
		ArrayList<Mob> visible = new ArrayList<>();

		boolean newMob = false;

		Mob target = null;
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (fieldOfView[m.pos] && m.alignment == Alignment.ENEMY && (!(m instanceof Phantom) || m.enemy == Dungeon.hero)) {
				visible.add(m);
				if (!visibleEnemies.contains( m )) {
					newMob = true;
				}

				if (!mindVisionEnemies.contains(m) && QuickSlotButton.autoAim(m) != -1){
					if (target == null){
						target = m;
					} else if (distance(target) > distance(m)) {
						target = m;
					}
					if (m instanceof Snake && Dungeon.level.distance(m.pos, pos) <= 4
							&& !Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_EXAMINING)){
						GLog.p(Messages.get(Guidebook.class, "hint"));
						GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_EXAMINING);
						//we set to read here to prevent this message popping up a bunch
						Document.ADVENTURERS_GUIDE.readPage(Document.GUIDE_EXAMINING);
					}
				}
			}
		}

		Char lastTarget = QuickSlotButton.lastTarget;
		if (target != null && (lastTarget == null ||
				!lastTarget.isAlive() || !lastTarget.isActive() ||
				lastTarget.alignment == Alignment.ALLY ||
				!fieldOfView[lastTarget.pos]) && !(target instanceof Phantom)){
			QuickSlotButton.target(target);
		}
		
		if (newMob) {
			if (resting){
				Dungeon.observe();
			}
			interrupt();
		}

		visibleEnemies = visible;
	}
	
	public int visibleEnemies() {
		return visibleEnemies.size();
	}
	
	public Mob visibleEnemy( int index ) {
		return visibleEnemies.get(index % visibleEnemies.size());
	}
	public boolean visibleEnemy( Mob mob ) {
		return visibleEnemies.contains(mob);
	}

	public ArrayList<Mob> getVisibleEnemies(){
		return new ArrayList<>(visibleEnemies);
	}

	private boolean walkingToVisibleTrapInFog = false;
	
	//FIXME this is a fairly crude way to track this, really it would be nice to have a short
	//history of hero actions
	public boolean justMoved = false;
	
	private boolean getCloser( final int target ) {

		if (target == pos)
			return false;

		if (rooted) {
			PixelScene.shake( 1, 1f );
			return false;
		}

		if (buff(ChampionEnemy.Paladin.class) != null){
			return false;
		}

		int step = -1;
		
		if (Dungeon.level.adjacent( pos, target )) {

			path = null;

			if (Actor.findChar( target ) == null) {
				if (Dungeon.level.passable[target] || Dungeon.level.avoid[target]) {
					step = target;
				}
				if (walkingToVisibleTrapInFog
						&& Dungeon.level.traps.get(target) != null
						&& Dungeon.level.traps.get(target).visible){
					return false;
				}
			}
			
		} else {

			boolean newPath = false;
			if (path == null || path.isEmpty() || !Dungeon.level.adjacent(pos, path.getFirst()))
				newPath = true;
			else if (path.getLast() != target)
				newPath = true;
			else {
				if (!Dungeon.level.passable[path.get(0)]) {
					newPath = true;
				} else if (Actor.findChar(path.get(0)) != null) {
					newPath = true;
				}
			}

			if (newPath) {

				int len = Dungeon.level.length();
				boolean[] p = Dungeon.level.passable;
				boolean[] v = Dungeon.level.visited;
				boolean[] m = Dungeon.level.mapped;
				boolean[] passable = new boolean[len];
				for (int i = 0; i < len; i++) {
					passable[i] = p[i] && (v[i] || m[i]);
				}

				PathFinder.Path newpath = Dungeon.findPath(this, target, passable, fieldOfView, !hasTalent(Talent.BIG_RUSH));
				if (newpath != null && path != null && newpath.size() > 2*path.size()){
					path = null;
				} else {
					path = newpath;
				}
			}

			if (path == null) return false;
			step = path.removeFirst();

		}

		if (step != -1) {

			float delay = 1 / speed();

			if (Dungeon.level.pit[step] && !Dungeon.level.solid[step]
					&& (!flying || buff(Levitation.class) != null && buff(Levitation.class).detachesWithinDelay(delay))){
				if (!Chasm.jumpConfirmed){
					Chasm.heroJump(this);
					interrupt();
				} else {
					flying = false;
					remove(buff(Levitation.class)); //directly remove to prevent cell pressing
					Chasm.heroFall(target);
				}
				canSelfTrample = false;
				return false;
			}

			if (subClass.is(HeroSubClass.FREERUNNER)){
				Buff.affect(this, Momentum.class).gainStack();
			}
			if (hasTalent(Talent.BIG_RUSH)){
				for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
					if (mob.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[mob.pos]
							&& mob.pos == step) {
						Buff.affect(this, Talent.BigRushTracker.class, 0f);
						enemy = mob;
						if (enemy.isAlive() && canAttack( enemy ) && !isCharmedBy( enemy )) {
							CellEmitter.center(pos).burst(Speck.factory(Speck.DUST), 10);
							Camera.main.shake(2, 0.5f);
							sprite.attack( enemy.pos );
//								spend(attackDelay());
							return false;
						}
					}
				}
			}

			sprite.move(pos, step);
			move(step);

			spend( delay );
			justMoved = true;


			search(false);

			return true;

		} else {

			return false;
			
		}

	}
	
	public boolean handle( int cell ) {
		
		if (cell == -1) {
			return false;
		}

		if (fieldOfView == null || fieldOfView.length != Dungeon.level.length()){
			fieldOfView = new boolean[Dungeon.level.length()];
			Dungeon.level.updateFieldOfView( this, fieldOfView );
		}
		
		Char ch = Actor.findChar( cell );
		Heap heap = Dungeon.level.heaps.get( cell );

		if (Dungeon.level.map[cell] == Terrain.ALCHEMY && cell != pos) {
			
			curAction = new HeroAction.Alchemy( cell );
			
		} else if (fieldOfView[cell] && ch instanceof Mob) {

			if (((Mob) ch).heroShouldInteract()) {
				curAction = new HeroAction.Interact( ch );
			} else {
				curAction = new HeroAction.Attack( ch );
			}

		//TODO perhaps only trigger this if hero is already adjacent? reducing mistaps
		} else if (Dungeon.level instanceof MiningLevel &&
					belongings.getItem(Pickaxe.class) != null &&
				(Dungeon.level.map[cell] == Terrain.WALL
						|| Dungeon.level.map[cell] == Terrain.WALL_DECO
						|| Dungeon.level.map[cell] == Terrain.MINE_CRYSTAL
						|| Dungeon.level.map[cell] == Terrain.MINE_BOULDER)){

			curAction = new HeroAction.Mine( cell );

		} else if (heap != null
				//moving to an item doesn't auto-pickup when enemies are near...
				&& (visibleEnemies.size() == 0 || cell == pos ||
				//...but only for standard heaps. Chests and similar open as normal.
				(heap.type != Type.HEAP && heap.type != Type.FOR_SALE))) {

			switch (heap.type) {
			case HEAP:
				curAction = new HeroAction.PickUp( cell );
				break;
			case FOR_SALE:
				curAction = heap.size() == 1 && heap.peek().value() > 0 ?
					new HeroAction.Buy( cell ) :
					new HeroAction.PickUp( cell );
				break;
			default:
				curAction = new HeroAction.OpenChest( cell );
			}
			
		} else if (Dungeon.level.map[cell] == Terrain.LOCKED_DOOR || Dungeon.level.map[cell] == Terrain.CRYSTAL_DOOR || Dungeon.level.map[cell] == Terrain.LOCKED_EXIT) {
			
			curAction = new HeroAction.Unlock( cell );
			
		} else if (Dungeon.level.getTransition(cell) != null
				//moving to a transition doesn't automatically trigger it when enemies are near
				&& (visibleEnemies.size() == 0 || cell == pos)
				&& !Dungeon.level.locked
				&& !Dungeon.level.plants.containsKey(cell)
				&& (Dungeon.depth != 26 || Dungeon.level.getTransition(cell).type == LevelTransition.Type.REGULAR_ENTRANCE) ) {

			curAction = new HeroAction.LvlTransition( cell );

		}  else {
			
			if (!Dungeon.level.visited[cell] && !Dungeon.level.mapped[cell]
					&& Dungeon.level.traps.get(cell) != null && Dungeon.level.traps.get(cell).visible) {
				walkingToVisibleTrapInFog = true;
			} else {
				walkingToVisibleTrapInFog = false;
			}
			
			curAction = new HeroAction.Move( cell );
			lastAction = null;
			
		}

		return true;
	}
	
	public void earnExp( int exp, Class source ) {

		//xp granted by ascension challenge is only for on-exp gain effects
		if (source != AscensionChallenge.class) {
			this.exp += exp;
		}
		float percent = exp/(float)maxExp();

		EtherealChains.chainsRecharge chains = buff(EtherealChains.chainsRecharge.class);
		if (chains != null) chains.gainExp(percent);

		HornOfPlenty.hornRecharge horn = buff(HornOfPlenty.hornRecharge.class);
		if (horn != null) horn.gainCharge(percent);
		
		AlchemistsToolkit.kitEnergy kit = buff(AlchemistsToolkit.kitEnergy.class);
		if (kit != null) kit.gainCharge(percent);

		MasterThievesArmband.Thievery armband = buff(MasterThievesArmband.Thievery.class);
		if (armband != null) armband.gainCharge(percent);

		Berserk berserk = buff(Berserk.class);
		if (berserk != null) berserk.recover(percent);
		
		if (source != PotionOfExperience.class) {
			for (Item i : belongings) {
				i.onHeroGainExp(percent, this);
				i.onHeroGainExp(exp, this);
			}
			if (buff(Talent.RejuvenatingStepsFurrow.class) != null){
				buff(Talent.RejuvenatingStepsFurrow.class).countDown(percent*200f);
				if (buff(Talent.RejuvenatingStepsFurrow.class).count() <= 0){
					buff(Talent.RejuvenatingStepsFurrow.class).detach();
				}
			}
			if (buff(ElementalStrike.ElementalStrikeFurrowCounter.class) != null){
				buff(ElementalStrike.ElementalStrikeFurrowCounter.class).countDown(percent*20f);
				if (buff(ElementalStrike.ElementalStrikeFurrowCounter.class).count() <= 0){
					buff(ElementalStrike.ElementalStrikeFurrowCounter.class).detach();
				}
			}
		}
		
		boolean levelUp = false;
		while (this.exp >= maxExp()) {
			this.exp -= maxExp();
			if (lvl < MAX_LEVEL) {
				lvl++;
				levelUp = true;
				
				if (buff(ElixirOfMight.HTBoost.class) != null){
					buff(ElixirOfMight.HTBoost.class).onLevelUp();
				}
				
				updateHT( true );
				attackSkill++;
				defenseSkill++;

			} else {
				Buff.prolong(this, Bless.class, Bless.DURATION);
				this.exp = 0;

				GLog.newLine();
				GLog.p( Messages.get(this, "level_cap"));
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
			}
			
		}
		
		if (levelUp) {
			
			if (sprite != null) {
				GLog.newLine();
				GLog.p( Messages.get(this, "new_level") );
				sprite.showStatus( CharSprite.POSITIVE, Messages.get(Hero.class, "level_up") );
				Sample.INSTANCE.play( Assets.Sounds.LEVELUP );
				if (lvl < Talent.tierLevelThresholds[Talent.MAX_TALENT_TIERS+1] && !Dungeon.isChallenged(Challenges.NO_TALENTS)){
					int points = 1;
					if(lvl >= 21 && lvl < 25) points += 1;
					if(lvl >= Talent.tierLevelThresholds[4] && armorAbility != null && armorAbility.talents().length == 0) points--;
					if(points > 0) {
						GLog.newLine();
						String new_talent = "new_talent";
						if(points > 1) new_talent += "s"; // double
						GLog.p( Messages.get(this, new_talent) );
						StatusPane.talentBlink = 10f;
						WndHero.lastIdx = 1;
					}
				}
			}

			Item.updateQuickslot();
			
			Badges.validateLevelReached();
		}
	}
	
	public int maxExp() {
		return maxExp( lvl );
	}
	
	public static int maxExp( int lvl ){
		return (int) ((5 + lvl * 5)*Math.pow(1.25, Math.max(0, lvl - 30)));
	}
	
	public boolean isStarving() {
		return Buff.affect(this, Hunger.class).isStarving();
	}
	
	@Override
	public boolean add( Buff buff ) {

		if (buff(TimekeepersHourglass.Stasis.class) != null) {
			return false;
		}

		boolean added = super.add( buff );

		if (sprite != null && added) {
			String msg = buff.heroMessage();
			if (msg != null){
				GLog.w(msg);
			}

			if (buff instanceof Paralysis || buff instanceof Vertigo) {
				interrupt();
			}

		}
		
		BuffIndicator.refreshHero();

		return added;
	}
	
	@Override
	public boolean remove( Buff buff ) {
		if (super.remove( buff )) {
			BuffIndicator.refreshHero();
			return true;
		}
		return false;
	}
	
	@Override
	public float stealth() {
		float stealth = super.stealth();
		
		if (belongings.armor() != null){
			stealth = belongings.armor().stealthFactor(this, stealth);
		}
		if (RobotBuff.isVehicle()){
			stealth += 3;
		}

		return stealth;
	}
	
	@Override
	public void die( Object cause ) {
		
		curAction = null;

		Ankh ankh = null;

		//look for ankhs in player inventory, prioritize ones which are blessed.
		for (Ankh i : belongings.getAllItems(Ankh.class)){
			if (ankh == null || i.isBlessed()) {
				ankh = i;
			}
		}

		if (ankh != null) {
			interrupt();

			if (ankh.isBlessed()) {
				this.HP = HT / 4;

				PotionOfHealing.cure(this);
				Buff.prolong(this, AnkhInvulnerability.class, AnkhInvulnerability.DURATION);

				SpellSprite.show(this, SpellSprite.ANKH);
				GameScene.flash(0x80FFFF40);
				Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
				GLog.w(Messages.get(this, "revive"));
				Statistics.ankhsUsed++;

				ankh.detach(belongings.backpack);

				for (Char ch : Actor.chars()) {
					if (ch instanceof DriedRose.GhostHero) {
						((DriedRose.GhostHero) ch).sayAnhk();
						return;
					}
				}
			} else {

				//this is hacky, basically we want to declare that a wndResurrect exists before
				//it actually gets created. This is important so that the game knows to not
				//delete the run or submit it to rankings, because a WndResurrect is about to exist
				//this is needed because the actual creation of the window is delayed here
				WndResurrect.instance = new Object();
				Ankh finalAnkh = ankh;
				Game.runOnRenderThread(new Callback() {
					@Override
					public void call() {
						GameScene.show( new WndResurrect(finalAnkh) );
					}
				});

				if (cause instanceof Hero.Doom) {
					((Hero.Doom)cause).onDeath();
				}

				SacrificialFire.Marked sacMark = buff(SacrificialFire.Marked.class);
				if (sacMark != null){
					sacMark.detach();
				}

			}
			return;
		}

		Actor.fixTime();
		super.die( cause );
		reallyDie( cause );
	}
	
	public static void reallyDie( Object cause ) {
		
		int length = Dungeon.level.length();
		int[] map = Dungeon.level.map;
		boolean[] visited = Dungeon.level.visited;
		boolean[] discoverable = Dungeon.level.discoverable;
		
		for (int i=0; i < length; i++) {
			
			int terr = map[i];
			
			if (discoverable[i]) {
				
				visited[i] = true;
				if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
					Dungeon.level.discover( i );
				}
			}
		}
		
		Bones.leave();
		
		Dungeon.observe();
		GameScene.updateFog();
				
		Dungeon.hero.belongings.identify();

		int pos = Dungeon.hero.pos;

		ArrayList<Integer> passable = new ArrayList<>();
		for (Integer ofs : PathFinder.NEIGHBOURS8) {
			int cell = pos + ofs;
			if ((Dungeon.level.passable[cell] || Dungeon.level.avoid[cell]) && Dungeon.level.heaps.get( cell ) == null) {
				passable.add( cell );
			}
		}
		Collections.shuffle( passable );

		ArrayList<Item> items = new ArrayList<>(Dungeon.hero.belongings.backpack.items);
		for (Integer cell : passable) {
			if (items.isEmpty()) {
				break;
			}

			Item item = Random.element( items );
			Dungeon.level.drop( item, cell ).sprite.drop( pos );
			items.remove( item );
		}

		for (Char c : Actor.chars()){
			if (c instanceof DeathCommentator){
				((DeathCommentator) c).sayHeroKilled();
			}
		}

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.gameOver();
				Sample.INSTANCE.play( Assets.Sounds.DEATH );
			}
		});

		if (cause instanceof Hero.Doom) {
			((Hero.Doom)cause).onDeath();
		}

		Dungeon.deleteGame( GamesInProgress.curSlot, true );
	}

	//effectively cache this buff to prevent having to call buff(...) a bunch.
	//This is relevant because we call isAlive during drawing, which has both performance
	//and thread coordination implications if that method calls buff(...) frequently
	private Berserk berserk;
	private NoDeath noDeath;

	@Override
	public boolean isAlive() {
		if (HP <= 0){
			if (noDeath == null) noDeath = buff(NoDeath.class);
			if (berserk == null) berserk = buff(Berserk.class);
			return (berserk != null && berserk.berserking()) || (noDeath != null && noDeath.visualcooldown() > 0);
		} else {
			berserk = null;
			noDeath = null;
			return super.isAlive();
		}
	}

	@Override
	public void move(int step, boolean travelling) {
		boolean wasHighGrass = Dungeon.level.map[step] == Terrain.HIGH_GRASS;

		super.move( step, travelling);
		
		if (!flying && travelling) {
			if (Dungeon.level.water[pos]) {
				Sample.INSTANCE.play( Assets.Sounds.WATER, 1, Random.Float( 0.8f, 1.25f ) );
			} else if (Dungeon.level.map[pos] == Terrain.EMPTY_SP) {
				Sample.INSTANCE.play( Assets.Sounds.STURDY, 1, Random.Float( 0.96f, 1.05f ) );
			} else if (Dungeon.level.map[pos] == Terrain.GRASS
					|| Dungeon.level.map[pos] == Terrain.EMBERS
					|| Dungeon.level.map[pos] == Terrain.FURROWED_GRASS){
				if (step == pos && wasHighGrass) {
					Sample.INSTANCE.play(Assets.Sounds.TRAMPLE, 1, Random.Float( 0.96f, 1.05f ) );
				} else {
					Sample.INSTANCE.play( Assets.Sounds.GRASS, 1, Random.Float( 0.96f, 1.05f ) );
				}
			} else {
				Sample.INSTANCE.play( Assets.Sounds.STEP, 1, Random.Float( 0.96f, 1.05f ) );
			}
		}
	}
	
	@Override
	public void onAttackComplete() {

		if (enemy == null){
			curAction = null;
			super.onAttackComplete();
			return;
		}

		AttackIndicator.target(enemy);
		boolean wasEnemy = enemy.alignment == Alignment.ENEMY
				|| (enemy instanceof Mimic && enemy.alignment == Alignment.NEUTRAL);

		boolean hit = attack( enemy );

		Invisibility.dispel();
		spend( attackDelay() );

		if (wasEnemy && subClass.is(HeroSubClass.GLADIATOR)){
			Combo combo = Buff.affect( this, Combo.class );
			if(hit) combo.hit(enemy);
			else 	combo.miss();
		}

		if (hit && heroClass.is(HeroClass.DUELIST) && wasEnemy){
			Buff.affect( this, Sai.ComboStrikeTracker.class).addHit();
		}

		RingOfForce.BrawlersStance brawlStance = buff(RingOfForce.BrawlersStance.class);
		if (brawlStance != null && brawlStance.hitsLeft() > 0){
			MeleeWeapon.Charger charger = Buff.affect(this, MeleeWeapon.Charger.class);
			charger.gainCharge(-RingOfForce.BrawlersStance.HIT_CHARGE_USE);
			BuffIndicator.refreshHero();
		}

		curAction = null;

		super.onAttackComplete();
	}
	
	@Override
	public void onMotionComplete() {
		GameScene.checkKeyHold();
	}
	
	@Override
	public void onOperateComplete() {
		
		if (curAction instanceof HeroAction.Unlock) {

			int doorCell = ((HeroAction.Unlock)curAction).dst;
			int door = Dungeon.level.map[doorCell];
			
			if (Dungeon.level.distance(pos, doorCell) <= 1) {
				boolean hasKey = true;
				if (door == Terrain.LOCKED_DOOR) {
					hasKey = Notes.remove(new IronKey(Dungeon.depth));
					if (hasKey) Level.set(doorCell, Terrain.DOOR);
				} else if (door == Terrain.CRYSTAL_DOOR) {
					hasKey = Notes.remove(new CrystalKey(Dungeon.depth));
					if (hasKey) {
						Level.set(doorCell, Terrain.EMPTY);
						Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
						CellEmitter.get( doorCell ).start( Speck.factory( Speck.DISCOVER ), 0.025f, 20 );
					}
				} else {
					hasKey = Notes.remove(new SkeletonKey(Dungeon.depth));
					if (hasKey) Level.set(doorCell, Terrain.UNLOCKED_EXIT);
				}
				
				if (hasKey) {
					GameScene.updateKeyDisplay();
					GameScene.updateMap(doorCell);
					spend(Key.TIME_TO_UNLOCK);
				}
			}
			
		} else if (curAction instanceof HeroAction.OpenChest) {
			
			Heap heap = Dungeon.level.heaps.get( ((HeroAction.OpenChest)curAction).dst );
			
			if (Dungeon.level.distance(pos, heap.pos) <= 1){
				boolean hasKey = true;
				if (heap.type == Type.SKELETON || heap.type == Type.REMAINS) {
					Sample.INSTANCE.play( Assets.Sounds.BONES );
				} else if (heap.type == Type.LOCKED_CHEST){
					hasKey = Notes.remove(new GoldenKey(Dungeon.depth));
				} else if (heap.type == Type.CRYSTAL_CHEST){
					hasKey = Notes.remove(new CrystalKey(Dungeon.depth));
				}
				
				if (hasKey) {
					GameScene.updateKeyDisplay();
					heap.open(this);
					spend(Key.TIME_TO_UNLOCK);
				}
			}
			
		}
		curAction = null;

		if (!ready) {
			super.onOperateComplete();
		}
	}

	@Override
	public boolean isImmune(Class effect) {
		if (effect == Electricity.class &&
			hasTalent(Talent.FARADAY_CAGE)){
			return true;
		}
		if (effect == Burning.class
				&& belongings.armor() != null
				&& belongings.armor().hasGlyph(Brimstone.class, this)){
			return true;
		}
		return super.isImmune(effect);
	}

	@Override
	public boolean isInvulnerable(Class effect) {
		return super.isInvulnerable(effect) || buff(AnkhInvulnerability.class) != null;
	}

	public boolean search( boolean intentional ) {
		
		if (!isAlive()) return false;
		
		boolean smthFound = false;

		int points = pointsInTalent(Talent.KINGS_VISION);
		boolean circular = points == 1;
		int distance = heroClass.is(HeroClass.ROGUE) ? 2 : 1;
		if (points > 0) distance++;
		distance += pointsInTalent(Talent.WIDE_SEARCH);
		
		boolean foresight = buff(Foresight.class) != null;
		boolean foresightScan = foresight && !Dungeon.level.mapped[pos];

		if (foresightScan){
			Dungeon.level.mapped[pos] = true;
		}

		if (foresight) {
			distance = Foresight.DISTANCE;
			circular = true;
		}

		Point c = Dungeon.level.cellToPoint(pos);

		TalismanOfForesight.Foresight talisman = buff( TalismanOfForesight.Foresight.class );
		boolean cursed = talisman != null && talisman.isCursed();

		int[] rounding = ShadowCaster.rounding[distance];

		int left, right;
		int curr;
		for (int y = Math.max(0, c.y - distance); y <= Math.min(Dungeon.level.height()-1, c.y + distance); y++) {
			if (!circular){
				left = c.x - distance;
			} else if (rounding[Math.abs(c.y - y)] < Math.abs(c.y - y)) {
				left = c.x - rounding[Math.abs(c.y - y)];
			} else {
				left = distance;
				while (rounding[left] < rounding[Math.abs(c.y - y)]){
					left--;
				}
				left = c.x - left;
			}
			right = Math.min(Dungeon.level.width()-1, c.x + c.x - left);
			left = Math.max(0, left);
			for (curr = left + y * Dungeon.level.width(); curr <= right + y * Dungeon.level.width(); curr++){

				if ((foresight || fieldOfView[curr]) && curr != pos) {

					if ((foresight && (!Dungeon.level.mapped[curr] || foresightScan))){
						GameScene.effectOverFog(new CheckedCell(curr, foresightScan ? pos : curr));
					} else if (intentional) {
						GameScene.effectOverFog(new CheckedCell(curr, pos));
					}

					if (foresight){
						Dungeon.level.mapped[curr] = true;
					}

					if (Dungeon.level.secret[curr]){

						Trap trap = Dungeon.level.traps.get( curr );
						float chance;

						//searches aided by foresight always succeed, even if trap isn't searchable
						if (foresight){
							chance = 1f;

						//otherwise if the trap isn't searchable, searching always fails
						} else if (trap != null && !trap.canBeSearched){
							chance = 0f;

						//intentional searches always succeed against regular traps and doors
						} else if (intentional){
							chance = 1f;
						
						//unintentional searches always fail with a cursed talisman
						} else if (cursed) {
							chance = 0f;
							
						//unintentional trap detection scales from 40% at floor 0 to 30% at floor 25
						} else if (Dungeon.level.map[curr] == Terrain.SECRET_TRAP) {
							chance = 0.4f - (Dungeon.scalingDepth() / 250f);
							
						//unintentional door detection scales from 20% at floor 0 to 0% at floor 20
						} else {
							chance = 0.2f - (Dungeon.scalingDepth() / 100f);
						}

						if (hasTalent(Talent.ROGUES_FORESIGHT)) chance *= 1.5f;

						//don't want to let the player search though hidden doors in tutorial
						if (SPDSettings.intro()){
							chance = 0;
						}

						if (Random.Float() < chance) {
						
							int oldValue = Dungeon.level.map[curr];
							
							GameScene.discoverTile( curr, oldValue );
							
							Dungeon.level.discover( curr );
							
							ScrollOfMagicMapping.discover( curr );
							
							if (fieldOfView[curr]) smthFound = true;
	
							if (talisman != null){
								if (oldValue == Terrain.SECRET_TRAP){
									talisman.charge(2);
								} else if (oldValue == Terrain.SECRET_DOOR){
									talisman.charge(10);
								}
							}

							if (pointsInTalent(Talent.ATTUNEXPLORATION) > 2){
								Buff.affect(this, MonkEnergy.class).gainEnergy(this);
							}
						}
					}
				}
			}
		}

		if (intentional) {
			sprite.showStatus( CharSprite.DEFAULT, Messages.get(this, "search") );
			sprite.operate( pos );
			if (!Dungeon.level.locked) {
				if (cursed) {
					GLog.n(Messages.get(this, "search_distracted"));
					Buff.affect(this, Hunger.class).affectHunger(TIME_TO_SEARCH - (2 * HUNGER_FOR_SEARCH));
				} else {
					Buff.affect(this, Hunger.class).affectHunger(TIME_TO_SEARCH - HUNGER_FOR_SEARCH);
				}
			}
			spendAndNext(TIME_TO_SEARCH);
			
		}
		
		if (smthFound) {
			GLog.w( Messages.get(this, "noticed_smth") );
			Sample.INSTANCE.play( Assets.Sounds.SECRET );
			interrupt();
		}

		if (foresight){
			GameScene.updateFog(pos, Foresight.DISTANCE+1);
		}
if (talisman != null){
			talisman.checkAwareness();
		}

		return smthFound;
	}
	
	public void resurrect() {
		HP = HT;
		live();

		MagicalHolster holster = belongings.getItem(MagicalHolster.class);

		Buff.affect(this, LostInventory.class);
		Buff.affect(this, Invisibility.class, 3f);
		//lost inventory is dropped in interlevelscene

		//activate items that persist after lost inventory
		//FIXME this is very messy, maybe it would be better to just have one buff that
		// handled all items that recharge over time?
		for (Item i : belongings){
			if (i instanceof EquipableItem && i.isEquipped(this)){
				((EquipableItem) i).activate(this);
			} else if (i instanceof CloakOfShadows && i.keptThroughLostInventory() && hasTalent(Talent.LIGHT_CLOAK, Talent.RK_FREERUNNER)){
				((CloakOfShadows) i).activate(this);
			} else if (i instanceof Wand && i.keptThroughLostInventory()){
				if (holster != null && holster.contains(i)){
					((Wand) i).charge(this, MagicalHolster.HOLSTER_SCALE_FACTOR);
				} else {
					((Wand) i).charge(this);
				}
			} else if (i instanceof MagesStaff && i.keptThroughLostInventory()){
				((MagesStaff) i).applyWandChargeBuff(this);
			}
		}

		updateHT(false);
	}

	@Override
	public void next() {
		if (isAlive())
			super.next();
	}

	public static interface Doom {
		public void onDeath();
	}

	/* says something when the hero dies.*/
	public interface DeathCommentator {
		void sayHeroKilled();
	}
}
