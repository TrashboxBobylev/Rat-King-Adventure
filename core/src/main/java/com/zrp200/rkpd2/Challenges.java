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

package com.zrp200.rkpd2;

import com.zrp200.rkpd2.items.Dewdrop;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.HornOfPlenty;
import com.zrp200.rkpd2.items.food.Blandfruit;
import com.zrp200.rkpd2.items.rings.RingOfMight;
import com.badlogic.gdx.utils.OrderedMap;
import com.watabou.utils.DeviceCompat;

public class Challenges {

	//Some of these internal IDs are outdated and don't represent what these challenges do
	public static final int NO_FOOD				= 1;
	public static final int NO_ARMOR			= 2;
	public static final int NO_HEALING			= 4;
	public static final int NO_HERBALISM		= 8;
	public static final int SWARM_INTELLIGENCE	= 16;
	public static final int DARKNESS			= 32;
	public static final int NO_SCROLLS		    = 64;
	public static final int CHAMPION_ENEMIES	= 128;
	public static final int STRONGER_BOSSES 	= 256;

	public static final int FATIQUE             = 512;
	public static final int NO_ACCURACY         = 1024;
	public static final int NO_HP               = 2048;
	public static final int BURN                = 4096;
	public static final int HERO_PATHING        = 8192;
	public static final int FORGET_PATH         = 16384;
	public static final int REDUCED_POWER       = 32768;
	public static final int RANDOM_HP           = 65536;
	public static final int EVIL_MODE           = 131072;

	public static final int NO_VEGAN            = 262144;
	public static final int ALLERGY             = 524288;
	public static final int UNSTABLE_DAMAGE     = 1048576;
	public static final int NO_ALCHEMY          = 2097152;
	public static final int MANY_MOBS			= 4194304;
	public static final int UNLIMITED_VISION    = 8388608;
	public static final int NO_STR              = 16777216;
	public static final int KROMER              = 33554432;
	public static final int NO_TALENTS          = 67108864;

	public static final int TOO_MANY_MOBS       = 134217728;
	public static final int JUST_KILL_ME        = 268435456;
	public static final int NO_LEVELS           = 536870912;


	public static OrderedMap<String, Integer> defaultChals = new OrderedMap<>();
	static {
		defaultChals.put("champion_enemies", CHAMPION_ENEMIES);
		defaultChals.put("stronger_bosses", STRONGER_BOSSES);
		defaultChals.put("no_food", NO_FOOD);
		defaultChals.put("no_armor", NO_ARMOR);
		defaultChals.put("no_healing", NO_HEALING);
		defaultChals.put("no_herbalism", NO_HERBALISM);
		defaultChals.put("swarm_intelligence", SWARM_INTELLIGENCE);
		defaultChals.put("darkness", DARKNESS);
		defaultChals.put("no_scrolls", NO_SCROLLS);
	}

	//summoning's solution would be better, but cmon, conduct refactoring for meme dlc?
	public static OrderedMap<String, Integer> availableChallenges(){
		OrderedMap<String, Integer> chals = new OrderedMap<>(defaultChals);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_NO_FOOD) || DeviceCompat.isDebug()) chals.put("fatique", FATIQUE);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_NO_ARMOR) || DeviceCompat.isDebug()) chals.put("no_accuracy", NO_ACCURACY);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_NO_HEALING) || DeviceCompat.isDebug()) chals.put("no_hp", NO_HP);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_NO_HERBALISM) || DeviceCompat.isDebug()) chals.put("burn", BURN);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_SWARM) || DeviceCompat.isDebug()) chals.put("hero_pathing", HERO_PATHING);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_DARKNESS) || DeviceCompat.isDebug()) chals.put("forget_path", FORGET_PATH);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_NO_SCROLLS) || DeviceCompat.isDebug()) chals.put("reduced_power", REDUCED_POWER);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_CHAMPS) || DeviceCompat.isDebug()) chals.put("random_hp", RANDOM_HP);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_BOSSES) || DeviceCompat.isDebug()) chals.put("evil_mode", EVIL_MODE);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_FATIQUE) || DeviceCompat.isDebug()) chals.put("no_vegan", NO_VEGAN);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_NO_ACCURACY) || DeviceCompat.isDebug()) chals.put("allergy", ALLERGY);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_NO_HP) || DeviceCompat.isDebug()) chals.put("unstable_damage", UNSTABLE_DAMAGE);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_BURN) || DeviceCompat.isDebug()) chals.put("no_alchemy", NO_ALCHEMY);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_HERO_PATHING) || DeviceCompat.isDebug()) chals.put("many_mobs", MANY_MOBS);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_FORGET_PATH) || DeviceCompat.isDebug()) chals.put("unlimited_vision", UNLIMITED_VISION);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_REDUCED_POWER) || DeviceCompat.isDebug()) chals.put("no_str", NO_STR);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_RANDOM_HP) || DeviceCompat.isDebug()) chals.put("kromer", KROMER);
		if (Badges.isUnlocked(Badges.Badge.CHAMPED_EVIL_MODE) || DeviceCompat.isDebug()) chals.put("no_talents", NO_TALENTS);
		if (Badges.isUnlocked(Badges.Badge.CHAMPION_3) || DeviceCompat.isDebug()) chals.put("too_many_mobs", TOO_MANY_MOBS);
		if (Badges.isUnlocked(Badges.Badge.CHAMPION_5) || DeviceCompat.isDebug()) chals.put("just_kill_me", JUST_KILL_ME);
		if (Badges.isUnlocked(Badges.Badge.CHAMPION_7) || DeviceCompat.isDebug()) chals.put("no_levels", NO_LEVELS);

		return chals;
	}

	public static int activeChallenges(){
		int chCount = 0;
		for (int ch : Challenges.availableChallenges().values().toArray()){
			if ((Dungeon.challenges & ch) != 0) chCount++;
		}
		return chCount;
	}

	public static boolean isItemBlocked( Item item ){

		if (Dungeon.isChallenged(NO_HERBALISM) && item instanceof Dewdrop){
			return true;
		}
		if (Dungeon.isChallenged(NO_VEGAN) && (item instanceof HornOfPlenty || item instanceof Blandfruit)){
			return true;
		}
		if (Dungeon.isChallenged(NO_STR) && (item instanceof RingOfMight)){
			return true;
		}

		return false;

	}

}