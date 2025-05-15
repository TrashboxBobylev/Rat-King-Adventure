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
		RATS("RAT-RAT-RAT"),
		ROGUE("ROG-UEB-UFF"),
		REVERSE("REV-ERS-EED"),
		NO_WALLS("NOW-ALL-SHE"),
		RANDOM_ITEMS("RNG-ITE-MSS"),
		CHESTS("ITE-MCH-EST"),
		BIGGER("EXP-ANS-IVE"),
		ALLIES("BES-TFR-END"),
		ECH("ECH-ECH-ECH"),
		EVERYTHING("BOB-PAL-KIA"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				list.addAll(Arrays.asList(SpecialSeed.values()));
			}
		},
		NO_WARP("ROT-INM-IND"),
		DUNGEONEER("WHO-LEP-ACK"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(CHESTS);
				list.add(BIGGER);
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
		},
		MAGE("AMA-GEN-ERF"),
		WARRIOR("COL-LOS-EUM"),
		HUNTRESS("BUF-HUN-TRS"),
		ALL_TALENTS("ULT-IMA-TEE"),
		ALL_CLASSES("SUP-ERM-ANN"),
		ALL_SUBS("IRO-NMA-NNN"),
		ALL_POWERS("ASC-END-ANT"){
			@Override
			public void addSeeds(HashSet<SpecialSeed> list) {
				super.addSeeds(list);
				list.add(ALL_CLASSES);
				list.add(ALL_TALENTS);
				list.add(ALL_SUBS);
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
		};

		public long seed;
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
		}

		SpecialSeed(String seed, boolean random){
			this.seed = convertFromText(seed);
			this.random = random;
		}

		public void addSeeds(HashSet<SpecialSeed> list){
			list.add(this);
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
