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

package com.zrp200.rkpd2.items.weapon.melee;

import com.zrp200.rkpd2.Assets;
import com.zrp200.rkpd2.Dungeon;
import com.zrp200.rkpd2.actors.Actor;
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Flail extends MeleeWeapon {

	{
		image = ItemSpriteSheet.FLAIL;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 0.8f;

		tier = 4;
		ACC = 0.8f; //0.8x accuracy
		//also cannot surprise attack, see Hero.canSurpriseAttack
	}

	@Override
	public int max(int lvl) {
		return  Math.round(7*(tier+1)) +        //35 base, up from 25
				lvl*Math.round(1.6f*(tier+1));  //+8 per level, up from +5
	}

	public static int slamDamageRoll(int level){
		return Random.NormalIntRange(
				Math.round(10 + 2.5f * level), //10 min, from 4
				Math.round(87.5f + 10 * level) //87.5 max, from 25
		);
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		int dmg = slamDamageRoll(buffedLvl());
		for (int i = 1; i < 4; i++) {
			int dmgReroll = slamDamageRoll(buffedLvl());
			if (dmgReroll > dmg) dmg = dmgReroll;
		}
		return dmg;
	}

	@Override
	public float warriorDelay() {
		return 4f;
	}

	private static int spinBoost = 0;

	@Override
	public int damageRoll(Char owner) {
		int dmg = super.damageRoll(owner) + spinBoost;
		if (spinBoost > 0) Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
		spinBoost = 0;
		return dmg;
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		SpinAbilityTracker spin = owner.buff(SpinAbilityTracker.class);
		if (spin != null) {
			Actor.add(new Actor() {
				{ actPriority = VFX_PRIO; }
				@Override
				protected boolean act() {
					if (owner instanceof Hero && !target.isAlive()){
						onAbilityKill((Hero)owner, target);
					}
					Actor.remove(this);
					return true;
				}
			});
			//we detach and calculate bonus here in case the attack misses (e.g. vs. monks)
			spin.detach();
			//+(8+2*lvl) damage per spin, roughly +40% base damage, +45% scaling
			// so +120% base dmg, +135% scaling at 3 spins
			spinBoost = spin.spins * augment.damageFactor(8 + 2*buffedLvl());
			return Float.POSITIVE_INFINITY;
		} else {
			spinBoost = 0;
			return super.accuracyFactor(owner, target);
		}
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target){
		if (Dungeon.hero.buff(SpinAbilityTracker.class) != null){
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {

		SpinAbilityTracker spin = hero.buff(SpinAbilityTracker.class);
		if (spin != null && spin.spins >= 3){
			GLog.w(Messages.get(this, "spin_warn"));
			return;
		}

		beforeAbilityUsed(hero, null);
		if (spin == null){
			spin = Buff.affect(hero, SpinAbilityTracker.class, 3f);
		}

		spin.spins++;
		Buff.prolong(hero, SpinAbilityTracker.class, 3f);
		Sample.INSTANCE.play(Assets.Sounds.CHAINS, 1, 1, 0.9f + 0.1f*spin.spins);
		hero.sprite.operate(hero.pos);
		hero.spendAndNext(Actor.TICK);
		BuffIndicator.refreshHero();

		afterAbilityUsed(hero);
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? 8 + 2*buffedLvl() : 8;
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(dmgBoost));
		} else {
			return Messages.get(this, "typical_ability_desc", augment.damageFactor(dmgBoost));
		}
	}

	public String upgradeAbilityStat(int level){
		return "+" + augment.damageFactor(8 + 2*level);
	}

	public static class SpinAbilityTracker extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		public int spins = 0;

		@Override
		public int icon() {
			return BuffIndicator.DUEL_SPIN;
		}

		@Override
		public void tintIcon(Image icon) {
			switch (spins){
				case 1:
					icon.hardlight(0, 1, 0);
					break;
				case 2:
					icon.hardlight(1, 1, 0);
					break;
				case 3:
					icon.hardlight(1, 0, 0);
					break;
				default:
					icon.hardlight(1, 0.8f, 0.8f);
					break;
			}
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (3 - visualcooldown()) / 3);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", (int)Math.round((spins/3f)*100f), dispTurns());
		}

		public static String SPINS = "spins";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(SPINS, spins);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			spins = bundle.getInt(SPINS);
		}
	}
}
