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
import com.zrp200.rkpd2.actors.Char;
import com.zrp200.rkpd2.actors.buffs.Buff;
import com.zrp200.rkpd2.actors.buffs.FlavourBuff;
import com.zrp200.rkpd2.actors.buffs.MagicImmune;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.actors.hero.Talent;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;

public class Sword extends MeleeWeapon implements Talent.SpellbladeForgeryWeapon {
	
	{
		image = ItemSpriteSheet.SWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 3;
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		if (enchantment != null && Dungeon.hero.buff(MagicImmune.class) == null) {
			damage = enchantment.proc( this, Dungeon.hero, enemy, damage );
		}
		return super.warriorAttack(damage, enemy);
	}
	@Override
	protected int baseChargeUse(Hero hero, Char target){
		if (hero.buff(Sword.CleaveTracker.class) != null){
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected MeleeAbility duelistAbility() {
		//+(5+lvl) damage, roughly +45% base dmg, +40% scaling
		int dmgBoost = augment.damageFactor(abilityStat(buffedLvl()));
		return cleaveAbility(dmgBoost);
	}

	public int abilityStat(int level) {
		//+(5+lvl) damage, roughly +45% base dmg, +40% scaling
		return tier + 2 + level;
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = abilityStat(levelKnown ? buffedLvl() : 0);
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(min()+dmgBoost), augment.damageFactor(max()+dmgBoost));
		} else {
			return Messages.get(this, "typical_ability_desc", min(0)+dmgBoost, max(0)+dmgBoost);
		}
	}

	public String upgradeAbilityStat(int level){
		return augment.damageFactor(min(level)+ abilityStat(level)) + "-" + augment.damageFactor(max(level)+ abilityStat(level));
	}

	public static MeleeAbility cleaveAbility(int boost){
		return new MeleeAbility() {
			{
				this.dmgBoost = boost;
			}
			@Override
			public void onKill(Hero hero) {
				delayMulti = 0;
				if (hero.buff(CleaveTracker.class) != null) {
					hero.buff(CleaveTracker.class).detach();
				} else {
					Buff.prolong(hero, CleaveTracker.class, 4f); //1 less as attack was instant
				}
			} @Override
			protected void proc(Hero hero, Char enemy) {
				if (hero.buff(CleaveTracker.class) != null) {
					hero.buff(CleaveTracker.class).detach();
				}
			}
		};
	}

	public static class CleaveTracker extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.DUEL_CLEAVE;
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (5 - visualcooldown()) / 5);
		}
	}

}
