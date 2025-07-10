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

package com.zrp200.rkpd2.actors.hero.abilities.rogue;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Barrier;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.Terror;
import com.zrp200.rkpd2.actors.buffs.Vertigo;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.HeroClass;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.actors.hero.abilities.ArmorAbility;
import com.zrp200.rkpd2.items.armor.ClassArmor;
import com.zrp200.rkpd2.items.bombs.Bomb;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.ui.HeroIcon;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

import static com.zrp200.rkpd2.Dungeon.hero;

public class DeathMark extends ArmorAbility {

	{
		baseChargeUse = 25f;
	}

	public static float damageMultiplier() {
		if (hero.hasTalent(Talent.CATACLYSMIC_ENERGY)) return 1f;
		return hero.heroClass.isExact(HeroClass.ROGUE) ? 5/3f : 1.25f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	public int targetedPos(Char user, int dst) {
		return dst;
	}

	@Override
	public float chargeUse( Hero hero ) {
		float chargeUse = super.chargeUse(hero);
		if (hero.buff(DoubleMarkTracker.class) != null){
			chargeUse -= chargeUse*0.5f*(Math.min(2, hero.pointsInTalent(Talent.DOUBLE_MARK)));
		}
		if (hero.buff(TripleMarkTracker.class) != null){
			//reduced charge use by 33%/55%
			chargeUse *= Math.pow(0.67, Dungeon.hero.pointsInTalent(Talent.DOUBLE_MARK)-3);
		}
		return chargeUse;
	}

	@Override
    public void activate(ClassArmor armor, Hero hero, Integer target) {
		if (target == null){
			return;
		}

		Char ch = Actor.findChar(target);

		if (ch == null || !Dungeon.level.heroFOV[target]){
			GLog.w(Messages.get(this, "no_target"));
			return;
		} else if (ch.alignment != Char.Alignment.ENEMY){
			GLog.w(Messages.get(this, "ally_target"));
			return;
		}

		Buff.affect(ch, DeathMarkTracker.class, DeathMarkTracker.DURATION +
				(hero.hasTalent(Talent.CATACLYSMIC_ENERGY) ? 1 + hero.pointsInTalent(Talent.CATACLYSMIC_ENERGY) : 0)).setInitialHP(ch.HP);

		armor.useCharge(hero, this);
		hero.sprite.zap(target);

		hero.next();

		if (hero.buff(DoubleMarkTracker.class) != null){
			hero.buff(DoubleMarkTracker.class).detach();
			if (hero.pointsInTalent(Talent.DOUBLE_MARK) > 2){
				Buff.affect(hero, TripleMarkTracker.class, 0.01f);
			}
		} else if (hero.hasTalent(Talent.DOUBLE_MARK) && hero.buff(TripleMarkTracker.class) == null) {
			Buff.affect(hero, DoubleMarkTracker.class, 0.01f);
		} else if (hero.buff(TripleMarkTracker.class) != null)
			hero.buff(TripleMarkTracker.class).detach();


	}

	public static void processFearTheReaper( Char ch, boolean killingBlow ){
		if (ch.HP > 0 || ch.buff(DeathMarkTracker.class) == null){
			return;
		}

		if(killingBlow) Talent.LethalMomentumTracker.process();

		if (hero.hasTalent(Talent.FEAR_THE_REAPER)) {
			if (hero.pointsInTalent(Talent.FEAR_THE_REAPER) >= 2) {
				Buff.prolong(ch, Terror.class, 5f).object = hero.id();
			}
			Buff.prolong(ch, Vertigo.class, 5f); // was Cripple

			if (hero.pointsInTalent(Talent.FEAR_THE_REAPER) >= 3) {
				boolean[] passable = BArray.not(Dungeon.level.solid, null);
				PathFinder.buildDistanceMap(ch.pos, passable, 3);

				for (Char near : Actor.chars()) {
					if (near != ch && near.alignment == Char.Alignment.ENEMY
							&& PathFinder.distance[near.pos] != Integer.MAX_VALUE) {
						if (hero.pointsInTalent(Talent.FEAR_THE_REAPER) == 4) {
							Buff.prolong(near, Terror.class, 5f).object = hero.id();
						}
						Buff.prolong(near, Vertigo.class, 5f); // was Cripple
					}
				}
			}
		}
	}

	// FIXME generalize?
	public static class DoubleMarkTracker extends FlavourBuff{};
	public static class TripleMarkTracker extends FlavourBuff{};

	@Override
	public int icon() {
		return HeroIcon.DEATH_MARK;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.FEAR_THE_REAPER, Talent.DEATHLY_DURABILITY, Talent.DOUBLE_MARK, Talent.CATACLYSMIC_ENERGY, Talent.HEROIC_ENERGY};
	}

	@Override public boolean isTracked(Hero hero) {
		return Actor.containsClass(DeathMarkTracker.class);
	}

	@Override
	public boolean isActive(Hero hero) {
		return hero.buff(DoubleMarkTracker.class) != null;
	}

	public static class DeathMarkTracker extends FlavourBuff {

		public static float DURATION = 5f;

		int initialHP = 0;

		{
			type = buffType.NEGATIVE;
			announced = true;
		}

		@Override
		public int icon() {
			return BuffIndicator.INVERT_MARK;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1f, 0.2f, 0.2f);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - visualcooldown()) / DURATION);
		}

		public void setInitialHP(int hp){
			if (initialHP < hp){
				initialHP = hp;
			}
		}

		@Override
		public boolean attachTo(Char target) {
			if (super.attachTo(target)){
				target.deathMarked = true;
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void detach() {
			super.detach();
			target.deathMarked = false;
			if (!target.isAlive()){
				target.sprite.flash();
				target.sprite.bloodBurstA(target.sprite.center(), target.HT*2);
				Sample.INSTANCE.play(Assets.Sounds.HIT_STAB);
				Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				target.die(this);
				int shld = Math.round(initialHP * (0.125f* hero.shiftedPoints(Talent.DEATHLY_DURABILITY)));
				if (shld > 0 && target.alignment != Char.Alignment.ALLY){
					Buff.affect(hero, Barrier.class).setShield(shld);
				}
			}
		}

		private static String INITIAL_HP = "initial_hp";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(INITIAL_HP, initialHP);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			initialHP = bundle.getInt(INITIAL_HP);
		}
	}

	public static class DeathBomb extends Bomb {
		@Override
		public boolean explodesDestructively() {
			return false;
		}

		@Override
		public void explode(int cell) {
			super.explode(cell);

			ArrayList<Char> affected = new ArrayList<>();

			PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), explosionRange() );
			for (int i = 0; i < PathFinder.distance.length; i++) {
				if (PathFinder.distance[i] < Integer.MAX_VALUE) {
					Char ch = Actor.findChar(i);
					if (ch != null){
						if (ch instanceof Hero) {
							continue;
						}
						affected.add(ch);
					}
				}
			}

			for (Char ch : affected){
				//pierces armor, and damage in 5x5 instead of 3x3
				int damage = Math.round(Random.NormalIntRange( 4 + Dungeon.scalingDepth(), 12 + 3*Dungeon.scalingDepth() ));
				ch.damage(damage, this);
			}
		}
	}

}
