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

import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FrostImbue;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.particles.SnowParticle;
import com.zrp200.rkpd2.items.potions.AlchemicalCatalyst;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfSnapFreeze;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;

public class ElixirOfIcyTouch extends Elixir {
	
	{
		//TODO finish visuals
		image = ItemSpriteSheet.ELIXIR_ICY;
	}
	
	@Override
	public void apply(Hero hero) {
		Buff.affect(hero, FrostImbue.class, FrostImbue.DURATION);
		hero.sprite.emitter().burst(SnowParticle.FACTORY, 5);
		HighnessBuff.agreenalineProc();
	}
	
	@Override
	protected int splashColor() {
		return 0xFF18C3E6;
	}
	
	@Override
	public int value() {
		//prices of ingredients
		return quantity * (50 + 40);
	}
	
	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfSnapFreeze.class, AlchemicalCatalyst.class};
			inQuantity = new int[]{1, 1};
			
			cost = 6;
			
			output = ElixirOfIcyTouch.class;
			outQuantity = 1;
		}
		
	}
}
