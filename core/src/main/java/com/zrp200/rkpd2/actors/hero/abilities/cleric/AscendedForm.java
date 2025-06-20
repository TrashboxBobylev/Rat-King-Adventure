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

package com.zrp200.rkpd2.actors.hero.abilities.cleric;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Invisibility;
import com.zrp200.rkpd2.actors.buffs.ShieldBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.actors.hero.spells.DivineIntervention;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class AscendedForm extends ArmorAbility {

	{
		baseChargeUse = 50;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {

		Buff.affect(hero, AscendBuff.class).reset();
		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
		new Flare(6, 48).color(0xFFFF00, true).show(hero.sprite, 2f);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();
		Invisibility.dispel();
		hero.spendAndNext(/*Actor.TICK*/0);

	}

	@Override
	public int icon() {
		return HeroIcon.ASCENDED_FORM;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.DIVINE_INTERVENTION, Talent.JUDGEMENT, Talent.FLASH, Talent.HEROIC_ENERGY};
	}

	public static class AscendBuff extends ShieldBuff {

		{
			type = buffType.POSITIVE;

			// why did shattered set this to true???
			detachesAtZero = false;
		}

		public static float DURATION = 10f;

		@Override
		public int icon() {
			return BuffIndicator.ASCEND;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - left) / DURATION);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)left);
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add(CharSprite.State.GLOWING);
			else    target.sprite.remove(CharSprite.State.GLOWING);
		}

		public int left = 10;
		public int spellCasts = 0;
		public int flashCasts = 0;
		public boolean divineInverventionCast = false;

		public void reset(){
			setShield(30);
			left = (int)DURATION;
		}

		public void extend( int amt ){
			left += amt;
		}

		@Override
		public boolean act() {
			left--;
			if (left <= 0){
				detach();
				for (Char ch : Actor.chars()){
					if (ch.buff(DivineIntervention.DivineShield.class) != null){
						ch.buff(DivineIntervention.DivineShield.class).detach();
					}
				}
				return true;
			}

			spend(TICK);
			return true;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", shielding(), left);
		}

		public static final String LEFT = "left";
		public static final String SPELL_CASTS = "spell_casts";
		public static final String FLASH_CASTS = "flash_casts";
		public static final String DIVINE_INTERVENTION_CAST = "divine_intervention_cast";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LEFT, left);
			bundle.put(SPELL_CASTS, spellCasts);
			bundle.put(FLASH_CASTS, flashCasts);
			bundle.put(DIVINE_INTERVENTION_CAST, divineInverventionCast);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			left = bundle.getInt(LEFT);
			spellCasts = bundle.getInt(SPELL_CASTS);
			flashCasts = bundle.getInt(FLASH_CASTS);
			divineInverventionCast = bundle.getBoolean(DIVINE_INTERVENTION_CAST);
		}
	}

}
