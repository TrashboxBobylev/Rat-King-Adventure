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
import com.zrp200.rkpd2.actors.buffs.Vulnerable;
import com.zrp200.rkpd2.actors.hero.Hero;
import com.zrp200.rkpd2.messages.Messages;
import com.zrp200.rkpd2.sprites.ItemSpriteSheet;
import com.zrp200.rkpd2.ui.BuffIndicator;
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

	protected DuelistAbility duelistAbility() {
		return new ComboStrike();
	}

	protected class ComboStrike extends MeleeAbility {

		// 1 - 0.45
		// 3 - 0.4
		// 5 - 0.35
		public final float boostPerHit = 0.45f - (tier-1)*.025f;

		private int recentHits;

		@Override
		protected void beforeAbilityUsed(Hero hero, Char target) {

			recentHits = 0;
			ComboStrikeTracker buff = hero.buff(ComboStrikeTracker.class);
			if (buff != null){
				recentHits = buff.totalHits();
				buff.detach();
			}
		}

		@Override
		public float dmgMulti(Char enemy) {
			return boostPerHit*recentHits;
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

		public static int DURATION = 6; //to account for the turn the attack is made in
		public int[] hits = new int[DURATION];

		@Override
		public int icon() {
			//pre-v2.1 saves
			if (totalHits() == 0) return BuffIndicator.NONE;

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

			//shuffle all hits down one turn
			for (int i = 0; i < DURATION; i++){
				if (i == DURATION-1){
					hits[i] = 0;
				} else {
					hits[i] =  hits[i+1];
				}
			}

			if (totalHits() == 0){
				detach();
			}

			spend(TICK);
			return true;
		}

		public void addHit(){
			hits[DURATION-1]++;
		}

		public int totalHits(){
			int sum = 0;
			for (int i = 0; i < DURATION; i++){
				sum += hits[i];
			}
			return sum;
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(totalHits());
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", totalHits());
		}

		public static String RECENT_HITS = "recent_hits";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(RECENT_HITS, hits);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			if (bundle.contains(RECENT_HITS)) {
				hits = bundle.getIntArray(RECENT_HITS);
			}
		}
	}

}
