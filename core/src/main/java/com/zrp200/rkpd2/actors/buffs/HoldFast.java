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

import static com.zrp200.rkpd2.Dungeon.hero;

import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class HoldFast extends Buff {

	{
		type = buffType.POSITIVE;
	}

	public int pos = -1;

	@Override
	public boolean act() {
		if (pos != target.pos) {
			detach();
		} else {
			spend(TICK);
		}
		return true;
	}

	public int armorBonus(){
		if (pos == target.pos && target instanceof Hero){
			return Random.NormalIntRange(armorMin(), armorMax());
		} else {
			detach();
			return 0;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.ARMOR;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1.9f, 2.4f, 3.25f);
	}

	// 1-2 / 2-4 / 3-6 / 4-8
	public static int armorMin() {
		return hero.shiftedPoints(Talent.HOLD_FAST, Talent.RK_BERSERKER);
	}
    public static int armorMax() {
        return 2 * hero.shiftedPoints(Talent.HOLD_FAST, Talent.RK_BERSERKER);
    }

	public static int minArmor(){
		return Dungeon.hero.pointsInTalent(Talent.HOLD_FAST);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", hero.subClass.title(), armorMin(), armorMax());
	}

	private static final String POS = "pos";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(POS, pos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		pos = bundle.getInt(POS);
	}
}
