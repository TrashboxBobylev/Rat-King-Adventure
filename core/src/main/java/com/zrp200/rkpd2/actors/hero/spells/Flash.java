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

package com.zrp200.rkpd2.actors.hero.spells;

import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.AscendedForm;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.scrolls.ScrollOfTeleportation;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;

public class Flash extends TargetedClericSpell {

	public static Flash INSTANCE = new Flash();

	@Override
	public int icon() {
		return HeroIcon.FLASH;
	}

	@Override
	public float chargeUse(Hero hero) {
		if (hero.buff(AscendedForm.AscendBuff.class) != null){
			return 1 + hero.buff(AscendedForm.AscendBuff.class).flashCasts;
		} else {
			return 1;
		}
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero)
				&& hero.hasTalent(Talent.FLASH)
				&& hero.buff(AscendedForm.AscendBuff.class) != null;
	}

	@Override
	public int targetingFlags() {
		return -1; //targets an empty cell, not an enemy
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {

		if (target == null){
			return;
		}

		if (Dungeon.level.solid[target] || (!Dungeon.level.mapped[target] && !Dungeon.level.visited[target])
				|| Dungeon.level.distance(hero.pos, target) > 2+hero.pointsInTalent(Talent.FLASH)){
			GLog.w(Messages.get(this, "invalid_target"));
			return;
		}

		if (ScrollOfTeleportation.teleportToLocation(hero, target)){
			hero.busy();
			if (SpellEmpower.isActive()) {
				GLog.p(Messages.get(SpellEmpower.class, "instant"));
			} else {
				hero.spend( 1f );
			}
			hero.next();
			onSpellCast(tome, hero);
			hero.buff(AscendedForm.AscendBuff.class).flashCasts++;
		}

	}
}
