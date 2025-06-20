/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.LifeLink;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.Beam;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.tiles.DungeonTilemap;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;

public class LifeLinkSpell extends ClericSpell {

	public static LifeLinkSpell INSTANCE = new LifeLinkSpell();

	@Override
	public int icon() {
		return HeroIcon.LIFE_LINK;
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", 4 + 2*Dungeon.hero.pointsInTalent(Talent.LIFE_LINK), 30 + 5*Dungeon.hero.pointsInTalent(Talent.LIFE_LINK)) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero)
				&& hero.hasTalent(Talent.LIFE_LINK)
				&& (PowerOfMany.getPoweredAlly() != null || Stasis.getStasisAlly() != null);
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		int duration = 4 + 2*hero.pointsInTalent(Talent.LIFE_LINK);

		Char ally = PowerOfMany.getPoweredAlly();

		if (ally != null) {
			hero.sprite.zap(ally.pos);
			hero.sprite.parent.add(
					new Beam.HealthRay(hero.sprite.center(), ally.sprite.center()));

			Buff.prolong(hero, LifeLink.class, duration).object = ally.id();
		} else {
			ally = Stasis.getStasisAlly();
			hero.sprite.operate(hero.pos);
			hero.sprite.parent.add(
					new Beam.HealthRay(DungeonTilemap.tileCenterToWorld(hero.pos), hero.sprite.center()));
		}

		Buff.prolong(ally, LifeLink.class, duration).object = hero.id();
		Buff.prolong(ally, LifeLinkSpellBuff.class, duration);

		if (ally == Stasis.getStasisAlly()){
			ally.buff(LifeLink.class).clearTime();
			ally.buff(LifeLinkSpellBuff.class).clearTime();
		}

		if (SpellEmpower.isActive()) {
			GLog.p(Messages.get(SpellEmpower.class, "instant"));
			hero.next();
		} else {
			hero.spendAndNext(Actor.TICK);
		}

		onSpellCast(tome, hero);

	}

	public static class LifeLinkSpellBuff extends FlavourBuff{

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HOLY_ARMOR;
		}

		@Override
		public float iconFadePercent() {
			int duration = 4 + 2*Dungeon.hero.pointsInTalent(Talent.LIFE_LINK);
			return Math.max(0, (duration - visualcooldown()) / duration);
		}
	}
}
