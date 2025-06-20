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

import com.watabou.noosa.Image;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.effects.Speck;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

public class AuraOfProtection extends ClericSpell {

	public static AuraOfProtection INSTANCE = new AuraOfProtection();

	@Override
	public int icon() {
		return HeroIcon.AURA_OF_PROTECTION;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		if (SpellEmpower.isActive()) icon.tint(1,0, 0, .5f);
	}

	public static float reduction() {
		return .1f * Math.max(1, Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION));
	}

	@Override
	public String desc() {
		int dmgReduction = Math.round(reduction() * 100);
		int glyphPow = 25 + 25*Dungeon.hero.pointsInTalent(Talent.AURA_OF_PROTECTION);
		AuraBuff buff = SpellEmpower.isActive() ? new RetributionBuff() : new AuraBuff();;
		return Messages.get(this, "desc", dmgReduction, glyphPow, (int)buff.getDuration(), (int)buff.getTurnsPerCharge()) + "\n\n" + Messages.get(this, "charge_cost", (int)chargeUse(Dungeon.hero));
	}

	@Override
	public float chargeUse(Hero hero) {
		return 2f;
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.shiftedPoints(Talent.AURA_OF_PROTECTION) > (SpellEmpower.isActive() ? 0 : 1);
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		PaladinSpellExtendable.virtualAffect(hero, AuraBuff.class,
				SpellEmpower.isActive() ? RetributionBuff.class : AuraBuff.class);
		Sample.INSTANCE.play(Assets.Sounds.READ);

		hero.spend( 1f );
		hero.busy();
		hero.sprite.operate(hero.pos);

		onSpellCast(tome, hero);

	}

	// made it extendable
	public static class AuraBuff extends PaladinSpellExtendable {

		@Override
		protected float getDuration() {
			return 20;
		}

		@Override
		public float getTurnsPerCharge() {
			// 4 turns per charge, 8 for retribution
			return getDuration() / 5;
		}

		@Override
		ClericSpell getSourceSpell() { return INSTANCE; }

		private Emitter particles;

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.PROT_AURA;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Messages.get(this, "empower")) +
					"\n\n" + getExtendableMessage() +
					"\n\n" + Messages.get(this, "turns", dispTurns());
		}

		protected int getSpeckType() {
			return Speck.LIGHT;
		}

		@Override
		public void fx(boolean on) {
			if (on && (particles == null || particles.parent == null)){
				particles = target.sprite.emitter(); //emitter is much bigger than char so it needs to manage itself
				particles.pos(target.sprite, -32, -32, 80, 80);
				particles.fillTarget = false;
				particles.pour(Speck.factory(getSpeckType()), 0.02f);
			} else if (!on && particles != null){
				particles.on = false;
			}
		}

	}

	public static class RetributionBuff extends AuraBuff {
		// fixme icon should probably be recolored or redone

		@Override
		public int icon() {
			return BuffIndicator.THORNS;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.tint(0x002157, 1/3f);
		}

		@Override
		protected int getSpeckType() {
			return Speck.RED_LIGHT;
		}

		@Override
		public float getDuration() { return 40f; }

	}

	public static boolean isActiveFor(Char ch) {
		return ch.alignment == Dungeon.hero.alignment
				&& Dungeon.hero.virtualBuff(AuraBuff.class) != null
				&& (Dungeon.level.distance(ch.pos, Dungeon.hero.pos) <= 2
						|| ch.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null
		);
	}

}
