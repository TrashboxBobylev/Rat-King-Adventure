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

package com.zrp200.rkpd2.actors.buffs;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.weapon.missiles.MissileWeapon;
import com.zrp200.rkpd2.items.weapon.missiles.darts.Dart;
import com.zrp200.rkpd2.items.weapon.missiles.darts.TippedDart;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collection;

public class PinCushion extends Buff {

	private ArrayList<MissileWeapon> items = new ArrayList<>();

	public void stick(MissileWeapon projectile){
		for (Item item : items){
			if (item.isSimilar(projectile)){
				item.merge(projectile);
				if (TippedDart.lostDarts > 0){
					Dart d = new Dart();
					d.quantity(TippedDart.lostDarts);
					TippedDart.lostDarts = 0;
					stick(d);
				}
				return;
			}
		}
		items.add(projectile);
	}

	public Item grabOne(){
		Item item = items.remove(0);
		if (items.isEmpty()){
			detach();
		}
		return item;
	}

	public ArrayList<MissileWeapon> getStuckItems(){
		return new ArrayList<>(items);
	}

	@Override
	public void detach() {
		for (Item item : items)
			Dungeon.level.drop( item, target.pos).sprite.drop();
		super.detach();
	}

	private static final String ITEMS = "items";

	@Override
	public void storeInBundle(Bundle bundle) {
		bundle.put( ITEMS , items );
		super.storeInBundle(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		items = new ArrayList<>((Collection<MissileWeapon>) ((Collection<?>) bundle.getCollection(ITEMS)));
		super.restoreFromBundle( bundle );
	}

	@Override
	public int icon() {
		return BuffIndicator.PINCUSHION;
	}

	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		for (Item i : items){
			desc += "\n" + i.title();
		}
		return desc;
	}

}
