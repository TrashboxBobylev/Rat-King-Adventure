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
import com.zrp200.rkpd2.items.armor.Armor;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

public class HolyWard extends ClericSpell {

	public static final HolyWard INSTANCE = new HolyWard();

	@Override
	public int icon() {
		return HeroIcon.HOLY_WARD;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo actual icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		PaladinSpellExtendable.virtualAffect(hero, HolyArmBuff.class,
				SpellEmpower.isActive() ? HolyArmBuff.Empowered.class : HolyArmBuff.class);
		Item.updateQuickslot();

		Sample.INSTANCE.play(Assets.Sounds.READ);

		hero.spend( 1f );
		hero.busy();
		hero.sprite.operate(hero.pos);
		if (hero.belongings.armor() != null) Enchanting.show(hero, hero.belongings.armor());

		onSpellCast(tome, hero);
	}

	@Override
	public String desc(){
		String desc = checkEmpowerMsg("desc");
		if (hero.subClass == HeroSubClass.PALADIN){
			desc += "\n\n" + Messages.get(this, "desc_paladin");
		}
		return desc + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(hero));
	}

	public static class HolyArmBuff extends PaladinSpellExtendable {

		public float getDuration() { return 60; }
		public float getTurnsPerCharge() { return 10; }

		public ClericSpell getSourceSpell() { return INSTANCE; }

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HOLY_ARMOR;
		}

		@Override
		public String desc() {
			String desc;
			if (hero.subClass == HeroSubClass.PALADIN) {
				desc = Messages.get(this, "desc_paladin") + "\n\n" + getExtendableMessage();
			} else {
				desc = Messages.get(this, "desc");
			}
			return desc + "\n\n" + Messages.get(this, "turns", dispTurns());
		}

		@Override
		public void detach() {
			super.detach();
			Item.updateQuickslot();
		}

		public static class Empowered extends HolyArmBuff {
			@Override
			public float getDuration() { return super.getDuration() * 2; }
			@Override
			public float getTurnsPerCharge() { return 15; }
			@Override
			public void tintIcon(Image icon) { icon.tint(0, .33f); }
		}
	}

	public static int proc(Char defender) {
		HolyArmBuff buff = hero.virtualBuff(HolyArmBuff.class);
		if (buff == null || !(
				defender == hero
						|| buff instanceof HolyArmBuff.Empowered
						|| AuraOfProtection.isActiveFor(defender)
		)) return 0;
		return Random.round(Armor.Glyph.genericProcChanceMultiplier(defender)
				* effectiveness(buff instanceof HolyArmBuff.Empowered)
		);
	}

	public static float effectiveness(boolean empowered) {
		// since it interacts with more stuff, the descriptions aren't updated to show the actual value
		return hero.subClass.is(HeroSubClass.PALADIN) ?
				empowered ? 4.5f : 3 :
				empowered ? 3 :
				hero.heroClass == HeroClass.CLERIC ? 1.5f :
						1;
	}

}
