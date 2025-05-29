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

package com.zrp200.rkpd2.items.scrolls;

import com.zrp200.rkpd2.Badges;
import com.zrp200.rkpd2.effects.Identification;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.rings.Ring;
import com.zrp200.rkpd2.items.trinkets.ShardOfOblivion;
import com.zrp200.rkpd2.items.wands.Wand;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.utils.GLog;

public class ScrollOfIdentify extends InventoryScroll {

	{
		icon = ItemSpriteSheet.Icons.SCROLL_IDENTIFY;

		bones = true;
	}

	@Override
	protected boolean usableOnItem(Item item) {
		return !item.isIdentified();
	}

	@Override
	public void onItemSelected( Item item ) {
		
		curUser.sprite.parent.add( new Identification( curUser.sprite.center().offset( 0, -16 ) ) );

		IDItem(item);
	}

	public static void IDItem( Item item ){
		if (ShardOfOblivion.passiveIDDisabled()) {
			if (item instanceof Weapon){
				((Weapon) item).setIDReady();
				GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), item.name());
				return;
			} else if (item instanceof Armor){
				((Armor) item).setIDReady();
				GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), item.name());
				return;
			} else if (item instanceof Ring){
				((Ring) item).setIDReady();
				GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), item.name());
				return;
			} else if (item instanceof Wand){
				((Wand) item).setIDReady();
				GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready"), item.name());
				return;
			}
		}

		item.identify();
		GLog.i(Messages.get(ScrollOfIdentify.class, "it_is", item.title()));
		Badges.validateItemLevelAquired( item );
	}
	
	@Override
	public int value() {
		return isKnown() ? 30 * quantity : super.value();
	}
}
