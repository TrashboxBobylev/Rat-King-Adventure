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
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.MindVision;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.SpellSprite;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.scenes.GameScene;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.zrp200.rkpd2.utils.GLog;

public class DivineSense extends ClericSpell {

	public static final DivineSense INSTANCE = new DivineSense();

	@Override
	public int icon() {
		return HeroIcon.DIVINE_SENSE;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo make icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && (SpellEmpower.isActive() || hero.hasTalent(Talent.DIVINE_SENSE));
	}

	public void apply(Hero hero, Char target) {
		if (SpellEmpower.isActive()) {
			// 60 / 90 / 120
			Buff.prolong(target, MindVision.class, 30 * (2 + hero.pointsInTalent(Talent.DIVINE_SENSE)));
			Buff.detach(target, DivineSenseTracker.class);
		} else {
			Buff.prolong(target, DivineSenseTracker.class, DivineSenseTracker.DURATION);
		}
	}
	public void onCast(HolyTome tome, Hero hero) {
		apply(hero, hero);
		Dungeon.observe();

		Sample.INSTANCE.play(Assets.Sounds.READ);

		hero.spend( 1f );
		hero.busy();
		SpellSprite.show(hero, SpellSprite.VISION);
		hero.sprite.operate(hero.pos);

		Char ally = PowerOfMany.getPoweredAlly();
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
			apply(hero, ally);
			SpellSprite.show(ally, SpellSprite.VISION);
		}

		onSpellCast(tome, hero);
	}

	public String desc(){
		return checkEmpowerMsg("desc", 4+4*Dungeon.hero.pointsInTalent(Talent.DIVINE_SENSE), 30 * (1 + Dungeon.hero.pointsInTalent(Talent.DIVINE_SENSE))) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	public static class DivineSenseTracker extends FlavourBuff {

		public static final float DURATION = 2 * 30f;

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.HOLY_SIGHT;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		@Override
		public void detach() {
			super.detach();
			Dungeon.observe();
			GameScene.updateFog();
		}
	}

}
