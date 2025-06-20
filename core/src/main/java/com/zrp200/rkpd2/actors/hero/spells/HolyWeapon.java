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

import static com.zrp200.rkpd2.Dungeon.hero;

import com.watabou.noosa.Image;
import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.HeroSubClass;
import com.zrp200.rkpd2.effects.Enchanting;
import com.zrp200.rkpd2.items.Item;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.weapon.Weapon;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class HolyWeapon extends ClericSpell {

	public static final HolyWeapon INSTANCE = new HolyWeapon();

	@Override
	public int icon() {
		return HeroIcon.HOLY_WEAPON;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo actual icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2;
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		PaladinSpellExtendable.virtualAffect(hero, HolyWepBuff.class,
				SpellEmpower.isActive() ? HolyWepBuff.Empowered.class : HolyWepBuff.class
		);
		Item.updateQuickslot();

		Sample.INSTANCE.play(Assets.Sounds.READ);

		hero.spend( 1f );
		hero.busy();
		hero.sprite.operate(hero.pos);
		if (hero.belongings.weapon() != null) Enchanting.show(hero, hero.belongings.weapon());

		onSpellCast(tome, hero);
	}

	@Override
	public String desc(){
		String desc = checkEmpowerMsg("desc", (
				SpellEmpower.isActive() ? new HolyWepBuff.Empowered() : new HolyWepBuff()
		).getDamage(), new HolyWepBuff.Empowered());
		if (hero.subClass == HeroSubClass.PALADIN){
			desc += "\n\n" + checkEmpowerMsg("desc_paladin");
		}
		return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
	}
	public static class HolyWepBuff extends PaladinSpellExtendable {

		protected float getEffectiveness() {
			float effectiveness = Weapon.Enchantment.genericProcChanceMultiplier(hero);
			if (hero.subClass.is(HeroSubClass.PALADIN)) return effectiveness * 3; // 6
			else if (hero.heroClass == HeroClass.CLERIC) return effectiveness * 1.5f; // 3
			else return effectiveness;
		}

		public int getDamage() { return Math.round(2 * getEffectiveness()); }

		public void proc(Char attacker, Char defender) {
			// todo probably should give allied attacks and thrown weapons different numbers.
			defender.damage(getDamage(), HolyWeapon.INSTANCE);
			// added effect for fun
			defender.sprite.burst(0xFFFFFFFF, Random.round(getEffectiveness() / 1.5f));
		}

		@Override
		public float getDuration() { return 60; }
		@Override
		public float getTurnsPerCharge() { return 10; }

		@Override
		public ClericSpell getSourceSpell() { return INSTANCE; }

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HOLY_WEAPON;
		}

		@Override
		public String desc() {
			String desc;
			if (hero.subClass.is(HeroSubClass.PALADIN)) {
				desc = Messages.get(this, "desc_paladin", getDamage()) + "\n\n" + getExtendableMessage();
			} else {
				desc = Messages.get(this, "desc", getDamage());
			}
			return desc + "\n\n" + Messages.get(this, "turns", dispTurns());
		}

		@Override
		public void detach() {
			super.detach();
			Item.updateQuickslot();
		}

		public static class Empowered extends HolyWepBuff {
			@Override
			public float getDuration() { return super.getDuration() * 2; }
			@Override
			public float getTurnsPerCharge() { return 15; }
			@Override
			public void tintIcon(Image icon) { icon.tint(0, .33f); }

			@Override
			public float getEffectiveness() {
				// does 9 for paladin, 6 otherwise
				return super.getEffectiveness() * (
						hero.subClass.is(HeroSubClass.PALADIN) ? 1.5f : 2
				);
			}
		}
	}

}
