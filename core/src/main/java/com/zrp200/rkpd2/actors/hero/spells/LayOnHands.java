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

import com.watabou.utils.PathFinder;
import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Adrenaline;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.cleric.PowerOfMany;
import com.zrp200.rkpd2.effects.FloatingText;
import com.zrp200.rkpd2.items.artifacts.HolyTome;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.CharSprite;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.Arrays;
import java.util.List;

public class LayOnHands extends TargetedClericSpell {

	public static LayOnHands INSTANCE = new LayOnHands();

	@Override
	public int icon() {
		return HeroIcon.LAY_ON_HANDS;
	}

	@Override
	public void tintIcon(HeroIcon icon) {
		// todo make icon
		if (SpellEmpower.isActive()) icon.tint(0, .33f);
	}

	private static final int STACKS = 3;

	@Override
	protected List<Object> getDescArgs() {
		int points = 1 + Dungeon.hero.pointsInTalent(Talent.LAY_ON_HANDS);
		int totalHeal = 5 * points;
		return Arrays.asList(
				/* standard heal */ totalHeal,
				/* standard adrenaline */ getAdrenalineDuration(false),
				/* empowered heal */ totalHeal * 2,
				/* direct adrenaline */ getAdrenalineDuration(true),
				/* cap */ totalHeal * STACKS);
	}

	@Override
	public int targetingFlags(){
		return -1; //auto-targeting behaviour is often wrong, so we don't use it
	}

	@Override
	public boolean canCast(Hero hero) {
		return super.canCast(hero) && hero.shiftedPoints(Talent.LAY_ON_HANDS) > (SpellEmpower.isActive() ? 0 : 1);
	}

	@Override
	protected void onTargetSelected(HolyTome tome, Hero hero, Integer target) {
		if (target == null) {
			return;
		}

		if (Dungeon.level.distance(hero.pos, target) > 1){
			if (hero.hasTalent(Talent.TRIAGE) && Actor.findChar(target) != null) {
				int leapPos = Combo.Leap.findLeapPos(hero, target, hero.pointsInTalent(Talent.TRIAGE));
				if (leapPos == -1) {
					Combo.Leap.onInvalid(Messages.get(this, "invalid_target"));
					return;
				}
				Combo.Leap.execute(hero, leapPos, () -> onTargetSelected(tome, hero, target));
				return;
			}

			GLog.w(Messages.get(this, "invalid_target"));
			return;
		}

		Char ch = Actor.findChar(target);
		if (ch == null){
			GLog.w(Messages.get(this, "no_target"));
			return;
		}

		Sample.INSTANCE.play(Assets.Sounds.TELEPORT);

		Char ally = PowerOfMany.getPoweredAlly();
		Char toDuplicate = null;
		boolean duplicateOriginal = ch == ally || ch == hero;
		if (ally != null && ally.buff(LifeLinkSpell.LifeLinkSpellBuff.class) != null) {
			if (ch == ally) {
				toDuplicate = hero;
			} else if (ch == hero || SpellEmpower.isActive()){
				toDuplicate = ally;
			}
		}

		// life link causes the the doubled effect to duplicate if either the hero or ally is the target,

		if (SpellEmpower.isActive()) {
			for (int i : PathFinder.NEIGHBOURS8) {
				Char c = Actor.findChar(target+i);
				if (c != null) {
					boolean originalTarget = c == ch;
					if (c == toDuplicate) {
						originalTarget = duplicateOriginal;
						toDuplicate = null; // duplicate was handled
					}
					affectChar(hero, ch, originalTarget);
				}
			}
		} else {
			affectChar(hero, ch, true);
		}

		if (ch == hero){
			hero.sprite.operate(ch.pos);
			hero.next();
		} else {
			hero.sprite.zap(ch.pos);
			hero.next();
		}


		//if cast on hero, duplicate to ally
		//if cast on ally, duplicate to hero
		if (toDuplicate != null) affectChar(hero, ch, duplicateOriginal);

		onSpellCast(tome, hero);

	}

	private void affectChar(Hero hero, Char ch, boolean originalTarget) {
		int totalHeal = 5 + 5*hero.pointsInTalent(Talent.LAY_ON_HANDS);
		if (SpellEmpower.isActive() && originalTarget) {
			totalHeal *= 2;
		}
		affectChar(hero, ch, totalHeal, STACKS);
		float duration = getAdrenalineDuration(originalTarget);
		if (ch == hero) duration -= 0.5f; // compensate for being instant
		Buff.affect(ch, Adrenaline.class,  duration);
	}

	private static int getAdrenalineDuration(boolean direct) {
		float duration = Dungeon.hero.pointsInTalent(Talent.LAY_ON_HANDS);
		if (SpellEmpower.isActive()) {
			duration++; // 1 / 2 / 3 / 4
			if (direct) duration = duration * 2; // 2 / 4 / 6 / 8
		}
		return (int)duration;
	}

	public static void affectChar(Hero hero, Char ch, int totalHeal, int stacks){
		Barrier barrier = Buff.affect(ch, Barrier.class);
		int totalBarrier = 0;
		if (ch == hero){
			totalBarrier = totalHeal;
			totalBarrier = Math.min(stacks*totalHeal - barrier.shielding(), totalBarrier);
			totalBarrier = Math.max(0, totalBarrier);
			barrier.incShield(totalBarrier);
		} else {
			if (ch.HT - ch.HP < totalHeal){
				totalBarrier = totalHeal - (ch.HT - ch.HP);
				totalBarrier = Math.min(stacks*totalHeal - barrier.shielding(), totalBarrier);
				totalBarrier = Math.max(0, totalBarrier);
				if (ch.HP != ch.HT) {
					ch.HP = ch.HT;
					ch.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(totalHeal - totalBarrier), FloatingText.HEALING);
				}
				if (totalBarrier > 0) {
					barrier.incShield(totalBarrier);
				}
			} else {
				ch.HP = ch.HP + totalHeal;
				ch.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(totalHeal), FloatingText.HEALING );
			}
		}
	}
}
