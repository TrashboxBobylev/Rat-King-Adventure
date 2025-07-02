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

package com.zrp200.rkpd2.utils;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.WarriorParry;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.sprites.EchSprite;
import com.zrp200.rkpd2.sprites.ItemSprite;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.sprites.RatSprite;
import com.zrp200.rkpd2.ui.BuffIcon;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.ui.Icons;
import com.zrp200.rkpd2.ui.TalentIcon;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

//This class defines the parameters for seeds in ShatteredPD and contains a few convenience methods
public class DungeonSeed {

	public static long TOTAL_SEEDS = 5429503678976L; //larges possible seed has a value of 26^9

	//Seed codes take the form @@@-@@@-@@@ where @ is any letter from A to Z (only uppercase)
	//This is effectively a base-26 number system, therefore 26^9 unique seeds are possible.

	//Seed codes exist to make sharing and inputting seeds easier
	//ZZZ-ZZZ-ZZZ is much easier to enter and share than 5,429,503,678,975

	//generates a random seed, omitting seeds that contain vowels (to minimize real words appearing randomly)
	//This means that there are 21^9 = 794,280,046,581 unique seeds that can be randomly generated
	public static long randomSeed(){
		Long seed;
		String seedText;
		do {
			seed = Random.Long(TOTAL_SEEDS);
			seedText = convertToCode(seed);
		} while (seedText.contains("A") || seedText.contains("E") || seedText.contains("I") || seedText.contains("O") || seedText.contains("U"));
		return seed;
	}

	//Takes a seed code (@@@@@@@@@) and converts it to the equivalent long value
	public static long convertFromCode( String code ){
		//if code is formatted properly, force uppercase
		if (code.length() == 11 && code.charAt(3) == '-' && code.charAt(7) == '-'){
			code = code.toUpperCase(Locale.ROOT);
		}

		//ignore whitespace characters and dashes
		code = code.replaceAll("[-\\s]", "");

		if (code.length() != 9) {
			throw new IllegalArgumentException("codes must be 9 A-Z characters.");
		}

		long result = 0;
		for (int i = 8; i >= 0; i--) {
			char c = code.charAt(i);
			if (c > 'Z' || c < 'A')
				throw new IllegalArgumentException("codes must be 9 A-Z characters.");

			result += (c - 65) * Math.pow(26, (8 - i));
		}
		return result;
	}

	//Takes a long value and converts it to the equivalent seed code
	public static String convertToCode( long seed ){
		if (seed < 0 || seed >= TOTAL_SEEDS) {
			throw new IllegalArgumentException("seeds must be within the range [0, TOTAL_SEEDS)");
		}

		//this almost gives us the right answer, but its 0-p instead of A-Z
		String interrim = Long.toString(seed, 26);
		StringBuilder result = new StringBuilder();

		//so we convert
		for (int i = 0; i < 9; i++) {

			if (i < interrim.length()){
				char c = interrim.charAt(i);
				if (c <= '9') c += 17; //convert 0-9 to A-J
				else          c -= 22; //convert a-p to K-Z

				result.append(c);

			} else {
				result.insert(0, 'A'); //pad with A (zeroes) until we reach length of 9

			}
		}

		//insert dashes for readability
		result.insert(3, '-');
		result.insert(7, '-');

		return result.toString();
	}

	//Creates a seed from arbitrary user text input
	public static long convertFromText( String inputText ){
		if (inputText.isEmpty()) return -1;

		//First see if input is a seed code, use that format if it is
		try {
			return convertFromCode(inputText);
		} catch (IllegalArgumentException e){

		}

		//Then see if input is a number (ignoring spaces), if so parse as a long seed (with overflow)
		try {
			return Long.parseLong(inputText.replaceAll("\\s", "")) % TOTAL_SEEDS;
		} catch (NumberFormatException e){

		}

		//Finally, if the user has entered unformatted text, convert it to a long seed equivalent
		// This is basically the same as string.hashcode except with long, and overflow
		// this lets the user input 'fun' seeds, like names or places
		long total = 0;
		for (char c : inputText.toCharArray()){
			total = 31 * total + c;
		}
		if (total < 0) total += Long.MAX_VALUE;
		total %= TOTAL_SEEDS;
		return total;
	}


	public static String formatText( String inputText ){
		try {
			//if the seed matches a code, then just convert it to using the code system
			return convertToCode(convertFromCode(inputText));
		} catch (IllegalArgumentException e){
			//otherwise just return the input text
			return inputText;
		}
	}

	public enum SpecialSeed {
		RATS("RAT-RAT-RAT"){
			@Override
			public Image getIcon() {
				return new RatSprite();
			}
		},
		ROGUE("ROG-UEB-UFF"){
			@Override
			public Image getIcon() {
				return new ItemSprite(ItemSpriteSheet.ARTIFACT_CLOAK);
			}
		},
		REVERSE("REV-ERS-EED"){
			@Override
			public Image getIcon() {
				Image icon = Icons.get(Icons.STAIRS);
				icon.invert();
				return icon;
			}
		},
		NO_WALLS("NOW-ALL-SHE"){
			@Override
			public Image getIcon() {
				return new Image(Assets.Environment.TILES_ABYSS, 128, 16, 16, 16);
			}
		},
		RANDOM_ITEMS("RNG-ITE-MSS"){
			@Override
			public Image getIcon() {
				ItemSprite sprite = new ItemSprite(ItemSpriteSheet.SOMETHING);
				sprite.glow(new ItemSprite.Glowing());
				return sprite;
			}
		},
		CHESTS("ITE-MCH-EST"){
			@Override
			public Image getIcon() {
				return new ItemSprite(ItemSpriteSheet.CHEST);
			}
		},
		BIGGER("EXP-ANS-IVE"){
			@Override
			public Image getIcon() {
				return Icons.get(Icons.STAIRS_LARGE);
			}
		},
		ALLIES("BES-TFR-END"){
			@Override
			public Image getIcon() {
				return new BuffIcon(BuffIndicator.HEART, true);
			}
		},
		ECH("ECH-ECH-ECH"){
			@Override
			public Image getIcon() {
				return new EchSprite();
			}
		},
		EVERYTHING("BOB-PAL-KIA"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				list.addAll(Arrays.asList(SpecialSeed.values()));
			}
		},
		NO_WARP("ROT-INM-IND"){
			@Override
			public Image getIcon() {
				Image sprite = new BuffIcon(BuffIndicator.WARP, true);
				sprite.hardlight(0x333333);
				return sprite;
			}
		},
		CAPITALISM("IWA-NTM-ONY"){
			@Override
			public Image getIcon() {
				return new ItemSprite(ItemSpriteSheet.GOLD);
			}
		},
		CORROSION("COR-ROS-ION"){
			@Override
			public Image getIcon() {
				return new ItemSprite(ItemSpriteSheet.WAND_CORROSION);
			}
		},
		ENCHANTED_WORLD("GLO-WIN-GGG"){
			@Override
			public Image getIcon() {
				return new ItemSprite(ItemSpriteSheet.STONE_ENCHANT);
			}
		},
		DUNGEONEER("WHO-LEP-ACK"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(CHESTS);
				list.add(BIGGER);
			}

			@Override
			public Image getIcon() {
				return new ItemSprite(ItemSpriteSheet.LOCKED_CHEST);
			}
		},
		RLETTER("RRR-RRR-RRR"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(RATS);
				list.add(REVERSE);
				list.add(ROGUE);
			}

			@Override
			public Image getIcon() {
				Image image = new Image(Assets.Interfaces.BANNERS, 0, 103, 40, 52);
				image.scale.set(1/2.5f);
				return image;
			}
		},
		MAGE("AMA-GEN-ERF"){
			@Override
			public Image getIcon() {
				return new ItemSprite().view(new MagesStaff());
			}
		},
		WARRIOR("COL-LOS-EUM"){
			@Override
			public Image getIcon() {
				return new HeroIcon(new WarriorParry());
			}
		},
		HUNTRESS("BUF-HUN-TRS"){
			@Override
			public Image getIcon() {
				Image sprite = new ItemSprite(ItemSpriteSheet.SPIRIT_BOW);
				sprite.hardlight(0x444444);
				return sprite;
			}
		},
		DUELIST("BET-TER-WEP"){
			@Override
			public Image getIcon() {
				ItemSprite sprite = new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER);
				sprite.glow(new ItemSprite.Glowing());
				return sprite;
			}
		},
		CLERIC("GOD-CHI-LDD"){
			@Override
			public Image getIcon() {
				ItemSprite sprite = new ItemSprite(ItemSpriteSheet.ARTIFACT_TOME);
				sprite.glow(new ItemSprite.Glowing(0xFFFF00, 0.85f));
				return sprite;
			}
		},
		RANDOM_TALENTS("RNG-SKI-LLS"){
			@Override
			public Image getIcon() {
				ItemSprite sprite = new ItemSprite(ItemSpriteSheet.MASTERY);
				sprite.glow(new ItemSprite.Glowing());
				return sprite;
			}
		},
		RANDOM_HERO("ROG-UEL-IKE"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(ROGUE);
				list.add(MAGE);
				list.add(DUELIST);
				list.add(RANDOM_TALENTS);
			}

			@Override
			public Image getIcon() {
				ItemSprite sprite = new ItemSprite(ItemSpriteSheet.UNSTABLE_SPELL);
				sprite.hardlight(0x2be538);
				return sprite;
			}
		},
		LEVELLING_DOWN("LEV-ELD-OWN"){
			@Override
			public Image getIcon() {
				Image sprite = Icons.get(Icons.TALENT);
				sprite.hardlight(0xFF0000);
				return sprite;
			}
		},
		ALL_TALENTS("ULT-IMA-TEE"){
			@Override
			public Image getIcon() {
				Image sprite = new BuffIcon(BuffIndicator.INVISIBLE, true);
				sprite.hardlight(0xfa00ff);
				return sprite;
			}
		},
		ALL_CLASSES("SUP-ERM-ANN"){
			@Override
			public Image getIcon() {
				return new TalentIcon(Talent.KINGS_WISDOM);
			}
		},
		ALL_SUBS("IRO-NMA-NNN"){
			@Override
			public Image getIcon() {
				ItemSprite sprite = new ItemSprite(ItemSpriteSheet.MASK);
				sprite.glow(new ItemSprite.Glowing());
				return sprite;
			}
		},
		ALL_POWERS("ASC-END-ANT"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(ALL_CLASSES);
				list.add(ALL_TALENTS);
				list.add(ALL_SUBS);
			}

			@Override
			public Image getIcon() {
				ItemSprite sprite = new ItemSprite(ItemSpriteSheet.AMULET);
				sprite.glow(new ItemSprite.Glowing(0.1f));
				return sprite;
			}
		},
		EASY_MODE("FUN-FUN-FUN"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(ECH);
				list.add(ALLIES);
				list.add(ENCHANTED_WORLD);
				list.add(NO_WARP);
			}

			@Override
			public Image getIcon() {
				return new HeroIcon(new PowerOfMany());
			}
		},
		BALANCE("VBA-LAN-CED"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(WARRIOR);
				list.add(MAGE);
				list.add(ROGUE);
				list.add(HUNTRESS);
			}

			@Override
			public Image getIcon() {
				return new HeroIcon(HeroSubClass.KING);
			}
		};

		public long seed;
		public String fullSeed;
		public boolean random;

		SpecialSeed(long seed) {
			this.seed = seed;
			this.random = true;
		}

		SpecialSeed(long seed, boolean random) {
			this.seed = seed;
			this.random = random;
		}

		SpecialSeed(String seed){
			this.seed = convertFromText(seed);
			this.random = true;
			this.fullSeed = seed;
		}

		SpecialSeed(String seed, boolean random){
			this.seed = convertFromText(seed);
			this.random = random;
			this.fullSeed = seed;
		}

		public void addSeeds(HashSet<SpecialSeed> list){
			list.add(this);
		}

		public Image getIcon(){
			return new ItemSprite(ItemSpriteSheet.SEED_PAGE);
		}

		public static void interpret(HashSet<SpecialSeed> list, String seed){
			long s = convertFromText(seed);
			for (SpecialSeed specialSeed : SpecialSeed.values()){
				if (s == specialSeed.seed) {
					specialSeed.addSeeds(list);
				}
			}
        }

		static final HashMap<String, String> conversions = new HashMap<>();

		public static String convert(String legacyName){
			if (conversions.containsKey(legacyName))
				return conversions.get(legacyName);
			return legacyName;
		}
	}

}
