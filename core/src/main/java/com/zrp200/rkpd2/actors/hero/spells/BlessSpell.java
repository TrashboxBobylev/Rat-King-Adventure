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
import static com.zrp200.rkpd2.Dungeon.level;

import com.watabou.utils.Random;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.Bless;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.Flare;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.Arrays;
import java.util.List;

public class BlessSpell extends TargetedClericSpell {

	public static final BlessSpell INSTANCE = new BlessSpell();

	@Override
	public int icon() {
		return HeroIcon.BLESS;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo make icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	@Override
	public int targetingFlags(){
		return -1; //auto-targeting behaviour is often wrong, so we don't use it
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) &&
				(hero.hasTalent(Talent.BLESS) || SpellEmpower.isActive());
	}

	@Override
	public void onCast(HolyTome tome, Hero hero) {
		if (SpellEmpower.isActive()) {
			onTargetSelected(tome, hero, hero.pos);
		}
		else super.onCast(tome, hero);
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		Char ch = Actor.findChar(target);
		if (ch == null || !level.heroFOV[target]){
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

		boolean affectedOther = false;
		if (SpellEmpower.isActive()) {
			for (Char c : Actor.chars()) {
				if (level.heroFOV[c.pos] && c.alignment == Char.Alignment.ALLY) {
					if (c != ch) affectedOther = true;
					affectChar(hero, c);
				}
			}
		}
		if (!affectedOther) affectChar(hero, ch);

		if (ch == hero){
			hero.busy();
			hero.sprite.operate(ch.pos);
			hero.spend( 1f );
		} else {
			hero.sprite.zap(ch.pos);
			hero.spendAndNext( 1f );
		}

		Char ally = PowerOfMany.getPoweredAlly();
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null){
			if (ch == hero){
				affectChar(hero, ally); //if cast on hero, duplicate to ally
			} else if (ally == ch) {
				affectChar(hero, hero); //if cast on ally, duplicate to hero
			}
		}

		onSpellCast(tome, hero);
	}


	private int getHeal(int points, boolean spellEmpower) {
		// 10 / 15 / 25 normally
		// 15 (+5) / 25 (+10) / 40 (+15) when empowered
		int heal = points == 0 ? 10 : 5 + 10 * points;
		if (spellEmpower) heal += 5 * (points + 1);
		return heal;
	}
	private int getHeal(int points) { return getHeal(points, SpellEmpower.isActive()); }

	private int getSelfBlessDuration(int points) {
		// 6 / 10 / 15
		return points == 0 ? 6 : 5 * (1 + points);
	}

	private int getBlessDuration(int points) {
		return getHeal(points);
	}

	public int getAdrenaline(int points) {
		return SpellEmpower.isActive() ? points + 1 : points;
	}

	private void affectChar(Hero hero, Char ch){
		new Flare(6, 32).color(0xFFFF00, true).show(ch.sprite, 2f);
		int points = hero.pointsInTalent(Talent.BLESS);
		if (SpellEmpower.isActive() || ch.buff(Bless.class) != null) {
			Buff.affect(ch, Adrenaline.class, getAdrenaline(points) + 0.5f);
		}
		if (ch == hero){
			Buff.prolong(ch, Bless.class, getSelfBlessDuration(points));
		} else {
			Buff.prolong(ch, Bless.class, getBlessDuration(points));
		}
		LayOnHands.affectChar(hero, ch, getHeal(points), SpellEmpower.isActive() ? 2 : 1);
	}

	@Override
	protected List<Object> getDescArgs() {
		int talentLvl = hero.pointsInTalent(Talent.BLESS);
		return Arrays.asList(getSelfBlessDuration(talentLvl), getHeal(talentLvl), getBlessDuration(talentLvl), getHeal(talentLvl), getAdrenaline(talentLvl));
	}
}
