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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FireImbue;
import com.zrp200.rkpd2.actors.buffs.HighnessBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.effects.particles.FlameParticle;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfDragonsBreath;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

public class ElixirOfDragonsBlood extends Elixir {
	
	{
		//TODO finish visuals
		image = ItemSpriteSheet.ELIXIR_DRAGON;
	}
	
	@Override
	public void apply(Hero hero) {
		Buff.affect(hero, FireImbue.class).set(FireImbue.DURATION);
		Sample.INSTANCE.play( Assets.Sounds.BURNING );
		hero.sprite.emitter().burst(FlameParticle.FACTORY, 10);
		HighnessBuff.agreenalineProc();
	}
	
	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfDragonsBreath.class};
			inQuantity = new int[]{1};
			
			cost = 10;
			
			output = ElixirOfDragonsBlood.class;
			outQuantity = 1;
		}
		
	}
}
