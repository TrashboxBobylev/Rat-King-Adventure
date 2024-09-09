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

import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.GamesInProgress;
import com.zrp200.rkpd2.ShatteredPixelDungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.ArtifactRecharge;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Bleeding;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.CounterBuff;
import com.zrp200.rkpd2.actors.buffs.EnhancedRings;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Haste;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.buffs.Hunger;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.LostInventory;
import com.zrp200.rkpd2.actors.buffs.MonkEnergy;
import com.zrp200.rkpd2.actors.buffs.PhysicalEmpower;
import com.zrp200.rkpd2.actors.buffs.Preparation;
import com.zrp200.rkpd2.actors.buffs.Recharging;
import com.zrp200.rkpd2.actors.buffs.RevealedArea;
import com.zrp200.rkpd2.actors.buffs.RobotBuff;
import com.zrp200.rkpd2.actors.buffs.Roots;
import com.zrp200.rkpd2.actors.buffs.ScrollEmpower;
import com.zrp200.rkpd2.actors.buffs.Stamina;
import com.zrp200.rkpd2.actors.buffs.WandEmpower;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.abilities.Ratmogrify;
import com.zrp200.rkpd2.actors.mobs.DM100;
import com.zrp200.rkpd2.actors.mobs.DarkestElf;
import com.zrp200.rkpd2.actors.mobs.Dragon;
import com.zrp200.rkpd2.actors.mobs.Elemental;
import com.zrp200.rkpd2.actors.mobs.Eye;
import com.zrp200.rkpd2.actors.mobs.FinalFroggit;
import com.zrp200.rkpd2.actors.mobs.Golem;
import com.zrp200.rkpd2.actors.mobs.LostSpirit;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.actors.mobs.Necromancer;
import com.zrp200.rkpd2.actors.mobs.Phantom;
import com.zrp200.rkpd2.actors.mobs.Shaman;
import com.zrp200.rkpd2.actors.mobs.SpectreRat;
import com.zrp200.rkpd2.actors.mobs.Succubus;
import com.zrp200.rkpd2.actors.mobs.Warlock;
import com.zrp200.rkpd2.actors.mobs.YogFist;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.effects.Wound;
import com.zrp200.rkpd2.effects.particles.ExoParticle;
import com.zrp200.rkpd2.items.BrokenSeal;
import com.zrp200.rkpd2.items.EquipableItem;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.armor.ClothArmor;
import com.zrp200.rkpd2.items.artifacts.Artifact;
import com.zrp200.rkpd2.items.artifacts.CloakOfShadows;
import com.zrp200.rkpd2.items.artifacts.HornOfPlenty;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.items.rings.Ring;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRecharging;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.weapon.SpiritBow;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.items.weapon.melee.Gloves;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.levels.Level;
import com.zrp200.rkpd2.levels.Terrain;
import com.zrp200.rkpd2.levels.features.HighGrass;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.ActionIndicator;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.DungeonSeed;
import com.zrp200.rkpd2.utils.GLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static com.zrp200.rkpd2.Dungeon.hero;
import static java.lang.Math.max;

public enum Talent {

	//Warrior T1
	HEARTY_MEAL(0), VETERANS_INTUITION(1), TEST_SUBJECT(2), IRON_WILL(3), WEAPON_MASTERY(imageAt(0, 8)),
	//Warrior T2
	IRON_STOMACH(4), LIQUID_WILLPOWER(5), RUNIC_TRANSFERENCE(6), LETHAL_MOMENTUM(7), IMPROVISED_PROJECTILES(8), BIG_RUSH(imageAt(1, 8)),
	//Warrior T3
	HOLD_FAST(9, 3), STRONGMAN(10, 3), BEAR_PAW(imageAt(3, 8), 3),
	//Berserker T3
	// yes, yes, I kept both talents and then swapped their icons... deal with it. Why does the one that protects you from death have a + next to it?
	ENDLESS_RAGE(11, 3), DEATHLESS_FURY(29, 3), BERSERKING_STAMINA(12, 3), ENRAGED_CATALYST(13, 3), ONE_MAN_ARMY(30,3), BRAVERY(imageAt(7, 8), 3),
	//Gladiator T3
	CLEAVE(14, 3), LETHAL_DEFENSE(15, 3), ENHANCED_COMBO(16, 3), SKILL(31,3), EFFICIENT_TRAINING(imageAt(6, 8), 3),
	//Brawler T3
	POWER_STABILIZER(imageAt(8, 8), 3), ARCANITY_ENSUES(imageAt(9, 8), 3), IN_MY_MEMORIES(imageAt(10, 8), 3), HOLERIC_BURST(imageAt(11, 8), 3), PRIDE_OF_STEEL(imageAt(12, 8), 3),
	//Heroic Leap T4
	BODY_SLAM(17, 4), IMPACT_WAVE(18, 4), DOUBLE_JUMP(19, 4), ALICE_GAMBIT(imageAt(13, 8), 4),
	//Shockwave T4
	EXPANDING_WAVE(20, 4), STRIKING_WAVE(21, 4), SHOCK_FORCE(22, 4), COCKATRIOCIOUS(imageAt(14, 8), 4),
	//Endure T4
	SUSTAINED_RETRIBUTION(23, 4), SHRUG_IT_OFF(24, 4), EVEN_THE_ODDS(25, 4), DEMONSHADER(imageAt(15, 8), 4),

	//Mage T1
	ENERGIZING_MEAL_I(36), SCHOLARS_INTUITION(33), TESTED_HYPOTHESIS(34), BACKUP_BARRIER(35), ARCANE_BOOST(imageAt(0, 9)),
	//Mage T2
	ENERGIZING_MEAL_II(36), INSCRIBED_POWER(37), WAND_PRESERVATION(38), ARCANE_VISION(39), SHIELD_BATTERY(40), PYROMANIAC(imageAt(1, 9)),
	//Mage T3
	DESPERATE_POWER(41, 3), ALLY_WARP(42, 3), CRYONIC_SPELL(imageAt(3, 9), 3),
	//Battlemage T3
	EMPOWERED_STRIKE(43, 3), MYSTICAL_CHARGE(44, 3), EXCESS_CHARGE(45, 3), SORCERY(62,3), SPECTRE_ALLIES(imageAt(6, 9), 3),
	//Warlock T3
	SOUL_EATER(46, 3){
		@Override
		public int icon() {
			return (hero != null ? hero.heroClass : GamesInProgress.selectedClass) == HeroClass.HUNTRESS ? imageAt(8, 11) : 46;
		}
	}, SOUL_SIPHON(47, 3){
		@Override
		public int icon() {
			return (hero != null ? hero.heroClass : GamesInProgress.selectedClass) == HeroClass.HUNTRESS ? imageAt(9, 11) : 47;
		}
	}, NECROMANCERS_MINIONS(48, 3){
		@Override
		public int icon() {
			return (hero != null ? hero.heroClass : GamesInProgress.selectedClass) == HeroClass.HUNTRESS ? imageAt(10, 11) : 48;
		}
	}, WARLOCKS_TOUCH (63, 3){
		@Override
		public int icon() {
			return (hero != null ? hero.heroClass : GamesInProgress.selectedClass) == HeroClass.HUNTRESS ? imageAt(11, 11) : 63;
		}
	}, BANISHED (208, 3){
		@Override
		public int icon() {
			return (hero != null ? hero.heroClass : GamesInProgress.selectedClass) == HeroClass.HUNTRESS ? imageAt(12, 11) : imageAt(7, 9);
		}
	},
	//Spirit Caller T3
	ECTOTOUGHNESS(imageAt(8, 9), 3), I_HATE_ALL_ELEMENTS(imageAt(9, 9), 3), STAB_FROM_NOWHERE(imageAt(10, 9), 3), POWER_IN_NUMBERS(imageAt(11, 9), 3), MIND_BREAKER(imageAt(12, 9), 3),
	//Elemental Blast T4
	BLAST_RADIUS(49, 4), ELEMENTAL_POWER(50, 4), REACTIVE_BARRIER(51, 4), EMPOWERED_STRIKE_II(imageAt(13, 9), 4),
	//Wild Magic T4
	WILD_POWER(52, 4), FIRE_EVERYTHING(53, 4), CONSERVED_MAGIC(54, 4), ELDRITCH_BLESSING(imageAt(14, 9), 4),
	//Warp Beacon T4
	TELEFRAG(55, 4), REMOTE_BEACON(56, 4), LONGRANGE_WARP(57, 4), CHRONO_SCREW(imageAt(15, 9), 4),

	//Rogue T1
	CACHED_RATIONS(64), THIEFS_INTUITION(65), SUCKER_PUNCH(66), MENDING_SHADOWS(67), FARADAY_CAGE(imageAt(0, 10)),
	//Rogue T2
	MYSTICAL_MEAL(68), INSCRIBED_STEALTH(69), WIDE_SEARCH(70), SILENT_STEPS(71), ROGUES_FORESIGHT(72), EFFICIENT_SHADOWS(imageAt(1, 10)),
	//Rogue T3
	ENHANCED_RINGS(73, 3), LIGHT_CLOAK(74, 3), TRAPPER_MASTERY(imageAt(3, 10), 3),
	//Assassin T3
	ENHANCED_LETHALITY(75, 3), ASSASSINS_REACH(76, 3), BOUNTY_HUNTER(77, 3), LETHAL_MOMENTUM_2(94,3), DARKENING_STEPS(imageAt(6, 10), 3),
	//Freerunner T3
	EVASIVE_ARMOR(78, 3), PROJECTILE_MOMENTUM(79, 3), SPEEDY_STEALTH(80, 3), FAST_RECOVERY(95,3), OLYMPIC_STATS(imageAt(7, 10), 3),
	//Shadowflare T3
	ENERGON_FUSION(imageAt(8, 10), 3), LASER_PRECISION(imageAt(9, 10), 3), ERADICATING_CHARGE(imageAt(10, 10), 3), VOID_WRATH(imageAt(11, 10), 3), MECHANICAL_POWER(imageAt(12, 10), 3),
	//Smoke Bomb T4
	HASTY_RETREAT(81, 4), BODY_REPLACEMENT(82, 4), SHADOW_STEP(83, 4), FRIGID_TOUCH(imageAt(13, 10), 4),
	//Death Mark T4
	FEAR_THE_REAPER(84, 4), DEATHLY_DURABILITY(85, 4), DOUBLE_MARK(86, 4), CATACLYSMIC_ENERGY(imageAt(14, 10), 4),
	//Shadow Clone T4
	SHADOW_BLADE(87, 4), CLONED_ARMOR(88, 4), PERFECT_COPY(89, 4), DAR_MAGIC(imageAt(15, 10), 4),

	//Huntress T1
	NATURES_BOUNTY(96), SURVIVALISTS_INTUITION(97), FOLLOWUP_STRIKE(98), NATURES_AID(99), NATURE_AID_2(imageAt(0, 11)),
	//Huntress T2
	INVIGORATING_MEAL(100), LIQUID_NATURE(101), REJUVENATING_STEPS(102), HEIGHTENED_SENSES(103), DURABLE_PROJECTILES(104), SCOURGING_THE_UNIVERSE(imageAt(1, 11)),
	//Huntress T3
	POINT_BLANK(105, 3), SEER_SHOT(106, 3), AUTO_RELOAD(imageAt(3, 11), 3),
	//Sniper T3
	FARSIGHT(107, 3), SHARED_ENCHANTMENT(108, 3), SHARED_UPGRADES(109, 3), MULTISHOT(126,3) {{aliases = new String[]{"RANGER"};}}, ARCHERY_MARK(imageAt(6, 11), 3),
	//Warden T3
	DURABLE_TIPS(110, 3), BARKSKIN(111, 3), SHIELDING_DEW(112, 3), NATURES_BETTER_AID(127,3), INDIRECT_BENEFITS(imageAt(7, 11), 3),
	//Spectral Blades T4
	FAN_OF_BLADES(113, 4), PROJECTING_BLADES(114, 4), SPIRIT_BLADES(115, 4), SPECTRAL_SHOT(imageAt(13, 11), 4),
	//Natures Power T4
	GROWING_POWER(116, 4), NATURES_WRATH(117, 4), WILD_MOMENTUM(118, 4), PRIMAL_AWAKENING(imageAt(14, 11), 4),
	//Spirit Hawk T4
	EAGLE_EYE(119, 4), GO_FOR_THE_EYES(120, 4), SWIFT_SPIRIT(121, 4),  BEAK_OF_POWER(imageAt(15, 11), 4),

	//Duelist T1
	STRENGTHENING_MEAL(128), ADVENTURERS_INTUITION(129), PATIENT_STRIKE(130), AGGRESSIVE_BARRIER(131), ADVENTUROUS_SNOOZING(imageAt(0, 12)),
	//Duelist T2
	FOCUSED_MEAL(132), LIQUID_AGILITY(133), WEAPON_RECHARGING(134), LETHAL_HASTE(135), SWIFT_EQUIP(136), SPELLBLADE_FORGERY(imageAt(1, 12)),
	//Duelist T3
	PRECISE_ASSAULT(137, 3), DEADLY_FOLLOWUP(138, 3), GRASSY_OFFENSE(imageAt(3, 12), 3),
	//Champion T3
	SECONDARY_CHARGE(139, 3), TWIN_UPGRADES(140, 3), COMBINED_LETHALITY(141, 3), ELITE_DEXTERITY(158, 3), HELPER_TO_HERO(imageAt(6, 12), 3),
	//Monk T3
	UNENCUMBERED_SPIRIT(142, 3), MONASTIC_VIGOR(143, 3), COMBINED_ENERGY(144, 3), MONK_6TH_ABILITY(159), GRASS_MUNCHING(imageAt(7, 12), 3),
	//Highness T3
	PARTY_FEELING(imageAt(8, 12), 3), SLASH_RUNNER(imageAt(9, 12), 3), PROLONGED_JOY(imageAt(10, 12), 3), WOUND_IGNORANCE(imageAt(11, 12), 3), AGREENALINE_RUSH(imageAt(12, 12), 3),
	//Challenge T4
	CLOSE_THE_GAP(145, 4), INVIGORATING_VICTORY(146, 4), ELIMINATION_MATCH(147, 4), DUNGEONS_CHAMPIONSHIP(imageAt(13, 12), 4),
	//Elemental Strike T4
	ELEMENTAL_REACH(148, 4), STRIKING_FORCE(149, 4), DIRECTED_POWER(150, 4), EMPOWERED_SWIPE(imageAt(14, 12), 4),
	//Duelist A3 T4
	FEIGNED_RETREAT(151, 4), EXPOSE_WEAKNESS(152, 4), COUNTER_ABILITY(153, 4), WARPED_AGGRESSION(imageAt(15, 12), 4),

	//universal T4
	HEROIC_ENERGY(26, 4) {
		// this is why wrath doesn't have any talents...
		private boolean ratmogrify() {
			// FIXME this is really brittle, will be an issue if/when I add OmniAbility
			return Ratmogrify.useRatroicEnergy
					|| GamesInProgress.selectedClass == HeroClass.RAT_KING
					|| hero != null
						&& (hero.heroClass.is(HeroClass.RAT_KING)
							|| hero.armorAbility instanceof Ratmogrify);
		}
		@Override public int icon() {
			if ( ratmogrify() ) return 218;
			switch (hero != null ? hero.heroClass : GamesInProgress.selectedClass){
				case WARRIOR: default: return 26;
				case MAGE: return 58;
				case ROGUE: return 90;
				case HUNTRESS: return 122;
				case DUELIST: return 154;
				// Rat King handled on line 164
			}
		}

		@Override public String title() {
			//TODO translate this
			if (ratmogrify()) {
				return Messages.get(this, name() + ".rat_title");
			}
			return super.title();
		}
	},
	HEROIC_ENDURANCE(imageAt(4, 8), 4), HEROIC_WIZARDRY(imageAt(4, 9), 4), HEROIC_STAMINA(imageAt(4, 10), 4), HEROIC_ARCHERY(imageAt(4, 11), 4), HEROIC_ADAPTABILITY(imageAt(4, 12), 4), HEROIC_RATINESS(imageAt(4, 14), 3),

	//Ratmogrify T4
	RATSISTANCE(215, 4), RATLOMACY(216, 4), RATFORCEMENTS(217, 4), DRRATEDON(imageAt(13, 14), 4),
	ROYAL_PRIVILEGE(224), // food related talents
	ROYAL_INTUITION(225), // intuition-related talents, uses survivalist's icon
	KINGS_WISDOM(226), // on-id + combat talents
	NOBLE_CAUSE(227), // other ones. uses iron will
	ROYAL_MEAL(228), //// all on-eat talents for tier 2
	RESTORATION(229), // all upgrade/potion of healing talents
	POWER_WITHIN(230), // runic (3), wand preservation (3), rogue's foresight (5), rejuvenating steps (3)
	KINGS_VISION(231), // improvised projectiles (4), arcane vision(4), wide search(3), heightened senses(4)
	PURSUIT(232), // durable projectiles (5),silent steps(4),lethal momentum (3),shield battery(5)
	// Rat King T3
	RK_BERSERKER(233,3), RK_GLADIATOR(234,3), RK_BATTLEMAGE(235,3),
	RK_WARLOCK(236,3), RK_ASSASSIN(237,3), RK_FREERUNNER(238,3), RK_SNIPER(239,3), RK_WARDEN(240,3),
	// placeholders for if/when duelist is implemented into rk
	RK_CHAMPION(241,3), RK_MONK(242,3),
    // RK Champion T3
    RK_FIRE(imageAt(5, 14), 3), RK_PROJECT(imageAt(6, 14), 3), RK_ANTIMAGIC(imageAt(7, 14), 3),
    RK_GIANT(imageAt(8, 14), 3), RK_BLESSED(imageAt(9, 14), 3), RK_CURSED(imageAt(10, 14), 3),
    RK_SPLINT(imageAt(11, 14), 3), RK_PALADIN(imageAt(12, 14), 3),
    // Wrath T4
    AURIC_TESLA(imageAt(15, 14), 4), QUANTUM_POSITION(imageAt(16, 14), 4), RAT_AGE(imageAt(17, 14), 4), AVALON_POWER_UP(imageAt(18, 14), 4),
    // Mus Rex Ira T4
    BLOODFLARE_SKIN(imageAt(19, 14), 4), ASTRAL_CHARGE(imageAt(20, 14), 4), SHADOWSPEC_SLICE(imageAt(21, 14), 4), SILVA_RANGE(imageAt(22, 14), 4),

    // Wrath2
	AFTERSHOCK(RK_MONK.icon+10,4), RAT_BLAST(AFTERSHOCK.icon+1,4), SMOKE_AND_MIRRORS(AFTERSHOCK.icon+2,4), SEA_OF_BLADES(AFTERSHOCK.icon+3,4), FUN(imageAt(14, 14), 4),

	// RKA replacements for consumable talents
	WILLPOWER_OF_INJURED(imageAt(2, 8)), ENERGIZING_UPGRADE(imageAt(2, 9)), DUAL_WIELDING(imageAt(2, 10)), IVYLASH(imageAt(2, 11)), SIXTH_SENSE(imageAt(2, 12)),

	// RKA assassin rework talents
	BLOODBATH(imageAt(16, 10), 3), THINKING_WITH_PORTALS(imageAt(17, 10), 3), ADAPT_AND_OVERCOME(imageAt(18, 10), 3),

	// RKA other replacements
	PROTEIN_INFUSION(imageAt(5, 10)), SCOUTS_BARRIER(imageAt(5, 11)), LIKE_A_BULLET(imageAt(16, 11), 3), POINTY_BLACK(imageAt(17, 11), 3),
	INDISCRIMINATE_RAGE(imageAt(16, 8), 3), BATTLE_TENDENCY(imageAt(17, 8), 3), ATTUNEXPLORATION(159, 3){
		@Override
		public String desc(boolean metamorphed) {
			String desc = super.desc(metamorphed) + "\n";
			for (Level.Feeling feeling: Level.Feeling.values()){
				if (feeling != Level.Feeling.NONE){
					desc += "\n" + Messages.get(MonkEnergy.class, "desc_" + feeling.name());
				}
			}
			return desc;
		}
	};

	protected String[] aliases = new String[0];

	public static abstract class Cooldown extends FlavourBuff {
		public static <T extends Cooldown> void affectHero(Class<T> cls) {
			if(cls == Cooldown.class) return;
			T buff = Buff.affect(hero, cls);
			buff.spend( buff.duration() );
		}
		public static <T extends Cooldown> void affectHero(Class<T> cls, float extraDuration) {
			if(cls == Cooldown.class) return;
			T buff = Buff.affect(hero, cls);
			buff.spend( buff.duration() + extraDuration );
		}
		public abstract float duration();
		public float iconFadePercent() { return GameMath.gate(0, visualcooldown() / duration(),1); }
		public String toString() { return Messages.get(this, "name"); }
		public String desc() { return Messages.get(this, "desc", dispTurns(visualcooldown())); }
	}

	// TODO is splitting up t2s arbitrarily really a good idea?
	public static class ImprovisedProjectileCooldown extends Cooldown {
		public float duration() { return hero.hasTalent(IMPROVISED_PROJECTILES) ? 15 : 50; }
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.15f, 0.2f, 0.5f); }
	};
	public static class TrapperMasteryCooldown extends Cooldown {
		public float duration() {
			switch (hero.pointsInTalent(TRAPPER_MASTERY)){
				case 1: default:
					return 170;
				case 2:
					return 150;
				case 3:
					return 130;
			}
		}
		public int icon() { return BuffIndicator.THORNS; }
	};
	public static class BanishedCooldown extends Cooldown {
		public float duration() {
			switch (hero.pointsInTalent(BANISHED)){
				case 1: default:
					return 250;
				case 2:
					return 220;
				case 3:
					return 190;
			}
		}
		public int icon() { return BuffIndicator.CORRUPT; }
	};
	public static class BigRushTracker extends FlavourBuff{};
	public static class LethalMomentumTracker extends FlavourBuff{
		public static void process() { hero.byTalent(process, LETHAL_MOMENTUM, LETHAL_MOMENTUM_2, PURSUIT); }
		private static final TalentCallback process = (talent, points) -> {
			if( Random.Float() < ( (talent == LETHAL_MOMENTUM ? 2 : 1) + points )
					/ (talent == PURSUIT ? 3f : 4f) ) {
				Buff.prolong(hero, LethalMomentumTracker.class, 1f);
			}
		};
		// check if it applies
		public static boolean apply(Char ch) {
			if(ch.buff(LethalMomentumTracker.class) != null) {
				detach(ch, LethalMomentumTracker.class);
				return true;
			}
			return false;
		}
	};

	// this is my idea of buffing lethal momentum: remove all possible inconsistencies with it.
	public static abstract class RKPD2LethalMomentumTracker extends FlavourBuff {
		{ actPriority = VFX_PRIO; }
		private boolean checkShielding = false, wasTurned; // this is a very specific case, but still needed.

		@Override public boolean attachTo(Char target) {
			// does not bundle.
			if (Char.restoring == target
					|| !tryAttach(target)
					|| target.HP == 0 && target.isAlive() && !(checkShielding = target.shielding() > 0)
					|| !super.attachTo(target)) {
				return false;
			}
			wasTurned = target.buff(AllyBuff.class) != null;
			return true;
		}
		protected abstract boolean tryAttach(Char target);

		public static void process(Char enemy) {
			for(Class<RKPD2LethalMomentumTracker> trackerClass : new Class[]{
					WarriorLethalMomentumTracker.class,
					AssassinLethalMomentumTracker.class}) {
				Buff.append(enemy, trackerClass);
			}
		}

		@Override protected void onRemove() {
			if (target != null &&
					// activates if the enemy was brought to 0 HP this turn.
					(target.HP == 0 && (!checkShielding || target.shielding() == 0) ||
							// also activates if the enemy was corrupted.
							(target.buff(AllyBuff.class) == null) == wasTurned
					)
			) {
				add(() -> {
					hero.timeToNow();
					proc();
				});
			}
		}
		protected void proc() {}

		// template class
		abstract static class Chain extends FlavourBuff {
			{ type = buffType.POSITIVE; }
			@Override public int icon() { return BuffIndicator.CORRUPT; }
			@Override public String desc() {
				String desc = super.desc();
				String effect = Messages.get(this, "effect");
				//noinspection StringEquality
				if(effect != Messages.NO_TEXT_FOUND) desc += "\n" + effect;
				return desc;
			}
		}
	}

	public static class WarriorLethalMomentumTracker extends RKPD2LethalMomentumTracker {
		@Override protected boolean tryAttach(Char target) {
			int points = hero.pointsInTalent(LETHAL_MOMENTUM);
			return points > 0 && points >= Random.Int(3);
		}

		@Override protected void proc() { Buff.affect(hero, Chain.class); }

		public static class Chain extends RKPD2LethalMomentumTracker.Chain // 2x accuracy
		{
			@Override public void tintIcon(Image icon) { icon.invert(); }
		}
	}

	public static class AssassinLethalMomentumTracker extends RKPD2LethalMomentumTracker {
		private Preparation prep = hero.buff(Preparation.class);
		@Override protected boolean tryAttach(Char target) {
			if(prep == null || !hero.hasTalent(LETHAL_MOMENTUM_2)) return false;
			// 60 / 80 / 100% chance to proc
			// todo determine if I should split these mechanics further?
			if(Random.Float() > .2*(2+hero.pointsInTalent(LETHAL_MOMENTUM_2))) return false;
			// preserve half of the levels on average.
			int level = Random.NormalIntRange(0, prep.attackLevel()-1);
			if(level > 0) prep.setAttackLevel(level); else prep = null;
			return true;
		}

		@Override
		protected void proc() {
			if(prep != null) {
				prep.attachTo(hero);
				prep.timeToNow();
				ActionIndicator.setAction(prep);
			}
		}
	}
	public static class StrikingWaveTracker extends FlavourBuff{};
	public static class WandPreservationCounter extends CounterBuff{{revivePersists = true;}};
	public static class EmpoweredStrikeTracker extends FlavourBuff{};
	public static class ProtectiveShadowsTracker extends Buff {
		private float incHeal = 1, incShield = 1;

		@Override
		public boolean act() {
			Hero target = (Hero) this.target;
			if (target.invisible > 0) {
				if (target.hasTalent(Talent.MENDING_SHADOWS)
						&& !Buff.affect(target, Hunger.class).isStarving()) {
					// heal every 4/2 turns when not starving. effectively a 1.5x boost to standard protective shadows, plus it doesn't go away.
					incHeal += target.pointsInTalent(Talent.MENDING_SHADOWS) / 4f;
					if (incHeal >= 1 && target.HP < target.HT) {
						incHeal = 0;
						target.HP++;
						target.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
					}
				}
				//barrier every 2/1 turns, to a max of 3/5
				if (target.hasTalent(Talent.MENDING_SHADOWS, Talent.NOBLE_CAUSE)) {
					Barrier barrier = Buff.affect(target, Barrier.class);
					int points = target.pointsInTalent(Talent.MENDING_SHADOWS, Talent.NOBLE_CAUSE);
					if (barrier.shielding() < 1 + 2 * points) {
						incShield += 0.5f * points;
					}
					if (incShield >= 1) {
						incShield = 0;
						barrier.incShield(1);
					} else {
						barrier.incShield(0); //resets barrier decay
					}
				}
			} else {
				detach();
			}
			spend( TICK );
			return true;
		}

		private static final String
				BARRIER_INC = "barrier_inc",
				HEAL_INC = "incHeal";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put( BARRIER_INC, incShield );
			bundle.put( HEAL_INC, incHeal );
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			incShield = bundle.getFloat( BARRIER_INC );
			incHeal = bundle.getFloat( HEAL_INC );
		}
	}
	public static class BountyHunterTracker extends FlavourBuff{};
	public static class DualWieldingWandTracker extends FlavourBuff{};
	public static class DualWieldingMissileTracker extends FlavourBuff{};
	public static class HelperToHeroReviveCooldown extends Cooldown {
		public float duration() { return 125; }
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0xBC7FFF); }
	};
	public static class RejuvenatingStepsCooldown extends Cooldown{
		{ revivePersists = true; }
		@Override public float duration() {
			// if both are present the higher one is used. They don't stack in this implementation.
			int points = hero.shiftedPoints(REJUVENATING_STEPS, POWER_WITHIN);
			if(hero.hasTalent(NATURES_BETTER_AID)) points = max(points, 1);
			return 10*(float)Math.pow(2,1-points);
		}
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.35f, 0.15f); }
	};
	public static class EnergizingUpgradeCooldown extends Cooldown {
		public float duration() { return 65 - Dungeon.hero.pointsInTalent(ENERGIZING_UPGRADE)*15; }
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0xf1f028); }
	};
	public static class RejuvenatingStepsFurrow extends CounterBuff{
		{revivePersists = true;}
		/** Track a successful proc of rejuvenating steps.
		 *	Moved logic from Level.java to here so I don't forget what this does.
		 **/
		public static void record() {
			int points = hero.pointsInTalent(false, REJUVENATING_STEPS, POWER_WITHIN);
			if(hero.hasTalent(NATURES_BETTER_AID)) points = max(points, 1);
			count(hero, Talent.RejuvenatingStepsFurrow.class, 3 - points);
		}
	};
	public static class SeerShotCooldown extends Cooldown{
		@Override public float duration() {
			return hero.hasTalent(SEER_SHOT)
					? 15 * hero.pointsInTalent(SEER_SHOT)
					: 20;
		}
		public int icon() { return target.buff(RevealedArea.class) != null ? BuffIndicator.NONE : BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.7f, 0.4f, 0.7f); }
	};
	public static class AutoReloadBuff extends FlavourBuff{};
	public static class SpiritBladesTracker extends FlavourBuff{
		// todo should I have enchant have increased proc chances for Wrath?
		public float getModifier() {
			return hero.pointsInTalent(SPIRIT_BLADES, SEA_OF_BLADES) < 4 ? 1f : 1.1f;
		}
		public void setModifier(float modifier) {/* ignored by default */}

		public static float getProcModifier() {
			SpiritBladesTracker tracker = hero.buff(SpiritBladesTracker.class, false);
			return tracker != null ? tracker.getModifier() : 1f;
		}
	};
	public static class PatientStrikeTracker extends Buff {
		public int pos;
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0f, 1f); }
		@Override
		public boolean act() {
			if (pos != target.pos) {
				detach();
			} else {
				spend(TICK);
			}
			return true;
		}
		private static final String POS = "pos";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(POS, pos);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			pos = bundle.getInt(POS);
		}
	};
	public static class AggressiveBarrierCooldown extends FlavourBuff{
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.35f, 0f, 0.7f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 50); }
	};
	public static class RestoredAgilityTracker extends FlavourBuff{};
	public static class LethalHasteCooldown extends Cooldown {
		public static void applyLethalHaste(Hero hero, boolean viaAbility) {
			if (!hero.hasTalent(LETHAL_HASTE)) return;
			if (viaAbility) {
				// using it as intended removes the cooldown.
				// You'll always be able to use it at least once in a chain. Very balanced I know.
				detach(hero, LethalHasteCooldown.class);
			} else {
				if (hero.buff(LethalHasteCooldown.class) != null) return;
				affectHero(LethalHasteCooldown.class);
			}
			float duration = 1.67f + hero.pointsInTalent(Talent.LETHAL_HASTE);
			Buff.prolong(hero, Haste.class, duration);
			Buff.prolong(hero, Adrenaline.class, duration); // :D

		}
		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.35f, 0f, 0.7f); }
		@Override
		public float duration() {
			return hero.heroClass == HeroClass.DUELIST ? 100 : 50;
		}
	};
	public static class SwiftEquipCooldown extends FlavourBuff{
		public boolean secondUse;
		public boolean hasSecondUse(){
			return secondUse && cooldown() > 14f;
		}

		public int icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) {
			if (hasSecondUse()) icon.hardlight(0.85f, 0f, 1.0f);
			else                icon.hardlight(0.35f, 0f, 0.7f);
		}
		public float iconFadePercent() { return GameMath.gate(0, visualcooldown() / 20f, 1); }

		private static final String SECOND_USE = "second_use";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(SECOND_USE, secondUse);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			secondUse = bundle.getBoolean(SECOND_USE);
		}
	};
	public static class DeadlyFollowupTracker extends FlavourBuff{
		public int object;
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(0.5f, 0f, 1f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
		private static final String OBJECT    = "object";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(OBJECT, object);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			object = bundle.getInt(OBJECT);
		}
	}
	public static class PreciseAssaultTracker extends FlavourBuff{
		{ type = buffType.POSITIVE; }

		int left = hero.heroClass == HeroClass.DUELIST ? 2 : 1;

		public int icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 1f, 0.0f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }

		private static final String LEFT    = "LEFT";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LEFT, left);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			left = bundle.getInt(LEFT);
		}
	};
	public static class CombinedLethalityAbilityTracker extends FlavourBuff{
		public MeleeWeapon weapon;
	};
	public static class CombinedLethalityTriggerTracker extends FlavourBuff{
		{ type = buffType.POSITIVE; }
		public int icon() { return BuffIndicator.CORRUPT; }
		public void tintIcon(Image icon) { icon.hardlight(0.6f, 0.15f, 0.6f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
	};
	public static class CombinedEnergyAbilityTracker extends FlavourBuff{
		public int energySpent = -1;
		public boolean wepAbilUsed = false;
	}
	public static class CounterAbilityTacker extends FlavourBuff{};

	int icon;
	int maxPoints;

	// tiers 1/2/3/4 start at levels 2/7/13/21
	public static int[] tierLevelThresholds = new int[]{0, 2, 7, 13, 21/*+4*/, 31};

	public static int talentAmount(HeroClass hCl, int tier){
		int amount = 5;
		if (tier == 2) amount++;
		if (hCl == HeroClass.RAT_KING) amount--;
		return amount;
	}

	public static int getMaxPoints(int tier) {
		if (tier >= 5) return 0;
		int max = tierLevelThresholds[tier+1] - tierLevelThresholds[tier];
		if(tier == 3) max += 4;
		if (hero != null && hero.talents.get(tier - 1).size() > talentAmount(hero.heroClass, tier) && tier < 3)
			max += 2 * (hero.talents.get(tier - 1).size() - talentAmount(hero.heroClass, tier));
		if (hero != null && hero.lvl > 30){
			max += (hero.lvl - 30)/3;
		}
		return max;
	}

	Talent( int icon ){
		this(icon, 2);
	}

	Talent( int icon, int maxPoints ){
		this.icon = icon;
		this.maxPoints = maxPoints;
	}

	public interface TalentCallback {
		void call(Talent talent, int points);
	}

	public static int imageAt(int x, int y){
		return x + y*32;
	}

	public int icon(){
		return icon;
	}

	public int maxPoints(){
		return maxPoints;
	}

	public String title(){
		return Messages.get(this, name() + ".title");
	}

	public final String desc(){
		return desc(false);
	}

	// this is an absurd way to check but it..works.
	public boolean isClassTalent(HeroClass cls) {
		ArrayList<LinkedHashMap<Talent, Integer>> talents = new ArrayList<>();
		initClassTalents(cls, talents);
		for (LinkedHashMap<Talent, Integer> tier : talents) if (tier.containsKey(this)) return true;
		return false;
	}

	// fixme there's gotta be a way to truncate the sheer amount of extra text that's about to show up.
	// todo also should decide if I want the comment to show up before or after the meta desc. currently it is set after
	public String desc(boolean metamorphed){
		String desc = Messages.get(this, name() + ".desc");
		if (metamorphed){
			String metaDesc = Messages.get(this, name() + ".meta_desc");
			if (!metaDesc.equals(Messages.NO_TEXT_FOUND)){
				desc += "\n\n" + metaDesc;
			}
		}
		String comment = Messages.get(this, name() + ".comment");
		//noinspection StringEquality
		return comment == Messages.NO_TEXT_FOUND ? desc : desc + "\n\n" + comment;
	}

	public static void onTalentUpgraded( Hero hero, Talent talent ){
		int points = hero.pointsInTalent(talent);
		switch(talent) {
			case IRON_WILL:
			case NOBLE_CAUSE:
				// lazily implementing this without checking hero class.
				Buff.affect(hero, BrokenSeal.WarriorShield.class);
				break;

			case VETERANS_INTUITION:
			case ADVENTURERS_INTUITION:
			case THIEFS_INTUITION:
			case ROYAL_INTUITION:
				for (Item item : hero.belongings) {
					// rerun these.
					item.collected = false;
					onItemCollected(hero, item);
					if (item.isEquipped(hero)) onItemEquipped(hero, item);
				}
				break;
			case SCHOLARS_INTUITION:
				for (Item item : hero.belongings) {
					if (item instanceof Scroll || item instanceof Potion) {
						for (int i = 0; i < item.quantity() && !item.isIdentified(); i++) {
							if (Random.Int(3 * points) == 0)
								item.identify(); // adjusts for the difference in chance.
						}
					}
				}
				break;
		}

		switch(talent) {
			case MENDING_SHADOWS: case NOBLE_CAUSE:
				if ( hero.invisible > 0 ){
					Buff.affect(hero, Talent.ProtectiveShadowsTracker.class);
				}
				break;

			case LIGHT_CLOAK:
				if (!hero.heroClass.is(HeroClass.ROGUE)) break;
			case RK_FREERUNNER:
				if (hero.pointsInTalent(LIGHT_CLOAK) == 1) {
					for (Item item : hero.belongings.backpack) {
						if (item instanceof CloakOfShadows) {
							if (hero.buff(LostInventory.class) == null || item.keptThroughLostInventory()) {
								((CloakOfShadows) item).activate(hero);
							}
						}
					}
				}
				break;
//			case BERSERKING_STAMINA: // takes immediate effect
//				Berserk berserk = hero.buff(Berserk.class);
//				if(berserk != null) berserk.recover(Berserk.STAMINA_REDUCTION);
//				break;

			case SEER_SHOT:
				float mod = points == 1 ? 0 : 1f/(points-1);
				for(RevealedArea buff : hero.buffs(RevealedArea.class)) buff.postpone(buff.cooldown() * mod);
				break;

			case HEIGHTENED_SENSES: case FARSIGHT: case RK_SNIPER: case KINGS_VISION:
				Dungeon.observe();
				break;

			case SECONDARY_CHARGE: case TWIN_UPGRADES: case DESPERATE_POWER:
				Item.updateQuickslot();
				break;

			case UNENCUMBERED_SPIRIT:
				if (points < 3) break;
				Item toGive = new ClothArmor().identify();
				if (!toGive.collect()){
					Dungeon.level.drop(toGive, hero.pos).sprite.drop();
				}
				toGive = new Gloves().identify();
				if (!toGive.collect()) {
					Dungeon.level.drop(toGive, hero.pos).sprite.drop();
					break;
				}
		}
	}

	public static class CachedRationsDropped extends CounterBuff{{revivePersists = true;}};
	public static class NatureBerriesAvailable extends CounterBuff{{revivePersists = true;}}; //for pre-1.3.0 saves
	public static class NatureBerriesDropped extends CounterBuff{{revivePersists = true;}};

	public static void onFoodEaten( Hero hero, float foodVal, Item foodSource ){
		final int[] healing = new int[1];
		hero.byTalent( (talent, points) -> {
			// somehow I managed to make it even more confusing than before.
			int factor = talent == HEARTY_MEAL ? 3 : 4;
			double missingHP = 1-(double)hero.HP/hero.HT;
			int strength = (int)(missingHP * factor);
			if(talent != HEARTY_MEAL) strength--; // missing 1/4 hp is not rewarded with healing normally.
			if(strength-- == 0) return; // adjusting for the addition of one point.
			strength += points;
			// hearty meal heals for (2.5/4)/(4/6). priv heals for (2/3)/(3/5)
			int boost = talent == HEARTY_MEAL && strength == 1
					? Random.round(2.5f) // simulate 2.5
					: (int) Math.ceil( (talent == HEARTY_MEAL ? 2.5 : 2) * Math.pow(1.5,strength-1) );
			healing[0] += boost;
		}, ROYAL_PRIVILEGE, HEARTY_MEAL);
		healing[0] = Math.min(healing[0], hero.HT - hero.HP); // prevent overheal

		if(healing[0] > 0) {
			hero.HP += healing[0];
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healing[0]), FloatingText.HEALING);

		}
		if (hero.hasTalent(IRON_STOMACH,ROYAL_MEAL)){
			if (hero.cooldown() > 0) {
				if (hero.hasTalent(IRON_STOMACH)) {
					Buff.prolong(hero, WarriorFoodImmunity.class, hero.cooldown()+1);
				} else {
					Buff.affect(hero, WarriorFoodImmunity.class, hero.cooldown());
				}

			}
		}
		boolean charge = false;
		if (hero.hasTalent(ROYAL_PRIVILEGE)){ // SHPD empowering meal talent
			//2/3 bonus wand damage for next 3 zaps
			int bonus = 1+hero.pointsInTalent(ROYAL_PRIVILEGE);
			Buff.affect( hero, WandEmpower.class).set(bonus, 3);
			ScrollOfRecharging.charge( hero );
		}
		if (hero.hasTalent(ENERGIZING_MEAL_I,ROYAL_MEAL)) {
			//5/8 turns of recharging.
			int points = hero.pointsInTalent(ENERGIZING_MEAL_I,ROYAL_MEAL);
			int duration = 2 + 3*points;
			if(hero.hasTalent(ENERGIZING_MEAL_I)) Buff.affect( hero, Recharging.class, duration);
			if(hero.hasTalent(ROYAL_MEAL)) Buff.prolong(hero, Recharging.class, duration);
			charge = true;
		}
		if (hero.hasTalent(ENERGIZING_MEAL_II)) {
			// 1/1.5 charges instantly replenished.
			hero.belongings.charge(0.5f*(1+hero.pointsInTalent(ENERGIZING_MEAL_II)),true);
			charge = true;
		}
		if(charge) ScrollOfRecharging.charge(hero);

		hero.byTalent( (talent, points) -> {
			//3/5 turns of recharging
			int duration = 1 + 2*points;
			ArtifactRecharge buff = Buff.affect( hero, ArtifactRecharge.class);
			boolean newBuff = true;
			if(talent == MYSTICAL_MEAL) {
				// current behavior 'corrupts' the existing buff if you use a horn of plenty
				// but this is preferable to the alternative...
				buff.prolong((float)Math.ceil(duration*1.5)); // 5-8 turns of recharge!!!
			}
			else if(buff.left() < duration){
				buff.set(duration);
			} else {
				newBuff = false;
			}
			buff.ignoreHornOfPlenty = buff.ignoreHornOfPlenty || newBuff && foodSource instanceof HornOfPlenty;
			ScrollOfRecharging.charge( hero );
			SpellSprite.show(hero, SpellSprite.CHARGE, 0, 1, 1);
		}, ROYAL_MEAL, MYSTICAL_MEAL );

		// TODO: palkia why
		// 4.5/6 tiles -> 3/5 turns
		hero.byTalent( (talent, points) -> Buff.affect(hero, Adrenaline.class, 2+2*points),
				INVIGORATING_MEAL);
		hero.byTalent( (talent, points) -> {
			//effectively 1/2 turns of haste
			Buff.prolong( hero, Haste.class, 0.67f+points);
			hero.sprite.emitter().burst(Speck.factory(Speck.JET), 4*points);
		}, ROYAL_MEAL);
		if (hero.hasTalent(EFFICIENT_TRAINING)){
			for (int i = 0; i < hero.pointsInTalent(EFFICIENT_TRAINING); i++){
				Buff.affect(hero, Combo.class).incCombo();
			}
			if (hero.buff(Combo.class) != null)
				Buff.affect(hero, Combo.class).addTime(8f);
		}
		if (hero.hasTalent(STRENGTHENING_MEAL)){
			//3 bonus physical damage for next 2/3 attacks
			Buff.affect( hero, PhysicalEmpower.class).set(3, 1 + 2*hero.pointsInTalent(STRENGTHENING_MEAL));
		}
		if (hero.hasTalent(FOCUSED_MEAL)){
			if (hero.heroClass == HeroClass.DUELIST){
				//1/1.5 charge for the duelist
				Buff.affect( hero, MeleeWeapon.Charger.class ).gainCharge(/*0.5f*/1f*(hero.pointsInTalent(FOCUSED_MEAL)+1));
			} else {
				// lvl/3 / lvl/2 bonus dmg on next hit for other classes
				Buff.affect( hero, PhysicalEmpower.class).set(Math.round(hero.lvl / (4f - hero.pointsInTalent(FOCUSED_MEAL))), 1);
			}
		}
		HighnessBuff.agreenalineProc();
	}

	public static class WarriorFoodImmunity extends FlavourBuff{
		{ actPriority = HERO_PRIO+1; }
	}

	public static class AdventurousSnoozingAntiAbuse extends FlavourBuff{}

	// royal intuition is additive, separate talents are multiplictive, however.
	public static float itemIDSpeedFactor( Hero hero, Item item ){
		float factor = 1f;

		// all royal intuition is now handled here.
		factor *= 1 + hero.pointsInTalent(ROYAL_INTUITION) * (0.75f + (
				item instanceof MeleeWeapon || item instanceof Armor ? 2 // armsmaster
						: item instanceof Ring ? 2 // thief's intuition
						: item instanceof Wand ? 3 // scholar's intuition
						: 0));

		factor *= 1 + 0.75f * 1.5f * hero.pointsInTalent(SURVIVALISTS_INTUITION);
		// 2x innate (+0) / instant for Warrior (see onItemEquipped)
		/*
		if (item instanceof MeleeWeapon || item instanceof Armor){
				factor *= 1f + hero.shiftedPoints(ARMSMASTERS_INTUITION);
		}
		*/
		// Affected by both Warrior(1.75x/2.5x) and Duelist(2.5x/inst.) talents
		if (item instanceof MeleeWeapon){
			factor *= 1f + 1.5f*hero.pointsInTalent(ADVENTURERS_INTUITION); //instant at +2 (see onItemEquipped)
			factor *= 1f + 0.75f*hero.pointsInTalent(VETERANS_INTUITION);
		}
		// Affected by both Warrior(2.5x/inst.) and Duelist(1.75x/2.5x) talents
		if (item instanceof Armor){
			factor *= 1f + 0.75f*hero.pointsInTalent(ADVENTURERS_INTUITION);
			factor *= 1f + hero.pointsInTalent(VETERANS_INTUITION); //instant at +2 (see onItemEquipped)
		}
		// 3x/instant for Mage (see Wand.wandUsed()), 4.5x/instant for rk
		// not shifted for Mage right now.
		if (item instanceof Wand){
			factor *= 1f + 2.0f*hero.pointsInTalent(SCHOLARS_INTUITION);
		}
		// 2x/instant for Rogue (see onItemEqupped), also id's type on equip/on pickup
		if (item instanceof Ring){
			factor *= 1f + hero.shiftedPoints(THIEFS_INTUITION);
		}
		return factor;
	}

	public static void onPotionUsed( Hero hero, int cell, float factor ){
		if (hero.hasTalent(LIQUID_WILLPOWER,RESTORATION)){
			if (hero.heroClass.is(HeroClass.WARRIOR)) {
				BrokenSeal.WarriorShield shield = hero.buff(BrokenSeal.WarriorShield.class);
				if (shield != null) {
					// 50/75% of total shield
					int shieldToGive = Math.round(factor * shield.maxShield() * hero.byTalent(/*stacks*/true, /*shifted*/true, LIQUID_WILLPOWER, 0.5f, RESTORATION, 0.25f));
					hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
					shield.supercharge(shieldToGive);
				}
			} else {
				// 5/7.5% of max HP
				int shieldToGive = Math.round( factor * hero.HT * hero.byTalent(/*stacks*/true, /*shifted*/true, LIQUID_WILLPOWER, 0.04f, RESTORATION, 0.025f));
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldToGive), FloatingText.SHIELDING);
				Buff.affect(hero, Barrier.class).setShield(shieldToGive);
			}
		}
		if (hero.hasTalent(LIQUID_NATURE, RESTORATION)){
			ArrayList<Integer> grassCells = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS9){
				grassCells.add(cell+i);
			}
			Random.shuffle(grassCells);
			for (int grassCell : grassCells){
				Char ch = Actor.findChar(grassCell);
				if (ch != null && ch.alignment == Char.Alignment.ENEMY){
					//1/2 turns of roots
					Buff.affect(ch, Roots.class, factor * hero.pointsInTalent(false, LIQUID_NATURE,RESTORATION));
				}
				if (Dungeon.level.map[grassCell] == Terrain.EMPTY ||
						Dungeon.level.map[grassCell] == Terrain.EMBERS ||
						Dungeon.level.map[grassCell] == Terrain.EMPTY_DECO){
					Level.set(grassCell, Terrain.GRASS);
					GameScene.updateMap(grassCell);
				}
				HighGrass.playVFX(grassCell);
			}
			// 4/6 cells total
			int totalGrassCells = (int) (factor * hero.byTalent(false,true,LIQUID_NATURE, 3/* 6/9 */, RESTORATION, 2));
			while (grassCells.size() > totalGrassCells){
				grassCells.remove(0);
			}
			for (int grassCell : grassCells){
				int t = Dungeon.level.map[grassCell];
				if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO || t == Terrain.EMBERS
						|| t == Terrain.GRASS || t == Terrain.FURROWED_GRASS)
						&& Dungeon.level.plants.get(grassCell) == null){
					Level.set(grassCell, Terrain.HIGH_GRASS);
					GameScene.updateMap(grassCell);
				}
			}
			Dungeon.observe();
		}
		if (hero.hasTalent(LIQUID_AGILITY)){
			Buff.prolong(hero, RestoredAgilityTracker.class, hero.cooldown() + Math.max(0, factor/*-1*/));
		}
		HighnessBuff.agreenalineProc();
	}

	public static void onScrollUsed( Hero hero, int pos, float factor ){
		if (hero.hasTalent(INSCRIBED_POWER, RESTORATION)){
			// 2/3 empowered wand zaps, 3 for inscribed power
			// inscribed power overrides restoration
			int left = hero.hasTalent(RESTORATION) ? hero.shiftedPoints(RESTORATION) : 3;
			Buff.affect(hero, ScrollEmpower.class).reset((int) factor * left);
		}
		if (hero.hasTalent(INSCRIBED_STEALTH, RESTORATION)){
			// 3/5 turns of stealth (restoration), . inscribed stealth overrides restoration
			int points;
			if (hero.hasTalent(INSCRIBED_STEALTH)) {
				points = hero.pointsInTalent(INSCRIBED_STEALTH);
				factor *= 2;
			} else {
				points = hero.pointsInTalent(RESTORATION);
			}
			Buff.affect(hero, Invisibility.class, factor * (1 + 2*points));
			Sample.INSTANCE.play( Assets.Sounds.MELD );
		}
		HighnessBuff.agreenalineProc();
	}

	public static void onArtifactUsed( Hero hero ){
		if (hero.hasTalent(ENHANCED_RINGS,RK_ASSASSIN)){
			float duration = 3f*hero.pointsInTalent(ENHANCED_RINGS,RK_ASSASSIN);
			if(hero.hasTalent(ENHANCED_RINGS)) Buff.affect(hero, EnhancedRings.class, duration);
			else Buff.prolong(hero, EnhancedRings.class, duration);
		}
		if (hero.hasTalent(HEROIC_STAMINA)){
			Buff.prolong(hero, Stamina.class, 1.25f * hero.pointsInTalent(HEROIC_STAMINA));
		}
		HighnessBuff.agreenalineProc();
	}

	public static void onItemEquipped( Hero hero, Item item ) {
		if (item.isIdentified()) return; // all talent interactions here regard identification
		boolean id = false;
		if (hero.pointsInTalent(ROYAL_INTUITION) == 2) {
			id = true;
		} else {
			// Armsmaster
			Talent shiftedTalent, otherTalent;
			if (item instanceof Armor) {
				shiftedTalent = VETERANS_INTUITION;
				otherTalent = ADVENTURERS_INTUITION;
			} else if (item instanceof Weapon) {
				shiftedTalent = ADVENTURERS_INTUITION;
				otherTalent = VETERANS_INTUITION;
			} else {
				shiftedTalent = otherTalent = null;
			}
			if (shiftedTalent != null) {
				// +1 for dedicated talent, +1 otherwise.
				id = hero.shiftedPoints(shiftedTalent, otherTalent) >= 2;
			} else if (item instanceof Ring) {
				// Thief's Intuition
				switch(hero.shiftedPoints(THIEFS_INTUITION, ROYAL_INTUITION)) {
					case 2: case 3: id = true; break;
					case 1: ((Ring) item).setKnown(); break;
				}
			}
		}
		if (id) {
			item.identify();
			if (hero.sprite.emitter() != null) hero.sprite.emitter().burst(
					Speck.factory(Speck.QUESTION),
					1
			);
		}
	}

	public static void onItemCollected( Hero hero, Item item ){
		if (item.isIdentified() || item.collected) return;
		item.collected = true;
		boolean id = false, curseID = item.cursedKnown;
		if (item instanceof Ring || item instanceof Artifact) {
			switch (hero.shiftedPoints(THIEFS_INTUITION, ROYAL_INTUITION)) {
				case 3: id = true; break;
				case 2:
					if (item instanceof Ring) ((Ring) item).setKnown();
					// +1 effect has a chance to id curses if it's actually cursed.
					curseID = curseID || hero.hasTalent(THIEFS_INTUITION) && item.cursed
							&& Random.Int(3) == 0;
					break;
			}
		}
		if (item instanceof Weapon || item instanceof Armor) {
			for (Talent talent : new Talent[]{VETERANS_INTUITION, ADVENTURERS_INTUITION}) {
				int points = hero.pointsInTalent(talent)-1; // we care about +1 +2 boosted +2
				// match talent to equipment type gets boost
				if (talent == VETERANS_INTUITION ^ item instanceof Weapon) points++;
				// 0%/0%/30%/60% to identify on pick-up with armsmaster talents
				if (0.3f * points > Random.Float()) {
					id = true;
					break;
				}
			}
		}
		// survivalist's intuition curse checking (.2/.4 chance)
		curseID = curseID || (item instanceof Wand || item instanceof EquipableItem && !(item instanceof MissileWeapon))
				&& Random.Int(5) < hero.pointsInTalent(SURVIVALISTS_INTUITION);
		// scholar's intuition consumable identification
		id = id || (item instanceof Scroll || item instanceof Potion)
				&& hero.hasTalent(SCHOLARS_INTUITION)
				&& Random.Int(4-hero.pointsInTalent(SCHOLARS_INTUITION)) == 0;

		if(id || curseID && !item.cursedKnown) {
			if (id) item.identify();
			else {
				// fixme this doesn't use .properties file.
				GLog.w("The %s is %s",
						item.name(),
						item.visiblyCursed() ? "cursed!" : "free of malevolent magic.");
			}
			if (hero.sprite.emitter() != null) hero.sprite.emitter().burst(
					Speck.factory(Speck.QUESTION),1);
		}
	}

	//note that IDing can happen in alchemy scene, so be careful with VFX here
	// near-identical talents in this area do stack, they're simple enough where it's really quite trivial to do so.
	public static void onItemIdentified( Hero hero, Item item ){
		int heal = 0;
		for(Talent talent : new Talent[]{TEST_SUBJECT, KINGS_WISDOM}) {
			//heal for 2/3 HP
			int points = hero.pointsInTalent(talent);
			if( points == 0 ) continue;
			heal += 1 + points;
			if(talent == TEST_SUBJECT) heal += points == 1 ? Random.Int(2) : 1; // 2-3/4
		}
		heal = Math.min(heal, hero.HT-hero.HP);
		if(heal > 0) {
			hero.HP += heal;
			if (hero.sprite != null) {
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
			}
		}
		hero.byTalent( (talent, points) -> {
			//2/3 turns of wand recharging
			int duration = 1 + points;
			if(talent == TESTED_HYPOTHESIS) duration = (int)Math.ceil(duration*1.5f); // 3/5
			Buff.affect(hero, Recharging.class, duration);
			ScrollOfRecharging.charge(hero);
		}, TESTED_HYPOTHESIS, KINGS_WISDOM);
	}

	public static int onAttackProc( Hero hero, Char enemy, int dmg ){
		if (hero.hasTalent(Talent.SUCKER_PUNCH,KINGS_WISDOM)
				&& enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)
				&& enemy.buff(SuckerPunchTracker.class) == null){
			int bonus = 0;
			if(hero.hasTalent(SUCKER_PUNCH)) bonus += 1+hero.pointsInTalent(SUCKER_PUNCH);  // 2/3
			if(hero.hasTalent(KINGS_WISDOM)) bonus += Random.round(0.5f*(2+hero.pointsInTalent(KINGS_WISDOM))); // 1-2/2
			dmg += bonus;
			Buff.affect(enemy, SuckerPunchTracker.class);
		}

		if (hero.hasTalent(Talent.FOLLOWUP_STRIKE,KINGS_WISDOM) && enemy.isAlive() && enemy.alignment == Char.Alignment.ENEMY) {
			if (hero.belongings.attackingWeapon() instanceof MissileWeapon && !Dungeon.isSpecialSeedEnabled(DungeonSeed.SpecialSeed.HUNTRESS)) {
				Buff.prolong(hero, FollowupStrikeTracker.class, 5f).object = enemy.id();
			} else if (hero.buff(FollowupStrikeTracker.class) != null
					&& hero.buff(FollowupStrikeTracker.class).object == enemy.id()){
				int bonus = 0;
				if(hero.hasTalent(KINGS_WISDOM)) bonus += hero.shiftedPoints(KINGS_WISDOM); // 2/3
				if(hero.hasTalent(FOLLOWUP_STRIKE)) bonus += Random.round(hero.shiftedPoints(FOLLOWUP_STRIKE) * 1.5f); // 3/4-5
				dmg += bonus;
				hero.buff(FollowupStrikeTracker.class).detach();
			}
		}

		/* fixme figure out if this is needed
		if (hero.buff(Talent.SpiritBladesTracker.class) != null
				&& Random.Int(10) < 3*hero.pointsInTalent(Talent.SPIRIT_BLADES)){
			SpiritBow bow = hero.belongings.getItem(SpiritBow.class);
			if (bow != null) dmg = bow.proc( hero, enemy, dmg );
			hero.buff(Talent.SpiritBladesTracker.class).detach();
		}*/

		if (hero.hasTalent(PATIENT_STRIKE)){
			if (hero.buff(PatientStrikeTracker.class) != null
					&& !(hero.belongings.attackingWeapon() instanceof MissileWeapon)){
				hero.buff(PatientStrikeTracker.class).detach();
				dmg += hero.pointsInTalent(Talent.PATIENT_STRIKE) == 1 ? 3 : Random.IntRange(4,5);
			}
		}

		if (hero.hasTalent(DEADLY_FOLLOWUP) && enemy.alignment == Char.Alignment.ENEMY) {
			if (hero.belongings.attackingWeapon() instanceof MissileWeapon) {
				if (!(hero.belongings.attackingWeapon() instanceof SpiritBow.SpiritArrow)) {
					Buff.prolong(hero, DeadlyFollowupTracker.class, 5f).object = enemy.id();
				}
			} else if (hero.buff(DeadlyFollowupTracker.class) != null
					&& hero.buff(DeadlyFollowupTracker.class).object == enemy.id()){
				dmg = Math.round(dmg * (1.0f + /*.08f*/.16f*hero.pointsInTalent(DEADLY_FOLLOWUP)));
			}
		}



		if (hero.hasTalent(BEAR_PAW) && hero.HP <= hero.HT / 4){
			Buff.affect(enemy, Bleeding.class).set(dmg / 3f);
			if (hero.pointsInTalent(BEAR_PAW) > 2){
				int healAmt = Math.round(dmg * 0.33f);
				healAmt = Math.min( healAmt, hero.HT - hero.HP );
				hero.HP += healAmt;
				hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 1 );
				hero.sprite.showStatus( CharSprite.POSITIVE, Integer.toString( healAmt ) );
			}
		}

		if (hero.pointsInTalent(MECHANICAL_POWER) > 2 && RobotBuff.isRobot() &&
				(enemy instanceof FinalFroggit || enemy instanceof DM100 || enemy instanceof Warlock ||
				enemy instanceof Dragon || enemy instanceof Succubus || enemy instanceof Eye ||
				enemy instanceof LostSpirit || enemy instanceof SpectreRat || enemy instanceof Shaman ||
				enemy instanceof Necromancer || enemy instanceof Phantom || enemy instanceof Golem ||
				enemy instanceof Elemental || enemy instanceof DarkestElf || enemy instanceof YogFist)){
				enemy.sprite.emitter().burst(ExoParticle.FACTORY, 25);
				Sample.INSTANCE.play(Assets.Sounds.DEGRADE, 1f, 2.5f);
				Buff.affect(enemy, AntiMagicBuff.class, 3f);
		}

		HighnessBuff.agreenalineProc();

		return dmg;
	}

	public static class SuckerPunchTracker extends Buff{};
	public static class FollowupStrikeTracker extends FlavourBuff{
		public int object;
		{ type = Buff.buffType.POSITIVE; }
		public int icon() { return BuffIndicator.INVERT_MARK; }
		public void tintIcon(Image icon) { icon.hardlight(0f, 0.75f, 1f); }
		public float iconFadePercent() { return Math.max(0, 1f - (visualcooldown() / 5)); }
		private static final String OBJECT    = "object";
		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(OBJECT, object);
		}
		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			object = bundle.getInt(OBJECT);
		}
	};
	public static class AntiMagicBuff extends FlavourBuff{};

	public interface SpellbladeForgeryWeapon {};

	public static class SpellbladeForgeryWound extends Wound {
		int color;

		@Override
		public void update() {
			super.update();

			hardlight(color);
		}

		public static void hit(int pos, float angle, int color ) {
			Group parent = Dungeon.hero.sprite.parent;
			SpellbladeForgeryWound w = parent.recycle( SpellbladeForgeryWound.class );
			parent.bringToFront( w );
			w.reset( pos );
			w.angle = angle;
			w.color = color;
		}
	}

	public static final int MAX_TALENT_TIERS = 4;

	public static Talent[] talentList(HeroClass cl, int tier){
		switch (tier){
			case 1: default:
				switch (cl){
					case WARRIOR: default:
						return new Talent[]{HEARTY_MEAL, VETERANS_INTUITION, TEST_SUBJECT, IRON_WILL, WEAPON_MASTERY};
					case MAGE:
						return new Talent[]{ENERGIZING_MEAL_I, SCHOLARS_INTUITION, TESTED_HYPOTHESIS, BACKUP_BARRIER, ARCANE_BOOST};
					case ROGUE:
						return new Talent[]{CACHED_RATIONS, THIEFS_INTUITION, SUCKER_PUNCH, MENDING_SHADOWS, FARADAY_CAGE};
					case HUNTRESS:
						return new Talent[]{NATURES_BOUNTY, SURVIVALISTS_INTUITION, FOLLOWUP_STRIKE, SCOUTS_BARRIER, NATURE_AID_2};
					case DUELIST:
						return new Talent[]{STRENGTHENING_MEAL, ADVENTURERS_INTUITION, PATIENT_STRIKE, AGGRESSIVE_BARRIER, ADVENTUROUS_SNOOZING};
					case RAT_KING:
						return new Talent[]{ROYAL_PRIVILEGE, ROYAL_INTUITION, KINGS_WISDOM, NOBLE_CAUSE};
				}
			case 2:
				switch (cl){
					case WARRIOR: default:
						return new Talent[]{IRON_STOMACH, WILLPOWER_OF_INJURED, RUNIC_TRANSFERENCE, LETHAL_MOMENTUM, IMPROVISED_PROJECTILES, BIG_RUSH};
					case MAGE:
						return new Talent[]{ENERGIZING_MEAL_II, ENERGIZING_UPGRADE, WAND_PRESERVATION, ARCANE_VISION, SHIELD_BATTERY, PYROMANIAC};
					case ROGUE:
						return new Talent[]{MYSTICAL_MEAL, DUAL_WIELDING, WIDE_SEARCH, SILENT_STEPS, PROTEIN_INFUSION, EFFICIENT_SHADOWS};
					case HUNTRESS:
						return new Talent[]{INVIGORATING_MEAL, IVYLASH, REJUVENATING_STEPS, HEIGHTENED_SENSES, DURABLE_PROJECTILES, SCOURGING_THE_UNIVERSE};
					case DUELIST:
						return new Talent[]{FOCUSED_MEAL, SIXTH_SENSE, WEAPON_RECHARGING, LETHAL_HASTE, SWIFT_EQUIP, SPELLBLADE_FORGERY};
					case RAT_KING:
						return new Talent[]{ROYAL_MEAL, RESTORATION, POWER_WITHIN, KINGS_VISION, PURSUIT};
				}
			case 3:
				switch (cl){
					case WARRIOR: default:
						return new Talent[]{HOLD_FAST, STRONGMAN, BEAR_PAW};
					case MAGE:
						return new Talent[]{DESPERATE_POWER, ALLY_WARP, CRYONIC_SPELL};
					case ROGUE:
						return new Talent[]{ENHANCED_RINGS, LIGHT_CLOAK, TRAPPER_MASTERY};
					case HUNTRESS:
						return new Talent[]{POINTY_BLACK, LIKE_A_BULLET, AUTO_RELOAD};
					case DUELIST:
						return new Talent[]{PRECISE_ASSAULT, DEADLY_FOLLOWUP, GRASSY_OFFENSE};
					case RAT_KING:
						return new Talent[]{};
				}
			case 4:
				switch (cl){
					case WARRIOR:
						return new Talent[]{HEROIC_ENDURANCE};
					case MAGE:
						return new Talent[]{HEROIC_WIZARDRY};
					case ROGUE:
						return new Talent[]{HEROIC_STAMINA};
					case HUNTRESS:
						return new Talent[]{HEROIC_ARCHERY};
					case DUELIST:
						return new Talent[]{HEROIC_ADAPTABILITY};
					case RAT_KING:
						return new Talent[]{HEROIC_RATINESS};
				}
		}
        return new Talent[0];
    }

	public static boolean trolling;

	public static void initClassTalents( Hero hero ){
		initClassTalents( hero.heroClass, hero.talents, hero.metamorphedTalents );
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents){
		initClassTalents( cls, talents, new LinkedHashMap<>());
	}

	public static void initClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Talent> replacements){
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 1
		Collections.addAll(tierTalents, talentList(cls, 1));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(0).put(talent, 0);
		}
		tierTalents.clear();

		//tier 2
		Collections.addAll(tierTalents, talentList(cls, 2));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(1).put(talent, 0);
		}
		tierTalents.clear();

		//tier 3
		Collections.addAll(tierTalents, talentList(cls, 3));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();

		//tier 4
		Collections.addAll(tierTalents, talentList(cls, 4));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(3).put(talent, 0);
		}
		tierTalents.clear();
	}

	public static void initSecondClassTalents( HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents, LinkedHashMap<Talent, Talent> replacements){
		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 1
		Collections.addAll(tierTalents, talentList(cls, 1));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(0).put(talent, 0);
		}
		tierTalents.clear();

		//tier 2
		Collections.addAll(tierTalents, talentList(cls, 2));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(1).put(talent, 0);
		}
		tierTalents.clear();

		//tier 3
		Collections.addAll(tierTalents, talentList(cls, 3));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(2).put(talent, 0);
		}
		tierTalents.clear();

		//tier 4
		Collections.addAll(tierTalents, talentList(cls, 4));
		for (Talent talent : tierTalents){
			if (replacements.containsKey(talent)){
				talent = replacements.get(talent);
			}
			talents.get(3).put(talent, 0);
		}
		tierTalents.clear();
	}

	public static void initSubclassTalents( Hero hero ){
		initSubclassTalents( hero.subClass, hero.talents );
	}

	public static void initSubclassTalents( HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents){
		initSubclassTalents( cls, talents, 2);
	}

	public static void initSubclassTalents( HeroSubClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents, int tier){
		if (cls == HeroSubClass.NONE) return;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		ArrayList<Talent> tierTalents = new ArrayList<>();

		//tier 3
		switch (cls){
			case BERSERKER: default:
				Collections.addAll(tierTalents, INDISCRIMINATE_RAGE, DEATHLESS_FURY, ENRAGED_CATALYST, BERSERKING_STAMINA, ONE_MAN_ARMY, BRAVERY);
				break;
			case BRAWLER:
				Collections.addAll(tierTalents, POWER_STABILIZER, ARCANITY_ENSUES, IN_MY_MEMORIES, HOLERIC_BURST, PRIDE_OF_STEEL);
				break;
			case GLADIATOR:
				Collections.addAll(tierTalents, BATTLE_TENDENCY, LETHAL_DEFENSE, ENHANCED_COMBO, SKILL, EFFICIENT_TRAINING);
				break;
			case BATTLEMAGE:
				Collections.addAll(tierTalents, EMPOWERED_STRIKE, MYSTICAL_CHARGE, EXCESS_CHARGE, SORCERY, SPECTRE_ALLIES);
				break;
			case WARLOCK:
				Collections.addAll(tierTalents, SOUL_EATER, SOUL_SIPHON, NECROMANCERS_MINIONS, WARLOCKS_TOUCH, BANISHED);
				break;
			case SPIRITUALIST:
				Collections.addAll(tierTalents, ECTOTOUGHNESS, I_HATE_ALL_ELEMENTS, STAB_FROM_NOWHERE, POWER_IN_NUMBERS, MIND_BREAKER);
				break;
			case ASSASSIN:
				Collections.addAll(tierTalents, BLOODBATH, THINKING_WITH_PORTALS, ADAPT_AND_OVERCOME, LETHAL_MOMENTUM_2, DARKENING_STEPS);
				break;
			case FREERUNNER:
				Collections.addAll(tierTalents, EVASIVE_ARMOR, PROJECTILE_MOMENTUM, SPEEDY_STEALTH, FAST_RECOVERY, OLYMPIC_STATS);
				break;
			case DECEPTICON:
				Collections.addAll(tierTalents, ENERGON_FUSION, LASER_PRECISION, ERADICATING_CHARGE, VOID_WRATH, MECHANICAL_POWER);
				break;
			case SNIPER:
				Collections.addAll(tierTalents, FARSIGHT, SHARED_ENCHANTMENT, SHARED_UPGRADES, MULTISHOT, ARCHERY_MARK);
				break;
			case WARDEN:
				Collections.addAll(tierTalents, DURABLE_TIPS, BARKSKIN, SHIELDING_DEW,
						NATURES_BETTER_AID, INDIRECT_BENEFITS);
				break;
			case CHAMPION:
				Collections.addAll(tierTalents, SECONDARY_CHARGE, TWIN_UPGRADES, COMBINED_LETHALITY, ELITE_DEXTERITY, HELPER_TO_HERO);
				break;
			case MONK:
				Collections.addAll(tierTalents, UNENCUMBERED_SPIRIT, MONASTIC_VIGOR, COMBINED_ENERGY/*, MONK_6TH_ABILITY*/, ATTUNEXPLORATION, GRASS_MUNCHING);
				break;
			case HIGHNESS:
				Collections.addAll(tierTalents, PARTY_FEELING, SLASH_RUNNER, PROLONGED_JOY, WOUND_IGNORANCE, AGREENALINE_RUSH);
				break;
			case KING: // this should be *lovely*
				Collections.addAll(tierTalents, RK_BERSERKER, RK_BATTLEMAGE, RK_ASSASSIN, RK_SNIPER, RK_GLADIATOR, RK_WARLOCK, RK_FREERUNNER, RK_WARDEN); break;
			case RK_CHAMPION:
				Collections.addAll(tierTalents, RK_FIRE, RK_PROJECT, RK_ANTIMAGIC, RK_GIANT, RK_BLESSED, RK_CURSED, RK_SPLINT, RK_PALADIN); break;
		}
		for (Talent talent : tierTalents){
			talents.get(tier).put(talent, 0);
		}
		tierTalents.clear();

	}

	public static void initArmorTalents( Hero hero ){
		initArmorTalents( hero.armorAbility, hero.talents);
	}

	public static ArrayList<LinkedHashMap<Talent, Integer>> initArmorTalents(ArmorAbility abil){
		return initArmorTalents(abil, new ArrayList());
	}
	public static ArrayList<LinkedHashMap<Talent, Integer>> initArmorTalents(ArmorAbility abil, ArrayList<LinkedHashMap<Talent, Integer>> talents ){
		if (abil == null) return talents;

		while (talents.size() < MAX_TALENT_TIERS){
			talents.add(new LinkedHashMap<>());
		}

		for (Talent t : abil.talents()){
			talents.get(3).put(t, 0);
		}
		return talents;
	}

	private static final String TALENT_TIER = "talents_tier_";

	public static void storeTalentsInBundle( Bundle bundle, Hero hero ){
		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = new Bundle();

			for (Talent talent : tier.keySet()){
				tierBundle.put(talent.name(), tier.get(talent));
				if (tierBundle.contains(talent.name())){
					tier.put(talent, Math.min(tierBundle.getInt(talent.name()), talent.maxPoints()));
				}
			}
			bundle.put(TALENT_TIER+(i+1), tierBundle);
		}

		Bundle replacementsBundle = new Bundle();
		for (Talent t : hero.metamorphedTalents.keySet()){
			replacementsBundle.put(t.name(), hero.metamorphedTalents.get(t));
		}
		bundle.put("replacements", replacementsBundle);
	}

	public static final HashMap<Integer, Talent[]> bonusTalents = new HashMap<>();
	static {
		bonusTalents.put(1, new Talent[]{
				NATURES_AID
		});
		bonusTalents.put(2, new Talent[]{
				LIQUID_WILLPOWER, LIQUID_NATURE,
				INSCRIBED_POWER, INSCRIBED_STEALTH,
				ROGUES_FORESIGHT, LIQUID_AGILITY
		});
		bonusTalents.put(3, new Talent[]{
				POINT_BLANK
		});
		bonusTalents.put(4, new Talent[]{});
	};

	private static final HashSet<String> removedTalents = new HashSet<>();
	static{
		//v2.2.0
		removedTalents.add("EMPOWERING_SCROLLS");
	}
	private static final HashMap<String, String> renamedTalents = new HashMap();
	//TODO: make this list not proc (?) if we want to have both RKA and RKPD2 second t2 talents accessible
	static {
		//RKA v2.0.0
		renamedTalents.put("RESTORED_WILLPOWER",        "WILLPOWER_OF_INJURED");
		renamedTalents.put("ENERGIZING_UPGRADE",        "ENERGIZING_UPGRADE");
		renamedTalents.put("MYSTICAL_UPGRADE",          "DUAL_WIELDING");
		renamedTalents.put("RESTORED_NATURE",           "IVYLASH");
		renamedTalents.put("NATURES_AID",               "SCOUTS_BARRIER");
		renamedTalents.put("ARMSMASTERS_INTUITION",     "VETERANS_INTUITION");
		renamedTalents.put("ENHANCED_LETHALITY",        "BLOODBATH");
		renamedTalents.put("ASSASSINS_REACH",           "THINKING_WITH_PORTALS");
		renamedTalents.put("BOUNTY_HUNTER",             "ADAPT_AND_OVERCOME");
		renamedTalents.put("ROGUES_FORESIGHT",          "PROTEIN_INFUSION");
		renamedTalents.put("POINT_BLANK",               "POINTY_BLACK");
		renamedTalents.put("SEER_SHOT",                 "LIKE_A_BULLET");
		renamedTalents.put("ENDLESS_RAGE",              "INDISCRIMINATE_RAGE");
		renamedTalents.put("CLEAVE",                    "BATTLE_TENDENCY");
	};

	public static void restoreTalentsFromBundle( Bundle bundle, Hero hero ){
		if (bundle.contains("replacements")){
			Bundle replacements = bundle.getBundle("replacements");
			for (String key : replacements.getKeys()){
				String value = replacements.getString(key);
				//only process if we are porting to 2.0
				if (Dungeon.initialVersion < ShatteredPixelDungeon.RKA_2_0) {
					if (renamedTalents.containsKey(key)) key = renamedTalents.get(key);
					if (renamedTalents.containsKey(value)) value = renamedTalents.get(value);
				}
                if (!removedTalents.contains(key) && !removedTalents.contains(value)) {
                    try {
                        hero.metamorphedTalents.put(Talent.valueOf(key), Talent.valueOf(value));
                    } catch (Exception e) {
                        ShatteredPixelDungeon.reportException(e);
                    }
                }
			}
		}

		if (hero.heroClass != null)     initClassTalents(hero);
		if (hero.heroClass2 != null) initSecondClassTalents(hero.heroClass2, hero.talents, hero.metamorphedTalents);
		if (hero.subClass != null)  initSubclassTalents(hero.subClass, hero.talents, hero.heroClass2 != null ? 3 : 2);
		if (hero.subClass2 != null) initSubclassTalents(hero.subClass2, hero.talents, 3);
		if (hero.armorAbility != null)  initArmorTalents(hero);

		for (int i = 0; i < MAX_TALENT_TIERS; i++){
			LinkedHashMap<Talent, Integer> tier = hero.talents.get(i);
			Bundle tierBundle = bundle.contains(TALENT_TIER+(i+1)) ? bundle.getBundle(TALENT_TIER+(i+1)) : null;

			if (tierBundle != null){
				// handle my swapping of the two talents
				for (String tName : tierBundle.getKeys()){
					int points = tierBundle.getInt(tName);
					if (renamedTalents.containsKey(tName)) tName = renamedTalents.get(tName);
					if (!removedTalents.contains(tName)) {
						try {
							Talent talent = Talent.valueOf(tName);
							if (talent == BERSERKING_STAMINA && !tierBundle.contains(DEATHLESS_FURY.name())) {
								talent = DEATHLESS_FURY;
							}
							tier.put(talent, Math.min(points, talent.maxPoints()));
						} catch (Exception e) {
							ShatteredPixelDungeon.reportException(e);
						}
					}
				}
			}
		}
	}

}
