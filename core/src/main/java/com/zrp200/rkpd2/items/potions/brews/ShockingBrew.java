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

package com.zrp200.rkpd2.items.potions.brews;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.blobs.Blob;
import com.zrp200.rkpd2.actors.blobs.Electricity;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.items.potions.AlchemicalCatalyst;
import com.zrp200.rkpd2.items.potions.PotionOfParalyticGas;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;

public class ShockingBrew extends Brew {
	
	{
		image = ItemSpriteSheet.BREW_SHOCKING;
	}
	
	@Override
	public void shatter(int cell) {
		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
			Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);
		}
		PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), 3 );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				GameScene.add(Blob.seed(i, 20 + Dungeon.hero.pointsInTalent(Talent.FARADAY_CAGE)*2, Electricity.class));
			}
		}
	}

	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfParalyticGas.class};
			inQuantity = new int[]{1};
			
			cost = 10;
			
			output = ShockingBrew.class;
			outQuantity = 1;
		}
		
	}
}
