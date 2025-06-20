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
import com.zrp200.rkpd2.actors.buffs.Combo;
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
import com.zrp200.rkpd2.utils.GLog;
import com.watabou.utils.Bundle;

public class Sai extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SAI;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.3f;

		tier = 3;
		DLY = 0.5f; //2x speed
	}

	@Override
	public int max(int lvl) {
		return  Math.round(2.5f*(tier+1)) +     //10 base, down from 20
				lvl*Math.round(0.5f*(tier+1));  //+2 per level, down from +4
	}

	@Override
	public int warriorAttack(int damage, Char enemy) {
		Buff.prolong(enemy, Vulnerable.class, damage);
		return 0;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	protected ComboStrike duelistAbility() {
		return new ComboStrike();
	}

	public int abilityStat(int level) {
		//+(4+lvl) damage, roughly +60% base damage, +67% scaling
		return augment.damageFactor(3 + tier / 2 + level);
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get(this, "ability_desc", abilityStat(buffedLvl()));
		} else {
			return Messages.get(this, "typical_ability_desc", abilityStat(0));
		}
	}

	public String upgradeAbilityStat(int level){
		return "+" + abilityStat(level);
	}

	protected class ComboStrike extends MeleeAbility {

		{
			dmgBoost = abilityStat(buffedLvl());
		}

		private int recentHits;

		@Override
		protected void beforeAbilityUsed(Hero hero, Char target) {

			recentHits = 0;
			ComboStrikeTracker buff = hero.buff(ComboStrikeTracker.class);
			if (buff != null){
				recentHits = buff.hits;
				// dmgMulti = 1 + dmgMulti * buff.hits;
				dmgBoost *= buff.hits;
				buff.detach();
			}
		}

		@Override
		protected void playSFX() {
			if (recentHits >= 2) super.playSFX();
		}
	}

	public static class ComboStrikeTracker extends Buff {

		{
			type = buffType.POSITIVE;
		}

		public static int DURATION = 5;
		private float comboTime = 0f;
		public int hits = 0;

		@Override
		public int icon() {
			if (Dungeon.hero.belongings.weapon() instanceof Gloves
					|| Dungeon.hero.belongings.weapon() instanceof Sai
					|| Dungeon.hero.belongings.weapon() instanceof Gauntlet
					|| Dungeon.hero.belongings.secondWep() instanceof Gloves
					|| Dungeon.hero.belongings.secondWep() instanceof Sai
					|| Dungeon.hero.belongings.secondWep() instanceof Gauntlet) {
				return BuffIndicator.DUEL_COMBO;
			} else {
				return BuffIndicator.NONE;
			}
		}

		@Override
		public boolean act() {
			comboTime-=TICK;
			spend(TICK);
			if (comboTime <= 0) {
				detach();
			}
			return true;
		}

		public void addHit(){
			hits++;
			comboTime = 5f;

			if (hits >= 2 && icon() != BuffIndicator.NONE){
				GLog.p( Messages.get(Combo.class, "combo", hits) );
			}
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - comboTime)/ DURATION);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)comboTime);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", hits, dispTurns(comboTime));
		}

		private static final String TIME  = "combo_time";
		public static String RECENT_HITS = "recent_hits";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TIME, comboTime);
			bundle.put(RECENT_HITS, hits);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			if (bundle.contains(TIME)){
				comboTime = bundle.getInt(TIME);
				hits = bundle.getInt(RECENT_HITS);
			} else {
				//pre-2.4.0 saves
				comboTime = 5f;
				hits = 0;
				if (bundle.contains(RECENT_HITS)) {
					for (int i : bundle.getIntArray(RECENT_HITS)) {
						hits += i;
					}
				}
			}
		}
	}

}
