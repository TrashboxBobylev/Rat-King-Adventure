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

import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;

import static com.zrp200.rkpd2.Dungeon.hero;
import static com.zrp200.rkpd2.actors.hero.Talent.INSCRIBED_POWER;

public class ScrollEmpower extends Buff {

	{
		type = buffType.POSITIVE;
	}

	public static int boost() {
		// 2 for restoration, 2/4 for inscribed power. +1 inscribed power is the same as +2 shattered's.
		return 2 + hero.pointsInTalent(INSCRIBED_POWER)*2;
	}


	private int left;

	public void reset(int left){
		this.left = left;
		Item.updateQuickslot();
	}

	public void use(){
		left--;
		if (left <= 0){
			detach();
		}
	}

	@Override
	public void detach() {
		super.detach();
		Item.updateQuickslot();
	}

	@Override
	public int icon() {
		return BuffIndicator.UPGRADE;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.84f, 0.79f, 0.65f); //scroll colors
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (3f - left) / 3f);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(left);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", hero.heroClass.title(), boost(), left);
	}

	private static final String LEFT = "left";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, left);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		left = bundle.getInt(LEFT);
	}
}
