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
import com.watabou.utils.Random;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;

import static com.zrp200.rkpd2.Dungeon.hero;

public class SoulMark extends FlavourBuff {

	public static final float DURATION	= 10f;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	// delayed skips the first proc.
	private boolean delayed = false;

	// fixme do I really need this?
	@Override public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put("delayed", delayed);
	}
	@Override public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if(bundle.contains("duration")) {
			spend( bundle.getFloat("duration") );
			delayed = true;
		} else delayed = bundle.getBoolean("delayed");
	}

	public static void process(Char defender, int level, int chargesUsed, boolean afterDamage) {
		// increased level to imitate mage's innate wand boost when applicable.
		if(hero.isSubclassedLoosely(HeroSubClass.WARLOCK) && !hero.heroClass.isExact(HeroClass.MAGE)) level += 2;

		//standard 1 - 0.92^x chance, plus 7%. Starts at 15%
		process(defender, level, 1f - (float)(Math.pow(0.92f, (level * chargesUsed) + 1) - 0.07f), afterDamage, true);
	}

	public static void process(Char defender, int bonusDuration, float chance, boolean afterDamage, boolean extend) {
		if (defender != hero
				&& hero.subClass.is(HeroSubClass.WARLOCK)
				&& Random.Float() < chance) {
			float duration = DURATION + bonusDuration;
			(extend ? affect(defender, SoulMark.class, duration)
					: prolong(defender, SoulMark.class, duration)
			).delayed = afterDamage;
			// see Char#damage
		}
	}

	public void proc(Object src, Char defender, int damage) {
		if(delayed) {
			delayed = false;
			return;
		}
		if(!( src instanceof Char || hero.hasTalent(Talent.SOUL_SIPHON) )) return; // this shouldn't come up, but in case it does...
		int restoration = Math.min(damage, defender.HP);
		//physical damage that doesn't come from the hero is less effective
		if (src != hero){
			// wand damage is handled prior this method's calling.
			int points = hero.pointsInTalent(Talent.SOUL_SIPHON,Talent.RK_WARLOCK);
			restoration = Math.round(restoration * (
					hero.hasTalent(Talent.SOUL_SIPHON) ?
							// can't use Hero#byTalent here because of the shift.
							src instanceof Char ? points*.2f : .1f*(1+points)
							: points / 7.5f ));
		}
		if(restoration > 0)
		{
			Buff.affect(hero, Hunger.class)
					.affectHunger( restoration*hero.byTalent(Talent.SOUL_EATER,1/2f,Talent.RK_WARLOCK,1/3f) );
			hero.HP = (int) Math.ceil(Math.min(hero.HT, hero.HP + restoration * 0.4f));
			hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.INVERT_MARK;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.5f, 0.2f, 1f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - visualcooldown()) / DURATION);
	}

	@Override
	public void fx(boolean on) {
		if (on) target.sprite.add(CharSprite.State.MARKED);
		else target.sprite.remove(CharSprite.State.MARKED);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", hero.subClass.title(), dispTurns());
	}
}
