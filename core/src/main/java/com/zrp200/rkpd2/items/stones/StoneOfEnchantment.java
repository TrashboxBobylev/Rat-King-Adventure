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

package com.zrp200.rkpd2.items.stones;

import com.zrp200.rkpd2.actors.hero.Belongings;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.scrolls.exotic.ScrollOfEnchantment;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.journal.Catalog;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class StoneOfEnchantment extends InventoryStone {
	
	{
		preferredBag = Belongings.Backpack.class;
		image = ItemSpriteSheet.STONE_ENCHANT;

		unique = true;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return ScrollOfEnchantment.enchantable(item);
	}
	
	@Override
	protected void onItemSelected(Item item) {
		if (!anonymous) {
			curItem.detach(curUser.belongings.backpack);
			Catalog.countUse(getClass());
			Talent.onRunestoneUsed(curUser, curUser.pos, getClass());
		}
		
		if (item instanceof Weapon) {
			
			((Weapon)item).enchant();
			
		} else {
			
			((Armor)item).inscribe();
			
		}
		
		curUser.sprite.emitter().start( Speck.factory( Speck.LIGHT ), 0.1f, 5 );
		Enchanting.show( curUser, item );
		
		if (item instanceof Weapon) {
			GLog.p(Messages.get(this, "weapon"));
		} else {
			GLog.p(Messages.get(this, "armor"));
		}
		
		useAnimation();
		
	}
	
	@Override
	public int value() {
		return 30 * quantity;
	}

	@Override
	public int energyVal() {
		return 5 * quantity;
	}

}
