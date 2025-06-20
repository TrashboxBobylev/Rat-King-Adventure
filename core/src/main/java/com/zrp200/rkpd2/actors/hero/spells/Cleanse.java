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
import com.zrp200.rkpd2.actors.buffs.AllyBuff;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.LostInventory;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.actors.mobs.Mob;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.items.potions.exotic.PotionOfCleansing;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Cleanse extends ClericSpell {

	public static Cleanse INSTANCE = new Cleanse();

	@Override
	public int icon() {
		return HeroIcon.CLEANSE;
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

	private int getImmunity() {
		int immunity = 1 + 2 * Dungeon.hero.pointsInTalent(Talent.CLEANSE);
		return (SpellEmpower.isActive() ? 2 : 1) * immunity;
	}

	private int getShield() {
		int shield = Math.max(10, 15 * Dungeon.hero.pointsInTalent(Talent.CLEANSE));
		return SpellEmpower.isActive() ? 2 * shield : shield;
	}

	@Override
	protected List<Object> getDescArgs() {
		float immunity = getImmunity();
		if (immunity != 0) immunity++;
		return Arrays.asList(immunity, getShield());
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && (SpellEmpower.isActive() || hero.hasTalent(Talent.CLEANSE));
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {

		ArrayList<Char> affected = new ArrayList<>();
		affected.add(hero);

		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (Dungeon.level.heroFOV[mob.pos] && mob.alignment == Char.Alignment.ALLY) {
				affected.add(mob);
			}
		}

		Char ally = PowerOfMany.getPoweredAlly();
		//hero is always affected, to just check for life linked ally
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null
				&& !affected.contains(ally)){
				affected.add(ally);
		}

		for (Char ch : affected) {
			for (Buff b : ch.buffs()) {
				if (b.type == Buff.buffType.NEGATIVE
						&& !(b instanceof AllyBuff)
						&& !(b instanceof LostInventory)) {
					b.detach();
				}
			}

			float immunity = getImmunity() - 1;
			if (immunity > 0) {
				//0, 2, or 4. 1 less than displayed as spell is instant
				Buff.affect(ch, PotionOfCleansing.Cleanse.class, immunity);
			}
			Buff.affect(ch, Barrier.class).setShield(getShield());
			new Flare( SpellEmpower.isActive() ? 12 : 6, 32 ).color(0xFF4CD2, true).show( ch.sprite, 2f );
		}

		hero.busy();
		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.READ);

		onSpellCast(tome, hero);

	}

}
