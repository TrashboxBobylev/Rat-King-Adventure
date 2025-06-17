/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.effects.CellEmitter;
import com.zrp200.rkpd2.effects.particles.KromerParticle;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.quest.Kromer;
import com.zrp200.rkpd2.items.scrolls.ScrollOfRemoveCurse;
import com.zrp200.rkpd2.items.weapon.melee.MagesStaff;
import com.zrp200.rkpd2.items.weapon.melee.MeleeWeapon;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Pipisfusion extends InventorySpell {
	
	{
		image = ItemSpriteSheet.PIPISFUSION;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return (item instanceof MeleeWeapon);
	}

	@Override
	protected void onItemSelected(Item item) {
		
		CellEmitter.get(curUser.pos).burst(KromerParticle.FACTORY, 15);
		Sample.INSTANCE.play(Assets.Sounds.CURSED, 2f, 2f);
		
		ScrollOfRemoveCurse.uncurse(Dungeon.hero, item);
		if (item instanceof MeleeWeapon) {
			MeleeWeapon w = (MeleeWeapon) item;
			w.enchant();
			w.trollers = true;
			if (w instanceof MagesStaff){
				((MagesStaff) w).updateWand(true);
			}
		}
		Badges.validateItemLevelAquired(item);
		updateQuickslot();
	}
	
	@Override
	public int value() {
		//prices of ingredients, divided by output quantity
		return Math.round(quantity * ((27 + Random.Int(1, 672)) / 2f));
	}
	
	public static class Recipe extends com.zrp200.rkpd2.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{CurseInfusion.class, Kromer.class};
			inQuantity = new int[]{1, 1};
			
			cost = 22;
			
			output = Pipisfusion.class;
			outQuantity = 2;
		}
		
	}
}
