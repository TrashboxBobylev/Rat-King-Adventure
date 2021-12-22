/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2021 Evan Debenham
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

package com.zrp200.rkpd2.items.potions;

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.exotic.ExoticPotion;
import com.zrp200.rkpd2.items.stones.Runestone;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;

public class AlchemicalCatalyst extends Potion {
	
	{
		image = ItemSpriteSheet.POTION_CATALYST;
		
	}
	
	private static HashMap<Class<? extends Potion>, Float> potionChances = new HashMap<>();
	static{
		potionChances.put(PotionOfHealing.class,        3f);
		potionChances.put(PotionOfMindVision.class,     2f);
		potionChances.put(PotionOfFrost.class,          2f);
		potionChances.put(PotionOfLiquidFlame.class,    2f);
		potionChances.put(PotionOfToxicGas.class,       2f);
		potionChances.put(PotionOfHaste.class,          2f);
		potionChances.put(PotionOfInvisibility.class,   2f);
		potionChances.put(PotionOfLevitation.class,     2f);
		potionChances.put(PotionOfParalyticGas.class,   2f);
		potionChances.put(PotionOfPurity.class,         2f);
		potionChances.put(PotionOfExperience.class,     1f);
	}
	
	@Override
	public void apply(Hero hero) {
		Potion p = Reflection.newInstance(Random.chances(potionChances));
		while (Dungeon.isChallenged(Challenges.NO_HEALING) && p instanceof PotionOfHealing){
			p = Reflection.newInstance(Random.chances(potionChances));
		}
		p.anonymize();
		p.apply(hero);
	}
	
	@Override
	public void shatter(int cell) {
		Potion p = Reflection.newInstance(Random.chances(potionChances));
		p.anonymize();
		curItem = p;
		p.shatter(cell);
	}
	
	@Override
	public boolean isKnown() {
		return true;
	}
	
	@Override
	public int value() {
		return 40 * quantity;
}

	@Override
	public int energyVal() {
		return 8 * quantity;
	}

	public static class Recipe extends com.zrp200.rkpd2.items.Recipe {
		
		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			boolean potion = false;
			boolean secondary = false;
			
			for (Item i : ingredients){
				if (i instanceof Plant.Seed || i instanceof Runestone){
					secondary = true;
				//if it is a regular or exotic potion
				} else if (ExoticPotion.regToExo.containsKey(i.getClass())
						|| ExoticPotion.regToExo.containsValue(i.getClass())) {
					potion = true;
				}
			}
			
			return potion && secondary;
		}
		
		@Override
		public int cost(ArrayList<Item> ingredients) {
			for (Item i : ingredients){
				if (i instanceof Plant.Seed){
					return 0;
				} else if (i instanceof Runestone){
					return 1;
				}
			}
			return 0;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			
			for (Item i : ingredients){
				i.quantity(i.quantity()-1);
			}
			
			return sampleOutput(null);
		}
		
		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return new AlchemicalCatalyst();
		}
	}
	
}
