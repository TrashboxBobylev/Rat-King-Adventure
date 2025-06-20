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

package com.zrp200.rkpd2.items.potions.exotic;

import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.Recipe;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.items.potions.PotionOfExperience;
import com.zrp200.rkpd2.items.potions.PotionOfFrost;
import com.zrp200.rkpd2.items.potions.PotionOfHaste;
import com.zrp200.rkpd2.items.potions.PotionOfHealing;
import com.zrp200.rkpd2.items.potions.PotionOfInvisibility;
import com.zrp200.rkpd2.items.potions.PotionOfLevitation;
import com.zrp200.rkpd2.items.potions.PotionOfLiquidFlame;
import com.zrp200.rkpd2.items.potions.PotionOfMindVision;
import com.zrp200.rkpd2.items.potions.PotionOfParalyticGas;
import com.zrp200.rkpd2.items.potions.PotionOfPurity;
import com.zrp200.rkpd2.items.potions.PotionOfStrength;
import com.zrp200.rkpd2.items.potions.PotionOfToxicGas;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ExoticPotion extends Potion {
	
	{
		//sprite = equivalent potion sprite but one row down
	}
	
	public static final LinkedHashMap<Class<?extends Potion>, Class<?extends ExoticPotion>> regToExo = new LinkedHashMap<>();
	public static final LinkedHashMap<Class<?extends ExoticPotion>, Class<?extends Potion>> exoToReg = new LinkedHashMap<>();
	static{
		regToExo.put(PotionOfStrength.class, PotionOfMastery.class);
		exoToReg.put(PotionOfMastery.class, PotionOfStrength.class);

		regToExo.put(PotionOfHealing.class, PotionOfShielding.class);
		exoToReg.put(PotionOfShielding.class, PotionOfHealing.class);

		regToExo.put(PotionOfMindVision.class, PotionOfMagicalSight.class);
		exoToReg.put(PotionOfMagicalSight.class, PotionOfMindVision.class);

		regToExo.put(PotionOfFrost.class, PotionOfSnapFreeze.class);
		exoToReg.put(PotionOfSnapFreeze.class, PotionOfFrost.class);

		regToExo.put(PotionOfLiquidFlame.class, PotionOfDragonsBreath.class);
		exoToReg.put(PotionOfDragonsBreath.class, PotionOfLiquidFlame.class);

		regToExo.put(PotionOfToxicGas.class, PotionOfCorrosiveGas.class);
		exoToReg.put(PotionOfCorrosiveGas.class, PotionOfToxicGas.class);

		regToExo.put(PotionOfHaste.class, PotionOfStamina.class);
		exoToReg.put(PotionOfStamina.class, PotionOfHaste.class);

		regToExo.put(PotionOfInvisibility.class, PotionOfShroudingFog.class);
		exoToReg.put(PotionOfShroudingFog.class, PotionOfInvisibility.class);
		
		regToExo.put(PotionOfLevitation.class, PotionOfStormClouds.class);
		exoToReg.put(PotionOfStormClouds.class, PotionOfLevitation.class);

		regToExo.put(PotionOfParalyticGas.class, PotionOfEarthenArmor.class);
		exoToReg.put(PotionOfEarthenArmor.class, PotionOfParalyticGas.class);

		regToExo.put(PotionOfPurity.class, PotionOfCleansing.class);
		exoToReg.put(PotionOfCleansing.class, PotionOfPurity.class);

		regToExo.put(PotionOfExperience.class, PotionOfDivineInspiration.class);
		exoToReg.put(PotionOfDivineInspiration.class, PotionOfExperience.class);

	}
	
	@Override
	public boolean isKnown() {
		return anonymous || (handler != null && handler.isKnown( exoToReg.get(this.getClass()) ));
	}
	
	@Override
	public void setKnown() {
		if (!isKnown()) {
			handler.know(exoToReg.get(this.getClass()));
			updateQuickslot();
		}
	}
	
	@Override
	public void reset() {
		super.reset();
		if (handler != null && handler.contains(exoToReg.get(this.getClass()))) {
			image = handler.image(exoToReg.get(this.getClass())) + 16;
			color = handler.label(exoToReg.get(this.getClass()));
		}
	}
	
	@Override
	//20 gold more than its none-exotic equivalent
	public int value() {
		return (Reflection.newInstance(exoToReg.get(getClass())).value() + 20) * quantity;
	}

	@Override
	//4 more energy than its none-exotic equivalent
	public int energyVal() {
		return (Reflection.newInstance(exoToReg.get(getClass())).energyVal() + 4) * quantity;
	}

	public static class PotionToExotic extends Recipe{

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients.size() == 1 && regToExo.containsKey(ingredients.get(0).getClass())){
				return true;
			}

			return false;
		}
		
		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 4;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			for (Item i : ingredients){
				i.quantity(i.quantity()-1);
			}

			return Reflection.newInstance(regToExo.get(ingredients.get(0).getClass()));
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			return Reflection.newInstance(regToExo.get(ingredients.get(0).getClass()));
		}
	}
}
