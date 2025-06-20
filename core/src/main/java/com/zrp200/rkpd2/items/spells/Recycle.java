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

package com.zrp200.rkpd2.items.spells;

import com.zrp200.rkpd2.Challenges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.items.Generator;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.potions.Potion;
import com.zrp200.rkpd2.items.potions.brews.Brew;
import com.zrp200.rkpd2.items.potions.elixirs.Elixir;
import com.zrp200.rkpd2.items.potions.exotic.ExoticPotion;
import com.zrp200.rkpd2.items.scrolls.Scroll;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTransmutation;
import com.zrp200.rkpd2.items.scrolls.exotic.ExoticScroll;
import com.zrp200.rkpd2.items.stones.Runestone;
import com.zrp200.rkpd2.items.weapon.missiles.darts.TippedDart;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.plants.Plant;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Reflection;

public class Recycle extends InventorySpell {
	
	{
		image = ItemSpriteSheet.RECYCLE;

		talentFactor = 2;
		talentChance = 1/(float)Recipe.OUT_QUANTITY;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return (item instanceof Potion && !(item instanceof Elixir || item instanceof Brew)) ||
				item instanceof Scroll ||
				item instanceof Plant.Seed ||
				item instanceof Runestone ||
				item instanceof TippedDart;
	}

	@Override
	protected void onItemSelected(Item item) {
		Item result;
		do {
			if (item instanceof Potion) {
				result = Generator.randomUsingDefaults(Generator.Category.POTION);
				if (item instanceof ExoticPotion){
					result = Reflection.newInstance(ExoticPotion.regToExo.get(result.getClass()));
				}
			} else if (item instanceof Scroll) {
				result = Generator.randomUsingDefaults(Generator.Category.SCROLL);
				if (item instanceof ExoticScroll){
					result = Reflection.newInstance(ExoticScroll.regToExo.get(result.getClass()));
				}
			} else if (item instanceof Plant.Seed) {
				result = Generator.randomUsingDefaults(Generator.Category.SEED);
			} else if (item instanceof Runestone) {
				result = Generator.randomUsingDefaults(Generator.Category.STONE);
			} else {
				result = TippedDart.randomTipped(1);
			}
		} while (result.getClass() == item.getClass() || Challenges.isItemBlocked(result));
		
		item.detach(curUser.belongings.backpack);
		GLog.p(Messages.get(this, "recycled", result.name()));
		if (!result.collect()){
			Dungeon.level.drop(result, curUser.pos).sprite.drop();
		}
		//TODO visuals
	}
	
	@Override
	public int value() {
		return (int)(60 * (quantity/(float)Recipe.OUT_QUANTITY));
	}

	@Override
	public int energyVal() {
		return (int)(12 * (quantity/(float)Recipe.OUT_QUANTITY));
	}
	
	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {

		private static final int OUT_QUANTITY = 12;
		
		{
			inputs =  new Class[]{ScrollOfTransmutation.class};
			inQuantity = new int[]{1};
			
			cost = 12;
			
			output = Recycle.class;
			outQuantity = OUT_QUANTITY;
		}
		
	}
}
