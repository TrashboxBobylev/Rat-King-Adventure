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

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.ShieldBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.AscendedForm;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.zrp200.rkpd2.utils.GLog;

public class DivineIntervention extends ClericSpell {

	public static DivineIntervention INSTANCE = new DivineIntervention();

	@Override
	public int icon() {
		return HeroIcon.DIVINE_INTERVENTION;
	}

	@Override
	public float chargeUse(Hero hero) {
		return 5;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero)
				&& hero.hasTalent(Talent.DIVINE_INTERVENTION)
				&& hero.buff(AscendedForm.AscendBuff.class) != null
				&& !hero.buff(AscendedForm.AscendBuff.class).divineInverventionCast;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		Sample.INSTANCE.play(Assets.Sounds.CHARGEUP, 1, 1.2f);
		hero.sprite.operate(hero.pos);

		for (Char ch : Actor.chars()){
			if (ch.alignment == Char.Alignment.ALLY && ch != hero){
				Buff.affect(ch, DivineShield.class).setShield(100 + 50*hero.pointsInTalent(Talent.DIVINE_INTERVENTION));
				new Flare(6, 32).color(0xFFFF00, true).show(ch.sprite, 2f);
			}
		}

		if (SpellEmpower.isActive()) {
			GLog.p(Messages.get(SpellEmpower.class, "instant"));
		} else {
			hero.spend( 1f );
		}
		hero.next();
		onSpellCast(tome, hero);

		//we apply buffs here so that the 5 charge cost and shield boost do not stack
		hero.buff(AscendedForm.AscendBuff.class).setShield(100 + 50*hero.pointsInTalent(Talent.DIVINE_INTERVENTION));
		new Flare(6, 32).color(0xFFFF00, true).show(hero.sprite, 2f);

		hero.buff(AscendedForm.AscendBuff.class).divineInverventionCast = true;
		hero.buff(AscendedForm.AscendBuff.class).extend(hero.pointsInTalent(Talent.DIVINE_INTERVENTION));

	}

	@Override
	public String desc() {
		int shield = 100 + 50*Dungeon.hero.pointsInTalent(Talent.DIVINE_INTERVENTION);
		int leftBonus = Dungeon.hero.pointsInTalent(Talent.DIVINE_INTERVENTION);
		return Messages.get(this, "desc", shield, leftBonus) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	public static class DivineShield extends ShieldBuff{

		@Override
		public boolean act() {

			if (Dungeon.hero == null || Dungeon.hero.buff(AscendedForm.AscendBuff.class) == null){
				detach();
			}

			spend(TICK);
			return true;
		}

		@Override
		public int shielding() {
			if (Dungeon.hero == null || Dungeon.hero.buff(AscendedForm.AscendBuff.class) == null){
				return 0;
			}
			return super.shielding();
		}

		@Override
		public void fx(boolean on) {
			if (on) target.sprite.add(CharSprite.State.SHIELDED);
			else    target.sprite.remove(CharSprite.State.SHIELDED);
		}
	}
}
