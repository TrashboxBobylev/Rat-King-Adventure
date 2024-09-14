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
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;

import static com.zrp200.rkpd2.Dungeon.hero;

public class EnhancedRings extends FlavourBuff {

	{
		type = Buff.buffType.POSITIVE;
	}

	@Override
	public boolean attachTo(Char target) {
		if (super.attachTo(target)){
			if (target instanceof Hero) ((Hero) target).updateHT(false);
			return true;
		}
		return false;
	}

	@Override
	public void detach() {
		super.detach();
		if (target instanceof Hero) ((Hero) target).updateHT(false);
	}

	@Override
	public int icon() {
		return BuffIndicator.UPGRADE;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0, 1, 0);
	}

	@Override
	public float iconFadePercent() {
		float max = 3* hero.pointsInTalent(Talent.ENHANCED_RINGS,Talent.RK_ASSASSIN);
		return Math.max(0, (max-visualcooldown()) / max);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc",
				Messages.titleCase(hero.heroClass.title()),
				hero.heroClass.is(HeroClass.HUNTRESS) ? "her" : "his",
				(int)visualcooldown());
	}
}
