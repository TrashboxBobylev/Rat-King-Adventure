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

package com.zrp200.rkpd2.items.potions.elixirs;

import com.zrp200.rkpd2.actors.buffs.ArcaneArmor;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfEarthenArmor;
import com.zrp200.rkpd2.items.quest.GooBlob;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class ElixirOfArcaneArmor extends Elixir {
	
	{
		image = ItemSpriteSheet.ELIXIR_ARCANE;
	}
	
	@Override
	public void apply(Hero hero) {
		Buff.affect(hero, ArcaneArmor.class).set(5 + hero.lvl/2, 80);
		HighnessBuff.agreenalineProc();
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (60 + 30);
	}
	
	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfEarthenArmor.class, GooBlob.class};
			inQuantity = new int[]{1, 1};
			
			cost = 8;
			
			output = ElixirOfArcaneArmor.class;
			outQuantity = 1;
		}
		
	}
}
