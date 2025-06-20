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

import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Adrenaline extends FlavourBuff {
	
	{
		type = buffType.POSITIVE;
		
		announced = true;
	}
	
	public static final float DURATION	= 10f;
	
	@Override
	public int icon() {
		return BuffIndicator.RAGE;
	}


	// I wonder if this can be generalized?
	private float maxSpend = cooldown();
	@Override
	protected void spendConstant(float time) {
		if (time > maxSpend) maxSpend = time;
		super.spendConstant(time);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, 1 - visualcooldown() / (1 + maxSpend));
	}

	private static final String VISUAL_DURATION = "duration";

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		maxSpend = bundle.contains(VISUAL_DURATION) ? bundle.getFloat(VISUAL_DURATION) : cooldown();
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VISUAL_DURATION, maxSpend);
	}
	
}
